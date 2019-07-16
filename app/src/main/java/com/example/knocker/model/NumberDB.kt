package com.example.knocker.model

import android.content.Context
import com.example.knocker.R

/**
 *
 * @author Florian Striebel
 */
object NumberAndMailDB {
   public fun convertSpinnerStringToChar(type :String, context: Context):String{ //récupère la valeur du spinner pour en sortir un caractère
       val array = context.resources.getStringArray(R.array.add_new_contact_phone_number_arrays)
       val arrayMail = context.resources.getStringArray(R.array.add_new_contact_mail_arrays)
       if(type.equals(context.getString(R.string.edit_contact_phone_number_mobile))){
            return "mobil"
        }else if (type == array[1]){
            return "work"
        }else if(type == array[2]){
            return "home"
        }else if(type == array[3]){
            return "principal"
        }else if(type == array[4]){
            return "other"
        }else if(type == array[5]){
            return "custom"
        }else if(type == arrayMail[2]){
            return "school"
        }
       return " "
    }
    public fun convertSpinnerMailStringToChar(type :String,mail:String):String {//méthode pour ajouter le champ ou non mail
        if(mail.equals("")){
            return ""
        }else{
            return convertSpinnerStringToChar(type).toString()
        }
    }
}