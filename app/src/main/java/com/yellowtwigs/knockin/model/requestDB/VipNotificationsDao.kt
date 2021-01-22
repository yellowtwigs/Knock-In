package com.yellowtwigs.knockin.model.requestDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yellowtwigs.knockin.model.ModelDB.VipNotificationsDB

/**
 * Interface réunissent les différentes requêtes d'interaction avec la table groups
 * @author Ryan Granet
 */
@Dao
interface VipNotificationsDao {
    //get toutes les notifications vip de la database triée par ID
    @Query("SELECT * FROM vip_notifications_table ORDER BY id")
    fun getAllVipNotificationsById(): List<VipNotificationsDB>

    @Insert
    fun insert(VipNotif: VipNotificationsDB)

    @Query("DELETE FROM vip_notifications_table")
    fun deleteAllVipNotifications()
}