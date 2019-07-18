package com.example.knocker.model.requestDB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.knocker.model.ModelDB.ContactDetailDB
import com.example.knocker.model.ModelDB.ContactDB
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import com.example.knocker.model.ModelDB.GroupDB

/**
 * Interface réunissent les différentes requêtes d'interaction avec la table contact
 * @author Florian Striebel, Ryan Granet
 */
@Dao
interface ContactsDao {
    /**
     * Récupère les [contacts][ContactWithAllInformation] trier par prénom A->Z
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT * FROM contacts_table ORDER BY first_name COLLATE NOCASE ASC")
    fun sortContactByFirstNameAZ(): List<ContactWithAllInformation>
    /**
     * Récupère les [contacts][ContactDB] trier par prénom Z->A
     * @return List&lt[ContactDB]&gt
     */
    @Query("SELECT * FROM contacts_table ORDER BY first_name COLLATE NOCASE DESC")
    fun sortContactByFirstNameZA(): List<ContactWithAllInformation>
    /**
     * Récupère les [contacts][ContactDB] trier par nom A->Z
     * @return List&lt[ContactDB]&gt
     */
    @Query("SELECT * FROM contacts_table ORDER BY last_name COLLATE NOCASE ASC")
    fun sortContactByLastNameAZ(): List<ContactWithAllInformation>
    /**
     * Récupère les [contacts][ContactDB] trier par nom Z->A
     * @return List&lt[ContactDB]&gt
     */
    @Query("SELECT * FROM contacts_table ORDER BY last_name COLLATE NOCASE DESC")
    fun sortContactByLastNameZA(): List<ContactWithAllInformation>
    /**
     * Récupere les [contacts][ContactDB] trier par priorité 0->2
     * @return List&lt[ContactDB]&gt
     */
    @Query("SELECT * FROM contacts_table ORDER BY contact_priority ASC")
    fun sortContactByPriority02(): List<ContactWithAllInformation>
    /**
     * Récupere les [contacts][ContactDB] trier par priorité 2->0
     * @return List&lt[ContactDB]&gt
     */
    @Query("SELECT * FROM contacts_table ORDER BY contact_priority DESC,first_name COLLATE NOCASE ASC")
    fun sortContactByPriority20(): List<ContactWithAllInformation>
    /**
     * Récupere tout les [contacts][ContactDB] de la base de données
     * @return List&lt[ContactDB]&gt
     */
    @Query("SELECT * FROM contacts_table ORDER BY id ASC")
    fun getAllContacts(): List<ContactDB>
    /**
     * Récupere un [contacts][ContactWithAllInformation] grâce à son id
     * @param id Int     Id du contact sélectionné
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT * FROM contacts_table WHERE id = :id")
    fun getContact(id: Int): ContactWithAllInformation
    /**
     * Récupere les [contacts][ContactDB] grâce à leurs nom ou prénom (pour la searchbar)
     * @param name String     nom ou prénom du contact
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT * FROM contacts_table WHERE instr(lower(first_name), lower(:name)) > 0 OR instr(lower(last_name), lower(:name)) > 0")
    fun getContactByName(name: String): List<ContactWithAllInformation>
    /**
     * Sauvegarde dans la base de données un [contacts][ContactDB]
     * @param contactDB ContactDB     Objet [Contact][ContactDB]
     * @return Long id du contact qu'on va sauvegarder
     */
    @Insert
    fun insert(contactDB: ContactDB):Long?
    /**
     * Sauvegarde une liste de [ContactDetailDB] dans la base de données
     * @param contactDetails List&lt[ContactDetailDB]&gt     Liste de contact avec toute leurs informations
     */
    @Insert
    fun insertDetails(contactDetails: List<ContactDetailDB>)
    /**
     * Update un [contact][ContactDB] grace à son id
     * @param id Int     Id du contact sélectionné
     * @param firstName String  Prénom du contact
     * @param lastName String   Nom du contact
     * @param profilePicture Int    avatar du contact
     * @param profilePicture64 String   image du contact
     * @param priority Int  priorité du contact
     */
    @Query("UPDATE contacts_table SET first_name = :firstName, last_name = :lastName, profile_picture_str = :profilePicture64, contact_priority = :priority WHERE id = :id")
    fun updateContactById(id: Int, firstName: String, lastName: String, profilePicture64: String, priority: Int)
    /**
     * Update un [contact][ContactDB] sans image de profil grace à son id
     * @param id Int     Id du contact sélectionné
     * @param firstName String  Prénom du contact
     * @param lastName String   Nom du contact
     * @param profilePicture Int    avatar du contact
     */
    @Query("UPDATE contacts_table SET first_name = :firstName, last_name = :lastName, contact_priority = :priority WHERE id = :id")
    fun updateContactByIdWithoutPic(id: Int, firstName: String, lastName: String, priority: Int)
    /**
     * Update un [contact][ContactDB] apres une synchronisation grace à son id
     * @param id Int     Id du contact sélectionné
     * @param firstName String  Prénom du contact
     * @param lastName String   Nom du contact
     */
    @Query("UPDATE contacts_table SET first_name = :firstName, last_name = :lastName WHERE id = :id")
    fun updateContactByIdSync(id: Int, firstName: String, lastName: String)
    /**
     * Supprime un [contacts][ContactDB] grâce à son id
     * @param id Int     Id du contact sélectionné
     */

    @Query("UPDATE contacts_table SET contact_priority=2 WHERE id= :id ")
    fun setPriority2(id:Int)
    /**
     * UPDATE la priorité d'un contact en priorité 2
     * @param id Int    Id du contact sélectionné
     */

    @Query("DELETE FROM contacts_table WHERE id = :id")
    fun deleteContactById(id: Int)
    /**
     * Supprime tout les [contacts][ContactDB] de la base de données
     */
    @Query("DELETE FROM contacts_table")
    fun deleteAll()
    /**
     * Récupere les [contacts][ContactWithAllInformation] qui possèdent un mail
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT contacts_table.* FROM contacts_table INNER JOIN contact_details_table ON contact_details_table.id_contact = contacts_table.id WHERE type='mail'")
    fun getContactWithMail(): List<ContactWithAllInformation>
    /**
     * Récupere les [contacts][ContactWithAllInformation] qui possèdent un numéro de téléphone
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT contacts_table.* FROM contacts_table INNER JOIN contact_details_table ON contact_details_table.id_contact = contacts_table.id WHERE type='phone'")
    fun getContactWithPhoneNumber(): List<ContactWithAllInformation>
    /**
     * Récupere les [contacts][ContactDB]
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT * FROM contacts_table")
    fun getContactAllInfo():List<ContactWithAllInformation>

    @Query("SELECT * FROM contacts_table INNER JOIN link_contact_group_table ON contacts_table.id=link_contact_group_table.id_group WHERE id_group=:groupId")
    fun getContactForGroup(groupId:Int): List<ContactWithAllInformation>
}