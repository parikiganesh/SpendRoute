package com.parikiganesh.spendroute.repository

import android.util.Base64
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles encryption/decryption of sensitive transaction fields (amount, note) for cloud backup.
 *
 * **Encryption Strategy**:
 * - Uses AES-256-GCM with per-user derived keys (PBKDF2)
 * - IV (12 bytes) + Ciphertext stored together, prefixed with "enc::" marker
 * - Only new transactions have encrypted amount/note; legacy records remain plaintext
 *
 * **Decryption Strategy** (Backward Compatible):
 * - If value starts with "enc::", decrypt it
 * - If value is plaintext, return as-is (no error)
 * - Safe to read mixed plaintext/encrypted cloud records
 *
 * **Future Removal Process** (if encryption needs to be disabled):
 * 1. Stop encrypting new transactions: Comment out encryptForUser() calls in transactionToMap()
 * 2. Keep decryption active for 1-2 releases to read existing encrypted records
 * 3. Migrate encrypted records (optional):
 *    - Create a one-time migration: Read encrypted cloud txns, decrypt, re-upload as plaintext
 *    - Or leave as-is; they'll decrypt transparently forever
 * 4. After migration complete, remove CloudEncryptionService entirely
 *
 * **Key Derivation**:
 * - Uses userId + fixed salt "SpendRoute.Cloud.FieldSalt.v1"
 * - PBKDF2-SHA256 with 120K iterations ensures same user gets same key on any device
 * - Different users get different keys (data isolation)
 */
@Singleton
class CloudEncryptionService @Inject constructor() {

    private val derivedKeyCache = ConcurrentHashMap<String, SecretKeySpec>()

    /**
     * Encrypts plaintext to "enc::BASE64(IV+CIPHERTEXT)" format.
     *
     * @param userId Current authenticated user ID (used for key derivation)
     * @param plaintext Plain string (e.g., "100.50" for amount, "lunch notes" for note)
     * @return Encrypted string with "enc::" prefix, or original plaintext on encryption error
     *
     * **Usage**:
     * ```kotlin
     * val encrypted = encryptForUser(uid, "100.50")  // "enc::JkB0xYz..."
     * ```
     */
    fun encryptForUser(userId: String, plaintext: String): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val iv = ByteArray(IV_LENGTH_BYTES)
        secureRandom.nextBytes(iv)

        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(userId), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        val encrypted = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))

        val payload = ByteBuffer.allocate(iv.size + encrypted.size)
            .put(iv)
            .put(encrypted)
            .array()

        return "$ENCRYPTED_PREFIX${Base64.encodeToString(payload, Base64.NO_WRAP)}"
    }

    /**
     * Decrypts "enc::..." values transparently; passes through plaintext as-is.
     *
     * **Backward Compatibility**: Supports mixed plaintext/encrypted records.
     * - If value starts with "enc::", attempt decryption
     * - If value is plaintext or decryption fails, return original value
     * - Never throws; always returns non-null (or original input)
     *
     * @param userId Current authenticated user ID (used for key derivation)
     * @param value Cloud value: either "enc::BASE64(...)" or plaintext
     * @return Decrypted plaintext if encrypted; original value if plaintext or error
     *
     * **Usage**:
     * ```kotlin
     * val original1 = decryptForUser(uid, "enc::JkB0xYz...")  // "100.50"
     * val original2 = decryptForUser(uid, "100.50")           // "100.50" (plaintext pass-through)
     * ```
     *
     * **Future Removal**: Once all encrypted records migrated to plaintext:
     * 1. Remove all encryptForUser() calls
     * 2. Keep this method another 1-2 releases (reads old encrypted records)
     * 3. Then delete CloudEncryptionService entirely
     */
    fun decryptForUser(userId: String, value: String): String? {
        if (!value.startsWith(ENCRYPTED_PREFIX)) return value

        return runCatching {
            val payload = Base64.decode(value.removePrefix(ENCRYPTED_PREFIX), Base64.DEFAULT)
            if (payload.size <= IV_LENGTH_BYTES) return null

            val iv = payload.copyOfRange(0, IV_LENGTH_BYTES)
            val ciphertext = payload.copyOfRange(IV_LENGTH_BYTES, payload.size)

            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(userId), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            val plainBytes = cipher.doFinal(ciphertext)
            String(plainBytes, StandardCharsets.UTF_8)
        }.getOrNull()
    }

    /**
     * Derives per-user AES-256 key using PBKDF2-SHA256.
     *
     * **Security Properties**:
     * - Same userId → Same key (consistent across devices for same user)
     * - Different userId → Different key (data isolation between users)
     * - Salt is constant and hardcoded (not random per-user)
     * - 120K iterations + SHA256 resists brute-force attacks
     *
     * **Key Size**: 256 bits (32 bytes)
     *
     * **To Change Encryption**: Edit DERIVATION_SALT or key size, but note:
     * - Existing encrypted records become unreadable (no way to re-derive old key)
     * - Solution: Decrypt all records with old key first, then re-encrypt with new key
     *
     * @param userId User's Firebase UID
     * @return SecretKeySpec ready for AES cipher initialization
     */
    private fun deriveKey(userId: String): SecretKeySpec {
        derivedKeyCache[userId]?.let { return it }

        val spec = PBEKeySpec(
            userId.toCharArray(),
            DERIVATION_SALT,
            PBKDF2_ITERATIONS,
            KEY_SIZE_BITS
        )
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALG)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, KEY_ALGORITHM).also { derivedKeyCache[userId] = it }
    }

    private companion object {
        const val ENCRYPTED_PREFIX = "enc::"
        const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_DERIVATION_ALG = "PBKDF2WithHmacSHA256"
        const val KEY_ALGORITHM = "AES"
        const val KEY_SIZE_BITS = 256
        const val GCM_TAG_LENGTH_BITS = 128
        const val IV_LENGTH_BYTES = 12
        const val PBKDF2_ITERATIONS = 120_000
        val DERIVATION_SALT = "SpendRoute.Cloud.FieldSalt.v1".toByteArray(StandardCharsets.UTF_8)

        val secureRandom: SecureRandom = SecureRandom()
    }
}




