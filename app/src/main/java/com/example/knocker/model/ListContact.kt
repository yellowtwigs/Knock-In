package com.example.knocker.model

import android.content.Context
import com.example.knocker.R
import org.json.JSONArray
import org.json.JSONObject

object ListContact {

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
    fun buildList(contacts:String):List<Contacts>{
        var listContacts = mutableListOf<Contacts>()
        try {
                val jsArray:JSONArray = JSONArray(contacts)
                for(x in 0..(jsArray.length()-1)){
                    listContacts.add(getContactFromJSONObject(jsArray.getJSONObject(x)))
                }
            }catch (e:Exception){
            e.printStackTrace()
        }
        return listContacts
    }
    fun getContactFromJSONObject(json: JSONObject): Contacts {
        val firstName:String = json.getString("first_name")
        val lastName:String = json.getString("last_name")

        val profilPicture: Int = R.drawable.img_avatar
        var backgroundImage:Int = R.drawable.aquarius
        val contactPriority :Int= json.getInt("contact_priority")
        val profilPictureStr:String = json.getString("profile_picture_str")

        println("contact :"+ firstName +" "+ lastName)
        val contact: Contacts = Contacts(null, firstName, lastName, profilPicture, backgroundImage, contactPriority, profilPictureStr)
        return contact
    }
 /*   fun getContactDeatailFromJSONObject(json: JSONObject): ContactsDetails {
        val phoneNumber:String = json.getString("phone_number")
        val mail:String=json.getString("mail")
        val contactDetails: ContactDetails = ContactDetails(null,null,phoneNumber,"phone")
        val contactDetails2: ContactDetails = ContactDetails(null,null,phoneNumber,"phone")
        return contactDetails
    }*/
}