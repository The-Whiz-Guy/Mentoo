package com.example.model5

import android.content.Context

fun saveRole(context: Context, userId: String?, role: String) {
    val sharedPreferences = context.getSharedPreferences("MentorConnectPrefs", Context.MODE_PRIVATE)
    userId?.let {
        with(sharedPreferences.edit()) {
            putString("role_$it", role)
            apply()
        }
    }
}

fun getStoredRole(context: Context, userId: String?): String? {
    val sharedPreferences = context.getSharedPreferences("MentorConnectPrefs", Context.MODE_PRIVATE)
    return userId?.let { sharedPreferences.getString("role_$it", null) }
}
