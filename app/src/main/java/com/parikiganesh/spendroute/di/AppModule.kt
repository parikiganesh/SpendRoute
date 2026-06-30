package com.parikiganesh.spendroute.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.parikiganesh.spendroute.data.UserPreferences
import com.parikiganesh.spendroute.repository.CloudBackupService
import com.parikiganesh.spendroute.repository.TransactionRepository
import com.parikiganesh.spendroute.utils.NotificationPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideTransactionRepository(
        @ApplicationContext context: Context,
        cloudBackupService: CloudBackupService
    ): TransactionRepository {
        return TransactionRepository(context, cloudBackupService)
    }

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    @Provides
    @Singleton
    fun provideNotificationPreferences(@ApplicationContext context: Context): NotificationPreferences {
        return NotificationPreferences(context)
    }
}

