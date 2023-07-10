package com.yellowtwigs.knockin.model.database.dao

import androidx.room.*
import com.yellowtwigs.knockin.model.database.data.NotificationDB
import kotlinx.coroutines.flow.Flow


@Dao
interface NotificationsDao {

    //region ============================================= GET ==============================================

    @Query("SELECT * FROM notifications_table ORDER BY id DESC")
    fun getAllNotifications(): Flow<List<NotificationDB>>

    @Query("SELECT * FROM notifications_table WHERE is_system = 0 ORDER BY id DESC")
    fun getMessagingNotifications(): Flow<List<NotificationDB>>

    @Query("SELECT * FROM notifications_table WHERE is_system = 1 ORDER BY id DESC")
    fun getSystemNotifications(): Flow<List<NotificationDB>>

    @Query("SELECT * FROM notifications_table ORDER BY id DESC")
    fun getAllNotificationsList(): List<NotificationDB>

    //endregion

    //region ============================================ INSERT ============================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationDB)

    //endregion

    //region ============================================ UPDATE ============================================

    @Update
    suspend fun updateNotification(notification: NotificationDB)

    //endregion

    //region ============================================ DELETE ============================================

    @Delete
    suspend fun deleteNotification(notification: NotificationDB)

    @Query("DELETE FROM notifications_table WHERE id = :id")
    suspend fun deleteNotificationById(id: Int)

    @Query("DELETE FROM notifications_table WHERE platform = :platform")
    suspend fun deleteNotificationsByPlatform(platform: String)

    @Query("DELETE FROM notifications_table WHERE is_system = 1")
    suspend fun deleteAllSystemNotifications()

    @Query("DELETE FROM notifications_table")
    suspend fun deleteAllNotifications()

    //endregion


    @Query("SELECT max(id) from notifications_table ")
    fun lastInsert(): Int

    @Query("SELECT * FROM notifications_table where :dateNow- timestamp <500")
    fun lastInsertByTime(dateNow: Long): List<NotificationDB>
}