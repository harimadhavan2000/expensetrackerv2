package com.smsexpensetracker

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionData::class, CapturedSms::class],
    version = 2,
    exportSchema = false
)
abstract class TransactionDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    abstract fun capturedSmsDao(): CapturedSmsDao
    
    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `captured_sms` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sender` TEXT NOT NULL,
                        `messageBody` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `isFromBank` INTEGER NOT NULL,
                        `bankName` TEXT,
                        `isParsed` INTEGER NOT NULL,
                        `parsingError` TEXT,
                        `transactionId` INTEGER
                    )
                """.trimIndent())
            }
        }
        
        fun getDatabase(context: Context): TransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    "transaction_database"
                )
                .addMigrations(MIGRATION_1_2)
                .addCallback(DatabaseCallback())
                .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Populate database with sample data if needed
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        // Add any initial setup here if needed
                    }
                }
            }
        }
    }
}