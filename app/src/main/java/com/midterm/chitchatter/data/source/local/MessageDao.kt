package com.midterm.chitchatter.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.midterm.chitchatter.data.model.Message

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg message: Message)

    @Query("DELETE FROM message")
    suspend fun clearAll()

    @Query("SELECT * FROM message")
    suspend fun all(): List<Message>
}