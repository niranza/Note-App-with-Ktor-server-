package com.niran.ktornoteapplication.dataset.room.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringListConverter {

    @TypeConverter
    fun fromStringList(stringList: List<String>): String = Gson().toJson(stringList)

    @TypeConverter
    fun toStringList(string: String): List<String> =
        Gson().fromJson(string, object : TypeToken<List<String>>() {}.type)
}