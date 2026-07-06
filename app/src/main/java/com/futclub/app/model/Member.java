package com.futclub.app.model;

import com.google.gson.annotations.SerializedName;

public class Member {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("photo_url")
    private String photoUrl;

    @SerializedName("joined_at")
    private String joinedAt;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhotoUrl() { return photoUrl; }
    public String getJoinedAt() { return joinedAt; }
}
