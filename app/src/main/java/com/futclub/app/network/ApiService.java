package com.futclub.app.network;

import com.futclub.app.model.Community;
import com.futclub.app.model.GalleryPhoto;
import com.futclub.app.model.Member;
import com.futclub.app.model.SportCategory;
import com.futclub.app.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Interface ini mendefinisikan semua endpoint REST API backend PHP kita.
 * Retrofit otomatis generate implementasinya, kita tinggal panggil method-nya saja.
 */
public interface ApiService {

    // ---- Auth ----
    @POST("auth.php")
    Call<ApiResponse<User>> login(@Body RequestModels.AuthRequest request);

    // ---- Categories ----
    @GET("categories.php")
    Call<ApiResponse<List<SportCategory>>> getCategories();

    @POST("user_categories.php")
    Call<ApiResponse<Object>> saveUserCategories(@Body RequestModels.UserCategoriesRequest request);

    @GET("user_categories.php")
    Call<ApiResponse<List<SportCategory>>> getUserCategories(@Query("user_id") int userId);

    // ---- Role ----
    @PUT("set_role.php")
    Call<ApiResponse<User>> setRole(@Body RequestModels.SetRoleRequest request);

    // ---- Communities ----
    @GET("communities.php")
    Call<ApiResponse<List<Community>>> getCommunities(@Query("category_ids") String categoryIds);

    @GET("communities.php")
    Call<ApiResponse<List<Community>>> getAllCommunities();

    @POST("communities.php")
    Call<ApiResponse<Community>> createCommunity(@Body RequestModels.CreateCommunityRequest request);

    @GET("community_detail.php")
    Call<ApiResponse<Community>> getCommunityDetail(@Query("id") int communityId);

    @PUT("community_detail.php")
    Call<ApiResponse<Community>> updateCommunity(@Query("id") int communityId,
                                                  @Body RequestModels.UpdateCommunityRequest request);

    // ---- Join community ----
    @POST("join_community.php")
    Call<ApiResponse<Object>> joinCommunity(@Body RequestModels.JoinRequest request);

    @DELETE("join_community.php")
    Call<ApiResponse<Object>> leaveCommunity(@Query("community_id") int communityId,
                                              @Query("user_id") int userId);

    // ---- Members ----
    @GET("members.php")
    Call<ApiResponse<List<Member>>> getMembers(@Query("community_id") int communityId);

    // ---- Gallery ----
    @GET("gallery.php")
    Call<ApiResponse<List<GalleryPhoto>>> getGallery(@Query("community_id") int communityId);

    @POST("gallery.php")
    Call<ApiResponse<Object>> addGalleryPhoto(@Body RequestModels.AddGalleryRequest request);

    // ---- User profile ----
    @GET("users.php")
    Call<ApiResponse<User>> getUser(@Query("id") int userId);

    @PUT("users.php")
    Call<ApiResponse<User>> updateProfile(@Query("id") int userId,
                                           @Body RequestModels.UpdateProfileRequest request);
}
