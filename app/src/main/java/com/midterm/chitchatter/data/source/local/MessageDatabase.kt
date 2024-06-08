package com.midterm.chitchatter.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.midterm.chitchatter.data.model.Message

@Database(entities = [Message::class], version = 1)
abstract class MessageDatabase : RoomDatabase() {
    abstract val messageDao: MessageDao

    companion object {
        private var _instance: MessageDatabase? = null

        @JvmStatic
        fun instance(context: Context): MessageDatabase? {
            if (_instance == null) {
                _instance = Room.databaseBuilder(
                    context.applicationContext,
                    MessageDatabase::class.java,
                    "message_db.db"
                ).build()
            }
            return _instance
        }
    }
}