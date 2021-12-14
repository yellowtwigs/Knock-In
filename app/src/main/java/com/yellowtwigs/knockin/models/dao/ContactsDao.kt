package com.yellowtwigs.knockin.models.dao

import androidx.room.*
import com.yellowtwigs.knockin.models.data.ContactDetailDB
import com.yellowtwigs.knockin.models.data.ContactDB
import com.yellowtwigs.knockin.models.data.ContactWithAllInformation
import kotlinx.coroutines.flow.Flow

/**
 * Interface réunissent les différentes requêtes d'interaction avec la table contact
 * @author Florian Striebel, Ryan Granet
 */
@Dao
interface ContactsDao {

    /**
     * Récupere tout les [contactList][ContactDB] de la base de données
     * @return List&lt[ContactDB]&gt
     */
    @Query("SELECT * FROM contacts_table ORDER BY id ASC")
    fun getAllContacts(): Flow<List<ContactDB>>

    /**
     * Sauvegarde dans la base de données un [contactList][ContactDB]
     * @param contactDB ContactDB     Objet [Contact][ContactDB]
     * @return Long id du contact qu'on va sauvegarder
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contactDB: ContactDB): Long?

    /**
     * Sauvegarde une liste de [ContactDetailDB] dans la base de données
     * @param contactDetails List&lt[ContactDetailDB]&gt     Liste de contact avec toute leurs informations
     */
    @Insert
    fun insertDetails(contactDetails: List<ContactDetailDB>)

    /**
     * Update un [contact][ContactDB]
     */
    @Update
    fun updateContact(contactDB: ContactDB)

    /**
     * Update un [contact][ContactDB] apres une synchronisation grace à son id
     * @param id Int     Id du contact sélectionné
     * @param firstName String  Prénom du contact
     * @param lastName String   Nom du contact
     */
    @Query("UPDATE contacts_table SET first_name = :firstName, last_name = :lastName WHERE id = :id")
    fun updateContactByIdSync(id: Int, firstName: String, lastName: String)

    /**
     * @param id Int    Id du contact sélectionné
     * @param notificationTone String   sound de notification
     *
     */
    @Query("UPDATE contacts_table SET notification_tone = :notificationTone WHERE id = :id ")
    fun setNotification(id: Int, notificationTone: String)

    /**
     * @param id Int    Id du contact sélectionné
     * @param notificationSound Int   sound de notification
     *
     */
    @Query("UPDATE contacts_table SET notification_Sound = :notificationSound WHERE id = :id ")
    fun setNotification(id: Int, notificationSound: Int)

    /**
     * Supprime un contact
     */
    @Delete
    fun deleteContact()

    /**
     * Supprime tout les [contactList][ContactDB] de la base de données
     */
    @Query("DELETE FROM contacts_table")
    fun deleteAll()

    /**
     * Récupere les [contactList][ContactDB]
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT * FROM contacts_table")
    fun getContactAllInfo(): List<ContactWithAllInformation>

    @Query("SELECT * FROM contacts_table INNER JOIN link_contact_group_table ON contacts_table.id=link_contact_group_table.id_contact WHERE id_group=:groupId")
    fun getContactForGroup(groupId: Int): List<ContactWithAllInformation>
}