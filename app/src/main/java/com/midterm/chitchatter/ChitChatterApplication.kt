package com.midterm.chitchatter

import android.app.Application
import com.midterm.chitchatter.data.source.DataSource
import com.midterm.chitchatter.data.source.DefaultRepository
import com.midterm.chitchatter.data.source.Repository
import com.midterm.chitchatter.data.source.local.DefaultLocalDataSource
import com.midterm.chitchatter.data.source.local.MessageDatabase
import com.midterm.chitchatter.data.source.remote.DefaultRemoteDataSource

class ChitChatterApplication : Application() {
    private lateinit var localDataSource: DataSource.LocalDataSource
    private lateinit var remoteDataSource: DataSource.RemoteDataSource
    private lateinit var database: MessageDatabase
    lateinit var repository: Repository

    override fun onCreate() {
        super.onCreate()
        setupViewModel()
    }

    private fun setupViewModel() {
        database = MessageDatabase.instance(this)!!
        localDataSource = DefaultLocalDataSource(database)
        remoteDataSource = DefaultRemoteDataSource()
        repository = DefaultRepository(
            remoteDataSource as DefaultRemoteDataSource,
            localDataSource as DefaultLocalDataSource
        )
    }
}