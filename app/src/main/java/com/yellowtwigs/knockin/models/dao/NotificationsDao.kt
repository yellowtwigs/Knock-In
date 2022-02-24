package com.yellowtwigs.knockin.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yellowtwigs.knockin.model.data.NotificationDB

/**
 * Interface réunissent les différentes requêtes d'interaction avec la table notification
 * @author Florian Striebel, Ryan Granet
 */
@Dao
interface NotificationsDao {
    /**
     * Récupère toutes les [notifications][NotificationDB] de la database
     * @return List&lt[NotificationDB]&gt trié par date
     */
    @Query("SELECT * FROM notifications_table ORDER BY timestamp DESC")
    fun getAllNotifications(): List<NotificationDB>

    @Query("SELECT * FROM notifications_table WHERE instr(lower(contact_name||' '||description), lower(:name)) > 0")
    fun getNotificationFiltered(name: String): List<NotificationDB>

    @Query("SELECT * FROM notifications_table INNER JOIN contacts_table ON contacts_table.id=notifications_table.id_contact where contact_priority!=1 ORDER BY contacts_table.contact_priority DESC,timestamp DESC ")
    fun getContactWithPriority0And2(): List<NotificationDB>

    @Query("SELECT * FROM notifications_table ORDER BY contact_name DESC")
    fun getNotifSortByContact(): List<NotificationDB>

    /**
     * Récupère toutes les [notifications][NotificationDB] lié à une plateforme
     * @param platform String   La plateforme sélectionnée
     * @return List&lt[NotificationDB]&gt
     */
    @Query("SELECT * FROM notifications_table WHERE platform = :platform")
    fun getNotificationByPlatform(platform: String): List<NotificationDB>

    @Query("SELECT COUNT(*) FROM notifications_table WHERE datetime('now')- datetime(timestamp)<1")
    fun getNotificationSinceYesterday(): Int

    @Query("SELECT datetime(timestamp) FROM notifications_table")
    fun getIntTime(): List<String>

    /**
     * Récupère une [notification][NotificationDB] grâce à son id
     * @param id Int   L'id de la [notification][NotificationDB] voulu
     * @return List&lt[NotificationDB]&gt
     */
    @Query("SELECT * FROM notifications_table WHERE id = :id")
    fun getNotification(id: Int): NotificationDB

    /**
     * Sauvegarde une [notification][NotificationDB] dans la Base de données
     * @param notifications NotificationDB  Objet [notification][NotificationDB]
     * @return Int  L'id de la [notification][NotificationDB] sauvegardée
     */
    @Insert
    fun insertNotifications(notifications: NotificationDB)

    /**
     * Supprime une [notification][NotificationDB] de la Base de donnée grâce à son id
     * @param id Int    Id de la [notification][NotificationDB] qui doit être supprimé
     */
    @Query("DELETE FROM notifications_table WHERE id = :id")
    fun deleteNotificationById(id: Int)

    /**
     * Supprime les [notifications][NotificationDB] de la Base de donnée lié à une plateforme
     * @param platform String    Plateforme choisie
     */
    @Query("DELETE FROM notifications_table WHERE platform = :platform")
    fun deleteNotificationByPlatform(platform: String)


    @Query("DELETE FROM notifications_table WHERE 1=1")
    fun deleteAllNotification()


    @Query("SELECT max(id) from notifications_table ")
    fun lastInsert(): Int

    @Query("SELECT * FROM notifications_table where :dateNow- timestamp <500")
    fun lastInsertByTime(dateNow: Long): List<NotificationDB>
}