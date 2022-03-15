package com.yellowtwigs.knockin.model.dao

import androidx.room.*
import com.yellowtwigs.knockin.model.data.ContactDetailDB
import com.yellowtwigs.knockin.model.data.ContactDB
import com.yellowtwigs.knockin.model.data.ContactWithAllInformation
import kotlinx.coroutines.flow.Flow

/**
 * Interface réunissent les différentes requêtes d'interaction avec la table contact
 * @author Florian Striebel, Ryan Granet
 */
@Dao
interface ContactsDao {

    //region ============================================= GET ==============================================

    /**
     * Récupere tout les [contactList][ContactDB] de la base de données
     * @return List&lt[ContactDB]&gt
     */
    @Query("SELECT * FROM contacts_table ORDER BY id ASC")
    fun getAllContacts(): Flow<List<ContactDB>>

    /**
     * Récupere un [contactList][ContactWithAllInformation] grâce à son id
     * @param id Int     Id du contact sélectionné
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT * FROM contacts_table WHERE id = :id")
    fun getContact(id: Int): Flow<ContactWithAllInformation>

    /**
     * Récupere les [contactList][ContactDB] grâce à leurs nom ou prénom (pour la searchbar)
     * @param name String     nom ou prénom du contact
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT * FROM contacts_table WHERE instr(lower(first_name||' '||last_name), lower(:name)) > 0")
    fun getContactByName(name: String): Flow<List<ContactWithAllInformation>>

    /**
     * Récupere les [contactList][ContactWithAllInformation] qui possèdent un mail
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT contacts_table.* FROM contacts_table INNER JOIN contact_details_table ON contact_details_table.id_contact = contacts_table.id WHERE type='mail'")
    fun getContactWithMail(): Flow<List<ContactWithAllInformation>>

    /**
     * Récupere les [contactList][ContactWithAllInformation] qui possèdent un numéro de téléphone
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT contacts_table.* FROM contacts_table INNER JOIN contact_details_table ON contact_details_table.id_contact = contacts_table.id WHERE type='phone'")
    fun getContactWithPhoneNumber(): Flow<List<ContactWithAllInformation>>

    /**
     * Récupere les [contactList][ContactDB]
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT * FROM contacts_table")
    fun getContactAllInfo(): Flow<List<ContactWithAllInformation>>

    @Query("SELECT * FROM contacts_table INNER JOIN link_contact_group_table ON contacts_table.id=link_contact_group_table.id_contact WHERE id_group=:groupId")
    fun getContactForGroup(groupId: Int): Flow<List<ContactWithAllInformation>>

    //endregion

    //region ============================================ INSERT ============================================

    /**
     * Sauvegarde dans la base de données un [contactList][ContactDB]
     * @param contactDB ContactDB     Objet [Contact][ContactDB]
     * @return Long id du contact qu'on va sauvegarder
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contactDB: ContactDB): Long?

    /**
     * Sauvegarde une liste de [ContactDetailDB] dans la base de données
     * @param contactDetails List&lt[ContactDetailDB]&gt     Liste de contact avec toute leurs informations
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetails(contactDetails: List<ContactDetailDB>)

    //endregion

    //region ============================================ UPDATE ============================================

    /**
     * Update un [contact][ContactDB]
     */
    @Update
    suspend fun updateContact(contact: ContactDB)

    /**
     * @param id Int    Id du contact sélectionné
     * @param notificationTone String   sound de notification
     *
     */
    @Query("UPDATE contacts_table SET notification_tone = :notificationTone WHERE id = :id ")
    fun setNotification(id: Int, notificationTone: String)

    /**
     * Update un [contact][ContactDB] grace à son id
     * @param vip_schedule Int
     */
    @Query("UPDATE contacts_table SET vip_schedule = :vipSchedule WHERE id = :id")
    fun updateSchedule(id: Int, vipSchedule: Int)

    /**
     * @param id Int    Id du contact sélectionné
     * @param hourLimitForNotification String sound de notification
     *
     */
    @Query("UPDATE contacts_table SET hour_limit_for_notification = :hourLimitForNotification WHERE id = :id ")
    fun setHourTimeLimit(id: Int, hourLimitForNotification: String)

    /**
     * Supprime un [contactList][ContactDB] grâce à son id
     * @param id Int     Id du contact sélectionné
     */

    @Query("UPDATE contacts_table SET contact_priority = :priority WHERE id= :id ")
    fun setPriority(id: Int, priority: Int)

    /**
     * UPDATE la priorité d'un contact en priorité 2
     * @param id Int    Id du contact sélectionné
     */

    @Query("UPDATE contacts_table SET is_favorite = 1 WHERE id = :id ")
    fun setIsFavorite(id: Int)

    /**
     * UPDATE si l'user possède ce contact dans son address book whatsapp
     * @param id Int    Id du contact sélectionné
     */

    @Query("UPDATE contacts_table SET has_whatsapp = 1 WHERE id = :id ")
    fun setHasWhatsapp(id: Int)

    /**
     * UPDATE si l'user ne possède pas ce contact dans son address book whatsapp
     * @param id Int    Id du contact sélectionné
     */
    @Query("UPDATE contacts_table SET has_whatsapp = 0 WHERE id = :id ")
    fun setHasNotWhatsapp(id: Int)

    /**
     * UPDATE si le contact est un favori ou non
     * @param id Int    Id du contact sélectionné
     */

    @Query("UPDATE contacts_table SET is_favorite = 0 WHERE id = :id ")
    fun setIsNotFavorite(id: Int)

    //endregion

    /**
     * DELETE contact
     * @param contact: ContactDB
     */
    @Delete
    suspend fun deleteContact(contact: ContactDB)
}