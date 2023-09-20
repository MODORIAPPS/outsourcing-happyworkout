package com.happy.workout.utils

import android.content.Context
import android.content.SharedPreferences
import com.kakao.sdk.auth.model.OAuthToken

object AuthManager {

    private const val PREFS_NAME = "AuthPrefs"
    private const val KEY_LOGIN_METHOD = "loginMethod"

    // 로그인 방법 상수
    const val LOGIN_METHOD_EMAIL = "email"
    const val LOGIN_METHOD_KAKAO = "kakao"

    fun saveLoginMethod(context: Context, loginMethod: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_LOGIN_METHOD, loginMethod)
        editor.apply()
    }

    fun loadLoginMethod(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_LOGIN_METHOD, "") ?: ""
    }

    fun clearLoginMethod(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(KEY_LOGIN_METHOD)
        editor.apply()
    }
}
