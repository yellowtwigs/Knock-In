package com.example.knocker.model

import android.content.Context
import com.example.knocker.R
import com.example.knocker.model.ModelDB.ContactDB
import com.example.knocker.model.ModelDB.ContactDetailDB
import com.example.knocker.model.ModelDB.ContactWithAllInformation
import org.json.JSONArray
import org.json.JSONObject

object FakeContact {

    fun loadJSONFromAsset(context: Context):String{
        var json = ""
        try{
            json= context.assets.open("premiers_contacts.json").bufferedReader().use{
                it.readText()
            }
        }catch (e :Exception){
            e.printStackTrace()
            return ""
        }
        return json
    }
    fun buildList(contacts:String):List<ContactWithAllInformation>{
        var listContacts = mutableListOf<ContactWithAllInformation>()
        try {
                val jsArray = JSONArray(contacts)
                for(x in 0..(jsArray.length()-1)){
                    listContacts.add(getContactFromJSONObject(jsArray.getJSONObject(x),x))
                }
            }catch (e:Exception){
            e.printStackTrace()
        }
        return listContacts
    }
    fun getContactFromJSONObject(json: JSONObject,id:Int): ContactWithAllInformation {
        val firstName:String = json.getString("first_name")
        val lastName:String = json.getString("last_name")

        val profilPicture: Int = R.drawable.img_avatar
        val contactPriority :Int= json.getInt("contact_priority")
        val profilPictureStr:String = json.getString("profile_picture_str")

        println("contact :"+ firstName +" "+ lastName)
        val contact = ContactDB(id, firstName, lastName, profilPicture, contactPriority, profilPictureStr)
        val contactInfo = ContactWithAllInformation()
        contactInfo.contactDB= contact
        contactInfo.contactDetailList=getContactDeatailFromJSONObject(json,id)
        return contactInfo
    }
   fun getContactDeatailFromJSONObject(json: JSONObject, idContact:Int): List<ContactDetailDB> {
        val phoneNumber:String = json.getString("phone_number")
        val mail:String=json.getString("mail")
        val contactDetails = ContactDetailDB(null,idContact,phoneNumber+"M","phone", "",0)
        val contactDetails2 = ContactDetailDB(null,idContact,mail+"B","mail", "",1)
        return mutableListOf<ContactDetailDB>(contactDetails,contactDetails2)
    }
    fun getContactId(id:Int,contactList:List<ContactWithAllInformation>): ContactWithAllInformation? {
        for (contactJSON in contactList){
            if(contactJSON.contactDB!!.id==id){
                return contactJSON
            }
        }
        return null
    }
}