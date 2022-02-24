package com.yellowtwigs.knockin.utils

import android.content.Context
import com.yellowtwigs.knockin.R

/**
 *
 * @author Florian Striebel
 */
object NumberAndMailDB {
    fun convertSpinnerStringToChar(
        type: String,
        context: Context
    ): String { //récupère la valeur du spinner pour en sortir un caractère
        val array = context.resources.getStringArray(R.array.add_new_contact_phone_number_arrays)
        val arrayMail = context.resources.getStringArray(R.array.add_new_contact_mail_arrays)
        when (type) {
            context.getString(R.string.edit_contact_phone_number_mobile) -> {
                return "mobil"
            }
            array[1] -> {
                return "work"
            }
            array[2] -> {
                return "home"
            }
            array[3] -> {
                return "principal"
            }
            array[4] -> {
                return "other"
            }
            array[5] -> {
                return "custom"
            }
            arrayMail[2] -> {
                return "school"
            }
            else -> return " "
        }
    }

    fun convertSpinnerMailStringToChar(
        type: String,
        mail: String,
        context: Context
    ): String {//méthode pour ajouter le champ ou non mail
        return if (mail == "") {
            ""
        } else {
            convertSpinnerStringToChar(type, context)
        }
    }

    fun convertStringToSpinnerString(type: String, context: Context): String {
        val array = context.resources.getStringArray(R.array.add_new_contact_phone_number_arrays)
        val arrayMail = context.resources.getStringArray(R.array.add_new_contact_mail_arrays)
        when (type) {
            "mobil" -> {
                return array[0]
            }
            "work" -> {
                return array[1]
            }
            "home" -> {
                return array[2]
            }
            "principal" -> {
                return array[3]
            }
            "other" -> {
                return array[4]
            }
            "custom" -> {
                return array[5]
            }
            "school" -> {
                return arrayMail[2]
            }
            else -> return " "
        }
    }
}