package com.yellowtwigs.knockin.model.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringToList(string: String): List<String> {
        return string.split(",").map { it }
    }

    @TypeConverter
    fun fromListToString(list: List<String>): String {
        return list.joinToString(separator = ",")
    }
}