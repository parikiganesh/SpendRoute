package com.parikiganesh.spendroute.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.parikiganesh.spendroute.data.UserPreferences
import com.parikiganesh.spendroute.data.local.SpendRouteDatabase
import com.parikiganesh.spendroute.data.local.dao.TransactionDao
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
    fun provideSpendRouteDatabase(
        @ApplicationContext context: Context
    ): SpendRouteDatabase {
        return Room.databaseBuilder(
            context,
            SpendRouteDatabase::class.java,
            "spend_route_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTransactionDao(database: SpendRouteDatabase): TransactionDao = database.transactionDao()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideTransactionRepository(
        transactionDao: TransactionDao,
        cloudBackupService: CloudBackupService
    ): TransactionRepository {
        return TransactionRepository(transactionDao, cloudBackupService)
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

