package com.futclub.app.model;

import com.google.gson.annotations.SerializedName;

public class GalleryPhoto {
    @SerializedName("id")
    private int id;

    @SerializedName("community_id")
    private int communityId;

    @SerializedName("photo_url")
    private String photoUrl;

    public int getId() { return id; }
    public int getCommunityId() { return communityId; }
    public String getPhotoUrl() { return photoUrl; }
}
