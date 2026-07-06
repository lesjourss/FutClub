package com.futclub.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.futclub.app.R;
import com.futclub.app.databinding.ActivityCreateCommunityBinding;
import com.futclub.app.model.Community;
import com.futclub.app.model.SportCategory;
import com.futclub.app.network.ApiClient;
import com.futclub.app.network.ApiResponse;
import com.futclub.app.network.RequestModels;
import com.futclub.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * CreateCommunityActivity - form untuk admin membuat komunitas baru.
 * Semua field WAJIB divalidasi dulu sebelum dikirim ke REST API (sesuai syarat UAS poin "Validasi Input").
 */
public class CreateCommunityActivity extends AppCompatActivity {

    private ActivityCreateCommunityBinding binding;
    private SessionManager sessionManager;
    private final List<SportCategory> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateCommunityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        loadCategoriesForSpinner();

        binding.btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                createCommunity();
            }
        });
    }

    private void loadCategoriesForSpinner() {
        ApiClient.getInstance(getString(R.string.base_url)).getApi().getCategories()
                .enqueue(new Callback<ApiResponse<List<SportCategory>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<SportCategory>>> call,
                                            @NonNull Response<ApiResponse<List<SportCategory>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            categoryList.clear();
                            categoryList.addAll(response.body().getData());

                            List<String> names = new ArrayList<>();
                            for (SportCategory c : categoryList) names.add(c.getName());

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    CreateCommunityActivity.this, android.R.layout.simple_spinner_dropdown_item, names);
                            binding.spinnerCategory.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<SportCategory>>> call, @NonNull Throwable t) {
                        Toast.makeText(CreateCommunityActivity.this, "Gagal memuat kategori", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Validasi form sebelum dikirim ke server.
     * @return true kalau semua valid, false kalau ada yang salah (sudah ditampilkan error-nya)
     */
    private boolean validateForm() {
        String name = getText(binding.etName);
        String description = getText(binding.etDescription);
        String whatsappLink = getText(binding.etWhatsappLink);

        if (TextUtils.isEmpty(name) || name.length() < 3) {
            binding.etName.setError("Nama komunitas minimal 3 karakter");
            binding.etName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(description)) {
            binding.etDescription.setError("Deskripsi tidak boleh kosong");
            binding.etDescription.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(whatsappLink)) {
            binding.etWhatsappLink.setError("Link grup WhatsApp wajib diisi");
            binding.etWhatsappLink.requestFocus();
            return false;
        }

        if (!Patterns.WEB_URL.matcher(whatsappLink).matches() || !whatsappLink.contains("chat.whatsapp.com")) {
            binding.etWhatsappLink.setError("Format link WhatsApp tidak valid");
            binding.etWhatsappLink.requestFocus();
            return false;
        }

        if (categoryList.isEmpty()) {
            Toast.makeText(this, "Kategori belum siap, coba lagi sebentar", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createCommunity() {
        SportCategory selectedCategory = categoryList.get(binding.spinnerCategory.getSelectedItemPosition());
        String photoUrl = getText(binding.etPhotoUrl);

        RequestModels.CreateCommunityRequest request = new RequestModels.CreateCommunityRequest(
                sessionManager.getUserId(),
                selectedCategory.getId(),
                getText(binding.etName),
                getText(binding.etDescription),
                TextUtils.isEmpty(photoUrl) ? null : photoUrl,
                getText(binding.etWhatsappLink)
        );

        ApiClient.getInstance(getString(R.string.base_url)).getApi().createCommunity(request)
                .enqueue(new Callback<ApiResponse<Community>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Community>> call, @NonNull Response<ApiResponse<Community>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(CreateCommunityActivity.this, "Komunitas berhasil dibuat!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CreateCommunityActivity.this, MainActivity.class));
                            finish();
                        } else {
                            String msg = response.body() != null ? response.body().getMessage() : "Gagal membuat komunitas";
                            Toast.makeText(CreateCommunityActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Community>> call, @NonNull Throwable t) {
                        Toast.makeText(CreateCommunityActivity.this,
                                "Tidak bisa terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getText(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
