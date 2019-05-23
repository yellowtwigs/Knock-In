package com.example.knocker.model.requestDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.knocker.model.ModelDB.NotificationDB

@Dao
interface NotificationsDao {

    //get toutes les notification de la database
    @Query("SELECT * FROM notifications_table ORDER BY timestamp DESC")
    fun getAllnotifications(): List<NotificationDB>

    //get les notifications grace à une platform
    @Query("SELECT * FROM notifications_table WHERE platform = :platform")
    fun getNotificationByPlatform(platform: String): List<NotificationDB>

    //get une notification grace à son id
    @Query("SELECT * FROM notifications_table WHERE id = :id")
    fun getNotification(id: Int): List<NotificationDB>

    //insert la notification dans la database
    @Insert
    fun insert(notifications: NotificationDB)

    //delete une notification grace à son id
    @Query("DELETE FROM notifications_table WHERE id = :id")
    fun deleteNotificationById(id: Int)

    //delete toute les notification d'une plateforme
    @Query("DELETE FROM notifications_table WHERE platform = :platform")
    fun deleteNotificationByPlatform(platform: String)

}