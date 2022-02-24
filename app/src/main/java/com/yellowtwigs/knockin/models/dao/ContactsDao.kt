package com.yellowtwigs.knockin.models.dao

import androidx.room.*
import com.yellowtwigs.knockin.models.data.Contact
import kotlinx.coroutines.flow.Flow

/**
 * Interface réunissent les différentes requêtes d'interaction avec la table contact
 * @author Florian Striebel, Ryan Granet
 */
@Dao
interface ContactsDao {

    //region ============================================== GET =============================================

    /**
     *  getAllContacts() = contact1, contact2, contact3....
     */
    @Query("SELECT * FROM contact ORDER BY first_name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    /**
     * getContact(id) = contact
     */
    @Query("SELECT * FROM contact WHERE id = :id")
    fun getContact(id: Int): Flow<Contact>

    //endregion

    //region ============================================= SORT =============================================

    /**
     * Récupère les [contactList][ContactWithAllInformation] trier par prénom A->Z
     * @return List&lt[ContactWithAllInformation]&gt
     */
    @Query("SELECT * FROM contact ORDER BY first_name COLLATE NOCASE ASC")
    fun sortContactByFirstNameAZ(): Flow<List<Contact>>

    /**
     * Récupère les [contactList][Contact] trier par nom de famille A->Z
     * @return List&lt[Contact]&gt
     */
    @Query("SELECT * FROM contact ORDER BY last_name COLLATE NOCASE ASC")
    fun sortContactByLastNameAZ(): Flow<List<Contact>>

    /**
     * Récupere les [contactList][Contact] trier par priorité 2->0
     * @return List&lt[Contact]&gt
     */
    @Query("SELECT * FROM contact ORDER BY contact_priority DESC,first_name COLLATE NOCASE ASC")
    fun sortContactByPriority20(): Flow<List<Contact>>

    /**
     * Récupere les [contactList][Contact] trier par favoris
     * @return List&lt[Contact]&gt
     */
    @Query("SELECT * FROM contact ORDER BY is_favorite DESC,first_name COLLATE NOCASE ASC")
    fun sortContactByFavorite(): Flow<List<Contact>>

    //endregion

    //region ============================================ INSERT ============================================

    /**
     * Sauvegarde dans la base de données un [contactList][Contact]
     * @param contact ContactDB     Objet [Contact][Contact]
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact)

    //endregion

    //region ============================================ UPDATE ============================================

    /**
     * Update un [contact][Contact]
     */
    @Update
    suspend fun updateContact(contact: Contact)

    //endregion

    //region ============================================ DELETE ============================================

    /**
     * Supprime un [Contact] de la base de données
     */
    @Delete
    suspend fun deleteContact(contact: Contact)

    /**
     * Supprime tous les [contactList][Contact] de la base de données
     */
    @Delete
    suspend fun deleteAll(contacts: List<Contact>)

    //endregion
}