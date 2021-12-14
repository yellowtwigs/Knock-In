package com.yellowtwigs.knockin.models.dao

import androidx.room.*
import com.yellowtwigs.knockin.models.data.ContactDetailDB
import kotlinx.coroutines.flow.Flow

/**
 * Interface réunissent les différentes requêtes d'interaction avec la table contact detail
 * @author Florian Striebel, Ryan Granet
 */
@Dao
interface ContactDetailsDao {

    /**
     * Récupère tout les [contactList details][ContactDetailDB] que possède un contact mail grâce à son id.
     * @param contactID Int     Id du contact sélectionné
     * @return List&lt[ContactDetailDB]&gt
     */
    @Query("SELECT * FROM contact_details_table WHERE id_contact=:contactID")
    fun getDetailsForAContact(contactID: Int): Flow<List<ContactDetailDB>>

    /**
     * Update un [contactList details][ContactDetailDB] grâce à son id.
     * @param id Int            Id du contact sélectionné
     * @param contactDetail     détail du contact (mail, numéro de tel, etc...)
     */
    @Update
    fun updateContactDetail(contactDetail: ContactDetailDB)

    /**
     * Ajoute un [contact detail][ContactDetailDB] dans la base de données.
     * @param ContactDetailDB contactDetailDB    Objet [contact detail][ContactDetailDB]
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contactDetailDB: ContactDetailDB)

    /**
     * Supprime un [contact detail][ContactDetailDB] dans la base de données.
     * @param id Int    id du detail
     */
    @Delete
    fun deleteDetail(contactDetailDB: ContactDetailDB)

    /**
     * Supprime tout les [contact detail][ContactDetailDB] d'un contact.
     * @param id Int    id du contact
     */
    @Delete
    fun deleteAllDetailsOfContact(contactDetailDB: ContactDetailDB)

}