package com.yellowtwigs.knockin.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yellowtwigs.knockin.model.data.VipSbnDB

/**
 * Interface réunissent les différentes requêtes d'interaction avec la table vip_sbn_table
 * @author Ryan Granet
 */
@Dao
interface VipSbnDao {

    @Query("SELECT * FROM vip_sbn_table WHERE vip_notification_id = :notificationId")
    fun getSbnWithNotifId(notificationId: String): List<VipSbnDB>

    @Query("DELETE FROM vip_sbn_table WHERE vip_notification_id = :notificationId")
    fun deleteSbnWithNotifId(notificationId: String)

    @Insert
    fun insert(VipSbn: VipSbnDB)

    @Query("DELETE FROM vip_sbn_table")
    fun deleteAllSbn()
}