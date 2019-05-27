package com.example.knocker.model.requestDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.knocker.model.ModelDB.NotificationDB

/**
 * Interface réunissent les différentes requêtes d'interaction avec la table notification
 * @author Florian Striebel, Ryan Granet
 */
@Dao
interface NotificationsDao {
    /**
     * Récupère toutes les [notifications][NotificationDB] de la database
     * @return List&lt[NotificationDB]&gt
     */
    @Query("SELECT * FROM notifications_table ORDER BY timestamp DESC")
    fun getAllnotifications(): List<NotificationDB>

    /**
     * Récupère toutes les [notifications][NotificationDB] lié à une plateforme
     * @param platform String   La plateforme sélectionnée
     * @return List&lt[NotificationDB]&gt
     */
    @Query("SELECT * FROM notifications_table WHERE platform = :platform")
    fun getNotificationByPlatform(platform: String): List<NotificationDB>

    /**
     * Récupère une [notification][NotificationDB] grâce à son id
     * @param id Int   L'id de la [notification][NotificationDB] voulu
     * @return List&lt[NotificationDB]&gt
     */
    @Query("SELECT * FROM notifications_table WHERE id = :id")
    fun getNotification(id: Int): List<NotificationDB>

    /**
     * Sauvegarde une [notification][NotificationDB] dans la Base de données
     * @param notifications NotificationDB  Objet [notification][NotificationDB]
     * @return Int  L'id de la [notification][NotificationDB] sauvegardée
     */
    @Insert
    fun insert(notifications: NotificationDB)

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
}