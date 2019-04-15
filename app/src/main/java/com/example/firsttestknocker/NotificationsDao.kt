package com.example.firsttestknocker

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface NotificationsDao {

    @Query("SELECT * FROM notifications_table")
    fun getAllnotifications(): List<Notifications>

    @Query("SELECT * FROM notifications_table WHERE id = :id")
    fun getNotification(id: Int): Notifications

    @Insert
    fun insert(notifications: Notifications)
}