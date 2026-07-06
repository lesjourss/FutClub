package com.futclub.app.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager cuma menyimpan status LOGIN user saat ini (id, nama, role, dll)
 * pakai SharedPreferences, supaya user ga perlu login ulang tiap buka app.
 *
 * PENTING: Ini BUKAN pengganti database utama. Semua data komunitas, member, gallery,
 * dll tetap 100% disimpan & diambil dari MySQL lewat REST API (sesuai syarat UAS).
 * SharedPreferences di sini cuma menyimpan "siapa yang sedang login", sama seperti
 * menyimpan token/session, bukan menyimpan data aplikasi.
 */
public class SessionManager {

    private static final String PREF_NAME = "futclub_session";
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(int userId, String name, String email, String photoUrl, String role) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("user_id", userId);
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("photo_url", photoUrl);
        editor.putString("role", role);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    public void updateRole(String role) {
        prefs.edit().putString("role", role).apply();
    }

    public void updateProfile(String name, String photoUrl) {
        prefs.edit().putString("name", name).putString("photo_url", photoUrl).apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean("is_logged_in", false);
    }

    public int getUserId() {
        return prefs.getInt("user_id", -1);
    }

    public String getName() {
        return prefs.getString("name", "");
    }

    public String getEmail() {
        return prefs.getString("email", "");
    }

    public String getPhotoUrl() {
        return prefs.getString("photo_url", null);
    }

    public String getRole() {
        return prefs.getString("role", "user");
    }

    public boolean isAdmin() {
        return "admin".equals(getRole());
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
