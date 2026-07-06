package com.futclub.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model User - merepresentasikan data user hasil login Google.
 * Field-field ini harus PERSIS sama namanya dengan key JSON dari backend PHP,
 * atau kalau beda, kasih anotasi @SerializedName seperti di bawah.
 */
public class User {
    @SerializedName("id")
    private int id;

    @SerializedName("firebase_uid")
    private String firebaseUid;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("photo_url")
    private String photoUrl;

    @SerializedName("role")
    private String role; // "user" atau "admin"

    public int getId() { return id; }
    public String getFirebaseUid() { return firebaseUid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhotoUrl() { return photoUrl; }
    public String getRole() { return role; }

    public boolean isAdmin() {
        return "admin".equals(role);
    }
}
