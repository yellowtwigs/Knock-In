package com.example.knocker.model

import android.content.Context
import com.example.knocker.R

/**
 *
 * @author Florian Striebel
 */
object NumberAndMailDB {
    public fun convertSpinnerStringToChar(type: String, context: Context): String { //récupère la valeur du spinner pour en sortir un caractère
        val array = context.resources.getStringArray(R.array.add_new_contact_phone_number_arrays)
        val arrayMail = context.resources.getStringArray(R.array.add_new_contact_mail_arrays)
        if (type.equals(context.getString(R.string.edit_contact_phone_number_mobile))) {
            return "mobil"
        } else if (type == array[1]) {
            return "work"
        } else if (type == array[2]) {
            return "home"
        } else if (type == array[3]) {
            return "principal"
        } else if (type == array[4]) {
            return "other"
        } else if (type == array[5]) {
            return "custom"
        } else if (type == arrayMail[2]) {
            return "school"
        }
        return " "
    }
    public fun convertSpinnerMailStringToChar(type :String,mail:String,context: Context):String {//méthode pour ajouter le champ ou non mail
        if(mail.equals("")){
            return ""
        }else{
            return convertSpinnerStringToChar(type,context).toString()
        }
    }
    public fun convertStringToSpinnerString(type:String,context:Context):String{
        val array = context.resources.getStringArray(R.array.add_new_contact_phone_number_arrays)
        val arrayMail = context.resources.getStringArray(R.array.add_new_contact_mail_arrays)
        if(type.equals("mobil")){
            return array[0]
        }else if (type == "work"){
            return array[1]
        }else if(type ==  "home"){
            return array[2]
        }else if(type =="principal"){
            return  array[3]
        }else if(type == "other"){
            return   array[4]
        }else if(type == "custom" ){
            return array[5]
        }else if(type == "school" ){
            return arrayMail[2]
        }
        return " "
    }
}