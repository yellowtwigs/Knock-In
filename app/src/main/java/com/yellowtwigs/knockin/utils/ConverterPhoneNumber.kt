package com.yellowtwigs.knockin.utils

object ConverterPhoneNumber {
    fun converter06To33(phoneNumber: String): String {
        return if (phoneNumber[0] == '0') {
            "+33$phoneNumber"
        } else phoneNumber
    }
}