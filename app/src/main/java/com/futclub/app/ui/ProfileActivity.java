package com.futclub.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.futclub.app.R;
import com.futclub.app.databinding.ActivityProfileBinding;
import com.futclub.app.model.User;
import com.futclub.app.network.ApiClient;
import com.futclub.app.network.ApiResponse;
import com.futclub.app.network.RequestModels;
import com.futclub.app.util.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ProfileActivity - user (baik role admin maupun olahragawan) bisa lihat & edit profil di sini.
 * Yang bisa diubah: nama & foto profil. Email tidak bisa diubah (mengikuti akun Google).
 */
public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());

        renderProfile();

        binding.btnSave.setOnClickListener(v -> {
            if (validateForm()) saveProfile();
        });

        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void renderProfile() {
        binding.etName.setText(sessionManager.getName());
        binding.etEmail.setText(sessionManager.getEmail());
        binding.tvRoleBadge.setText(sessionManager.isAdmin() ? "Admin Komunitas" : "Olahragawan");

        Glide.with(this)
                .load(sessionManager.getPhotoUrl())
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(binding.imgProfilePhoto);
    }

    private boolean validateForm() {
        String name = getText(binding.etName);
        if (TextUtils.isEmpty(name) || name.length() < 3) {
            binding.etName.setError("Nama minimal 3 karakter");
            return false;
        }
        return true;
    }

    private void saveProfile() {
        RequestModels.UpdateProfileRequest request = new RequestModels.UpdateProfileRequest();
        request.name = getText(binding.etName);
        String newPhoto = getText(binding.etPhotoUrl);
        request.photoUrl = TextUtils.isEmpty(newPhoto) ? sessionManager.getPhotoUrl() : newPhoto;

        ApiClient.getInstance(getString(R.string.base_url)).getApi()
                .updateProfile(sessionManager.getUserId(), request)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            User updated = response.body().getData();
                            sessionManager.updateProfile(updated.getName(), updated.getPhotoUrl());
                            Toast.makeText(ProfileActivity.this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                            renderProfile();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                        Toast.makeText(ProfileActivity.this,
                                "Tidak bisa terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        sessionManager.logout();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getText(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
