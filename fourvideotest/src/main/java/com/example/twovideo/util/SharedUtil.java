package com.example.twovideo.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedUtil {

    public static SharedPreferences.Editor editor;

    public static SharedPreferences prefs;

    /**
     * 保存持久化的值
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveShared(Context context, String key, String value) {
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 保存持久化的值
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveShared(Context context, String key, int value) {
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * 获取持久化的值
     * @param context
     * @param key
     * @return
     */
    public static String getShared(Context context, String key,String defaultValue) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, null);
    }

    /**
     * 获取持久化的值
     * @param context
     * @param key
     * @return
     */
    public static int getShared(Context context, String key,int defaultValue) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(key, 0);
    }

}
