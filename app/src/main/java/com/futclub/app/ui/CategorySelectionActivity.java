package com.futclub.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.futclub.app.R;
import com.futclub.app.databinding.ActivityCategorySelectionBinding;
import com.futclub.app.model.SportCategory;
import com.futclub.app.network.ApiClient;
import com.futclub.app.network.ApiResponse;
import com.futclub.app.network.RequestModels;
import com.futclub.app.util.SessionManager;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * CategorySelectionActivity - user pilih kategori olahraga yang diminati (multi-select).
 * Kategori diambil dari backend (GET categories.php), ditampilkan sebagai Chip yang bisa dicentang.
 */
public class CategorySelectionActivity extends AppCompatActivity {

    private ActivityCategorySelectionBinding binding;
    private SessionManager sessionManager;
    private final List<SportCategory> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategorySelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        loadCategories();

        binding.btnContinue.setOnClickListener(v -> saveSelectedCategories());
    }

    private void loadCategories() {
        ApiClient.getInstance(getString(R.string.base_url)).getApi().getCategories()
                .enqueue(new Callback<ApiResponse<List<SportCategory>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<SportCategory>>> call,
                                            @NonNull Response<ApiResponse<List<SportCategory>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            categoryList.clear();
                            categoryList.addAll(response.body().getData());
                            renderChips();
                        } else {
                            Toast.makeText(CategorySelectionActivity.this, "Gagal memuat kategori", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<SportCategory>>> call, @NonNull Throwable t) {
                        Toast.makeText(CategorySelectionActivity.this,
                                "Tidak bisa terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void renderChips() {
        binding.chipGroupCategories.removeAllViews();
        for (SportCategory category : categoryList) {
            Chip chip = new Chip(this);
            chip.setText(category.getName());
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setChipBackgroundColorResource(R.color.green_light);
            chip.setTextColor(getResources().getColor(R.color.text_primary));
            binding.chipGroupCategories.addView(chip);
        }
    }

    private void saveSelectedCategories() {
        List<Integer> selectedIds = new ArrayList<>();

        for (int i = 0; i < binding.chipGroupCategories.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupCategories.getChildAt(i);
            if (chip.isChecked()) {
                selectedIds.add(categoryList.get(i).getId());
            }
        }

        // ---- Validasi input: minimal 1 kategori harus dipilih ----
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Pilih minimal 1 kategori olahraga dulu ya", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestModels.UserCategoriesRequest request =
                new RequestModels.UserCategoriesRequest(sessionManager.getUserId(), selectedIds);

        ApiClient.getInstance(getString(R.string.base_url)).getApi().saveUserCategories(request)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            // Lanjut ke pemilihan role (admin atau olahragawan)
                            startActivity(new Intent(CategorySelectionActivity.this, RoleSelectionActivity.class));
                            finish();
                        } else {
                            Toast.makeText(CategorySelectionActivity.this, "Gagal menyimpan kategori", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                        Toast.makeText(CategorySelectionActivity.this,
                                "Tidak bisa terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
