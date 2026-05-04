package com.parikiganesh.spendroute.utils

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Manages in-app updates from Google Play Store
 *
 * Supports two types:
 * - FLEXIBLE: User can dismiss and update later
 * - IMMEDIATE: Forces user to update before using app
 */
class InAppUpdateManager(private val context: Context) {

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(context)
    }

    private var updateListener: InstallStateUpdatedListener? = null

    /**
     * Check for available updates
     */
    fun checkForUpdates(
        onUpdateAvailable: (AppUpdateInfo) -> Unit,
        onNoUpdateAvailable: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            // Check if update is available
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // Show update options
                onUpdateAvailable(appUpdateInfo)
            } else {
                onNoUpdateAvailable()
            }
        }.addOnFailureListener { exception ->
            onError(exception)
        }
    }

    /**
     * Start FLEXIBLE update (user can dismiss)
     *
     * @param activity Activity context for launching update flow
     * @param appUpdateInfo Update info from Play Store
     * @param requestCode For onActivityResult callback
     */
    fun startFlexibleUpdate(
        activity: Activity,
        appUpdateInfo: AppUpdateInfo,
        requestCode: Int = FLEXIBLE_UPDATE_REQUEST_CODE
    ) {
        try {
            @Suppress("DEPRECATION")
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,  // Flexible update type
                activity,
                requestCode
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Start IMMEDIATE update (force update)
     *
     * @param activity Activity context for launching update flow
     * @param appUpdateInfo Update info from Play Store
     * @param requestCode For onActivityResult callback
     */
    fun startImmediateUpdate(
        activity: Activity,
        appUpdateInfo: AppUpdateInfo,
        requestCode: Int = IMMEDIATE_UPDATE_REQUEST_CODE
    ) {
        try {
            @Suppress("DEPRECATION")
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,  // Immediate/force update type
                activity,
                requestCode
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Register listener for flexible update installation state
     * Call this to track download progress and installation status
     */
    fun registerListener(listener: InstallStateUpdatedListener) {
        this.updateListener = listener
        appUpdateManager.registerListener(listener)
    }

    /**
     * Unregister listener
     * Call this in onDestroy or when no longer needed
     */
    fun unregisterListener() {
        updateListener?.let {
            appUpdateManager.unregisterListener(it)
        }
    }

    /**
     * Complete flexible update installation
     * Call this after user agrees to install
     */
    fun completeFlexibleUpdate() {
        appUpdateManager.completeUpdate()
    }

    companion object {
        const val FLEXIBLE_UPDATE_REQUEST_CODE = 100
        const val IMMEDIATE_UPDATE_REQUEST_CODE = 101
    }
}



