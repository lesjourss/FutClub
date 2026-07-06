package com.futclub.app.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.futclub.app.R;
import com.futclub.app.databinding.ActivityEditCommunityBinding;
import com.futclub.app.model.Community;
import com.futclub.app.network.ApiClient;
import com.futclub.app.network.ApiResponse;
import com.futclub.app.network.RequestModels;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * EditCommunityActivity - khusus admin pemilik komunitas.
 * Bisa mengubah nama, deskripsi, foto, link WhatsApp, dan menambah foto galeri (maks 3).
 */
public class EditCommunityActivity extends AppCompatActivity {

    private ActivityEditCommunityBinding binding;
    private int communityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditCommunityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        communityId = getIntent().getIntExtra("community_id", -1);
        if (communityId == -1) {
            finish();
            return;
        }

        loadCurrentData();

        binding.btnSave.setOnClickListener(v -> {
            if (validateForm()) saveChanges();
        });

        binding.btnAddGallery.setOnClickListener(v -> addGalleryPhoto());
    }

    private void loadCurrentData() {
        ApiClient.getInstance(getString(R.string.base_url)).getApi().getCommunityDetail(communityId)
                .enqueue(new Callback<ApiResponse<Community>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Community>> call, @NonNull Response<ApiResponse<Community>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Community c = response.body().getData();
                            binding.etName.setText(c.getName());
                            binding.etDescription.setText(c.getDescription());
                            binding.etPhotoUrl.setText(c.getPhotoUrl());
                            binding.etWhatsappLink.setText(c.getWhatsappLink());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Community>> call, @NonNull Throwable t) {
                        Toast.makeText(EditCommunityActivity.this, "Gagal memuat data komunitas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateForm() {
        String name = getText(binding.etName);
        String description = getText(binding.etDescription);
        String whatsappLink = getText(binding.etWhatsappLink);

        if (TextUtils.isEmpty(name) || name.length() < 3) {
            binding.etName.setError("Nama komunitas minimal 3 karakter");
            return false;
        }
        if (TextUtils.isEmpty(description)) {
            binding.etDescription.setError("Deskripsi tidak boleh kosong");
            return false;
        }
        if (TextUtils.isEmpty(whatsappLink) || !Patterns.WEB_URL.matcher(whatsappLink).matches()
                || !whatsappLink.contains("chat.whatsapp.com")) {
            binding.etWhatsappLink.setError("Format link WhatsApp tidak valid");
            return false;
        }
        return true;
    }

    private void saveChanges() {
        RequestModels.UpdateCommunityRequest request = new RequestModels.UpdateCommunityRequest();
        request.name = getText(binding.etName);
        request.description = getText(binding.etDescription);
        request.photoUrl = getText(binding.etPhotoUrl);
        request.whatsappLink = getText(binding.etWhatsappLink);

        ApiClient.getInstance(getString(R.string.base_url)).getApi().updateCommunity(communityId, request)
                .enqueue(new Callback<ApiResponse<Community>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Community>> call, @NonNull Response<ApiResponse<Community>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(EditCommunityActivity.this, "Perubahan tersimpan", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditCommunityActivity.this, "Gagal menyimpan perubahan", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Community>> call, @NonNull Throwable t) {
                        Toast.makeText(EditCommunityActivity.this,
                                "Tidak bisa terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addGalleryPhoto() {
        String photoUrl = getText(binding.etGalleryPhotoUrl);
        if (TextUtils.isEmpty(photoUrl) || !Patterns.WEB_URL.matcher(photoUrl).matches()) {
            binding.etGalleryPhotoUrl.setError("Masukkan URL foto yang valid");
            return;
        }

        RequestModels.AddGalleryRequest request = new RequestModels.AddGalleryRequest(communityId, photoUrl);
        ApiClient.getInstance(getString(R.string.base_url)).getApi().addGalleryPhoto(request)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(EditCommunityActivity.this, "Foto galeri ditambahkan", Toast.LENGTH_SHORT).show();
                            binding.etGalleryPhotoUrl.setText("");
                        } else {
                            // Termasuk pesan error "Gallery sudah mencapai batas maksimal 3 foto" dari backend
                            String msg = response.body() != null ? response.body().getMessage() : "Gagal menambah foto";
                            Toast.makeText(EditCommunityActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                        Toast.makeText(EditCommunityActivity.this,
                                "Tidak bisa terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getText(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
