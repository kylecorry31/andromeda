package com.kylecorry.andromeda.json

import com.google.gson.Gson
import java.lang.Exception

object JsonConvert {

    fun toJson(obj: Any): String {
        return Gson().toJson(obj)
    }

    inline fun <reified T> fromJson(json: String): T? {
        return try {
            Gson().fromJson(json, T::class.java)
        } catch (e: Exception){
            null
        }
    }

}