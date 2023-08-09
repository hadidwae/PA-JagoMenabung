@file:Suppress("PrivatePropertyName")

package com.d3if2099.jagomenabung.model

import android.content.Context
import android.content.SharedPreferences

class Preference(context: Context)  {
    private val TAG_APP = "app"
    private val TAG_PASSWORD = "password"
    private val pref : SharedPreferences = context.getSharedPreferences(TAG_APP, Context.MODE_PRIVATE)

    var prefpassword: String?
        get() = pref.getString(TAG_PASSWORD,"")
        set(value) = pref.edit().putString(TAG_PASSWORD,value).apply()
}