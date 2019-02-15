package com.ufoto.dns.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

public class DiskUtil {
    static Map<String, String> map = new HashMap();
    private static final String SP_NAME = "dns_sp";
    public static final String KEY_TTL_TIME = "key_ttl_time";
    private static SharedPreferences preferences;

    public static void put(Context context, String key, String value) {
        map.put(key, value);
        putImpl(context, key, value);
    }

    private static void putImpl(Context context, String key, String value) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        preferences.edit().putString(key, value).apply();
    }

    public static String get(Context context, String key) {
        String ip = map.get(key);
        if (ip == null) {
            ip = getImpl(context, key);
            map.put(key, ip);
        }
        return ip;
    }

    private static String getImpl(Context context, String key) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        return preferences.getString(key, "");
    }

    public static void remove(Context context, String key) {
        map.remove(key);
        removeImpl(context, key);
    }

    private static void removeImpl(Context context, String key) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        preferences.edit().remove(key).apply();
    }
}
