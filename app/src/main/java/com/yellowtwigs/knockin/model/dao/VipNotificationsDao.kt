package com.yellowtwigs.knockin.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yellowtwigs.knockin.model.data.VipNotificationsDB

/**
 * Interface réunissent les différentes requêtes d'interaction avec la table groups
 * @author Ryan Granet
 */
@Dao
interface VipNotificationsDao {
    //get toutes les notifications vip de la database triée par ID
    @Query("SELECT * FROM vip_notifications_table ORDER BY id")
    fun getAllVipNotificationsById(): List<VipNotificationsDB>

    @Query("SELECT * FROM vip_notifications_table WHERE notification_text = :notificationContent")
    fun getVipNotificationByContent(notificationContent: String): VipNotificationsDB

    @Insert
    fun insert(VipNotif: VipNotificationsDB): Long?

    @Query("DELETE FROM vip_notifications_table WHERE id = :notificationId")
    fun deleteVipNotificationsWithId(notificationId: String)

    @Query("DELETE FROM vip_notifications_table")
    fun deleteAllVipNotifications()
}