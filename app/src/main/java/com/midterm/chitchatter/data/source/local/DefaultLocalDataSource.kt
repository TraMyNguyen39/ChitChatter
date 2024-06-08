package com.midterm.chitchatter.data.source.local

import com.midterm.chitchatter.data.model.Message
import com.midterm.chitchatter.data.source.DataSource
import com.midterm.chitchatter.data.source.remote.ResponseNoResult
import com.midterm.chitchatter.data.source.remote.ResponseResult

class DefaultLocalDataSource(private val database: MessageDatabase?) : DataSource.LocalDataSource {
    override suspend fun loadData(): ArrayList<Message> {
        val data = database?.messageDao?.all()
        return if (!data.isNullOrEmpty()) {
            ArrayList(data)
        } else {
            ArrayList() // Trả về một danh sách trống nếu không có dữ liệu
        }
    }

    override suspend fun clearDatabase(): Boolean {
        return try {
            database?.messageDao?.clearAll()
            true
        } catch (e: Exception) {
            false
        }
    }


    override suspend fun updateDatabase(messages: List<Message>): Boolean {
        return try {
            for (message in messages) {
                database?.messageDao?.insert(message)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

}