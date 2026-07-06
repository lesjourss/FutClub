package com.futclub.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Community {
    @SerializedName("id")
    private int id;

    @SerializedName("admin_id")
    private int adminId;

    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("admin_name")
    private String adminName;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("photo_url")
    private String photoUrl;

    @SerializedName("whatsapp_link")
    private String whatsappLink;

    @SerializedName("member_count")
    private int memberCount;

    @SerializedName("gallery")
    private List<GalleryPhoto> gallery; // hanya terisi kalau ambil dari community_detail.php

    public int getId() { return id; }
    public int getAdminId() { return adminId; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getAdminName() { return adminName; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPhotoUrl() { return photoUrl; }
    public String getWhatsappLink() { return whatsappLink; }
    public int getMemberCount() { return memberCount; }
    public List<GalleryPhoto> getGallery() { return gallery; }
}
