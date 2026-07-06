package com.futclub.app.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Kumpulan class kecil untuk body request POST/PUT.
 * Dipisah per aksi supaya jelas field apa saja yang wajib dikirim ke tiap endpoint.
 */
public class RequestModels {

    public static class AuthRequest {
        @SerializedName("firebase_uid") public String firebaseUid;
        @SerializedName("name") public String name;
        @SerializedName("email") public String email;
        @SerializedName("photo_url") public String photoUrl;

        public AuthRequest(String firebaseUid, String name, String email, String photoUrl) {
            this.firebaseUid = firebaseUid;
            this.name = name;
            this.email = email;
            this.photoUrl = photoUrl;
        }
    }

    public static class UserCategoriesRequest {
        @SerializedName("user_id") public int userId;
        @SerializedName("category_ids") public List<Integer> categoryIds;

        public UserCategoriesRequest(int userId, List<Integer> categoryIds) {
            this.userId = userId;
            this.categoryIds = categoryIds;
        }
    }

    public static class SetRoleRequest {
        @SerializedName("user_id") public int userId;
        @SerializedName("role") public String role;

        public SetRoleRequest(int userId, String role) {
            this.userId = userId;
            this.role = role;
        }
    }

    public static class CreateCommunityRequest {
        @SerializedName("admin_id") public int adminId;
        @SerializedName("category_id") public int categoryId;
        @SerializedName("name") public String name;
        @SerializedName("description") public String description;
        @SerializedName("photo_url") public String photoUrl;
        @SerializedName("whatsapp_link") public String whatsappLink;

        public CreateCommunityRequest(int adminId, int categoryId, String name, String description,
                                       String photoUrl, String whatsappLink) {
            this.adminId = adminId;
            this.categoryId = categoryId;
            this.name = name;
            this.description = description;
            this.photoUrl = photoUrl;
            this.whatsappLink = whatsappLink;
        }
    }

    public static class UpdateCommunityRequest {
        @SerializedName("name") public String name;
        @SerializedName("description") public String description;
        @SerializedName("photo_url") public String photoUrl;
        @SerializedName("whatsapp_link") public String whatsappLink;
    }

    public static class JoinRequest {
        @SerializedName("community_id") public int communityId;
        @SerializedName("user_id") public int userId;

        public JoinRequest(int communityId, int userId) {
            this.communityId = communityId;
            this.userId = userId;
        }
    }

    public static class AddGalleryRequest {
        @SerializedName("community_id") public int communityId;
        @SerializedName("photo_url") public String photoUrl;

        public AddGalleryRequest(int communityId, String photoUrl) {
            this.communityId = communityId;
            this.photoUrl = photoUrl;
        }
    }

    public static class UpdateProfileRequest {
        @SerializedName("name") public String name;
        @SerializedName("photo_url") public String photoUrl;
    }
}
