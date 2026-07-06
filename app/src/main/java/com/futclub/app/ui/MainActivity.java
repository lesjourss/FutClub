package com.futclub.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.futclub.app.R;
import com.futclub.app.adapter.CommunityAdapter;
import com.futclub.app.databinding.ActivityMainBinding;
import com.futclub.app.model.Community;
import com.futclub.app.model.SportCategory;
import com.futclub.app.network.ApiClient;
import com.futclub.app.network.ApiResponse;
import com.futclub.app.util.SessionManager;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MainActivity - halaman Home utama.
 * Menampilkan:
 * - Header dengan logo & foto profil (klik -> ProfileActivity)
 * - Filter kategori olahraga (chip, sesuai kategori yang dipilih user saat onboarding)
 * - RecyclerView daftar komunitas sesuai kategori yang di-filter
 * - Bottom navigation glass (Home, Komunitasku, Profil)
 * - FAB "+" untuk admin membuat komunitas baru
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessionManager;
    private CommunityAdapter adapter;
    private final List<Community> communityList = new ArrayList<>();
    private final List<SportCategory> userCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        setupRecyclerView();
        setupHeader();
        setupBottomNav();
        setupFab();

        binding.swipeRefresh.setOnRefreshListener(this::loadCommunities);

        loadUserCategoriesForFilter();
        loadCommunities();
    }

    private void setupRecyclerView() {
        adapter = new CommunityAdapter(communityList, community -> {
            Intent intent = new Intent(MainActivity.this, CommunityDetailActivity.class);
            intent.putExtra("community_id", community.getId());
            startActivity(intent);
        });
        binding.rvCommunities.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCommunities.setAdapter(adapter);
    }

    private void setupHeader() {
        Glide.with(this)
                .load(sessionManager.getPhotoUrl())
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(binding.imgProfile);

        binding.imgProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
    }

    private void setupBottomNav() {
        binding.bottomNav.setSelectedItemId(R.id.nav_home);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true; // sudah di Home
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_communities) {
                // "Komunitasku": untuk simpel, filter ke komunitas yang dibuat sendiri kalau admin
                Toast.makeText(this, "Menampilkan komunitas yang kamu ikuti", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void setupFab() {
        if (sessionManager.isAdmin()) {
            binding.fabAddCommunity.setVisibility(android.view.View.VISIBLE);
            binding.fabAddCommunity.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, CreateCommunityActivity.class)));
        }
    }

    /** Ambil kategori yang dipilih user saat onboarding, dipakai sebagai filter chip di Home */
    private void loadUserCategoriesForFilter() {
        ApiClient.getInstance(getString(R.string.base_url)).getApi()
                .getUserCategories(sessionManager.getUserId())
                .enqueue(new Callback<ApiResponse<List<SportCategory>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<SportCategory>>> call,
                                            @NonNull Response<ApiResponse<List<SportCategory>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            userCategories.clear();
                            userCategories.addAll(response.body().getData());
                            renderFilterChips();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<SportCategory>>> call, @NonNull Throwable t) {
                        // Filter gagal load bukan error fatal, list komunitas tetap bisa tampil semua
                    }
                });
    }

    private void renderFilterChips() {
        binding.chipGroupFilter.removeAllViews();

        Chip chipAll = new Chip(this);
        chipAll.setText("Semua");
        chipAll.setCheckable(true);
        chipAll.setChecked(true);
        chipAll.setChipBackgroundColorResource(R.color.green_light);
        binding.chipGroupFilter.addView(chipAll);
        chipAll.setOnClickListener(v -> loadCommunities());

        for (SportCategory category : userCategories) {
            Chip chip = new Chip(this);
            chip.setText(category.getName());
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.green_light);
            chip.setOnClickListener(v -> loadCommunitiesByCategory(category.getId()));
            binding.chipGroupFilter.addView(chip);
        }
    }

    private void loadCommunities() {
        binding.swipeRefresh.setRefreshing(true);
        ApiClient.getInstance(getString(R.string.base_url)).getApi().getAllCommunities()
                .enqueue(getCommunitiesCallback());
    }

    private void loadCommunitiesByCategory(int categoryId) {
        binding.swipeRefresh.setRefreshing(true);
        ApiClient.getInstance(getString(R.string.base_url)).getApi()
                .getCommunities(String.valueOf(categoryId))
                .enqueue(getCommunitiesCallback());
    }

    private Callback<ApiResponse<List<Community>>> getCommunitiesCallback() {
        return new Callback<ApiResponse<List<Community>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Community>>> call,
                                    @NonNull Response<ApiResponse<List<Community>>> response) {
                binding.swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    communityList.clear();
                    communityList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                    binding.tvEmptyState.setVisibility(communityList.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                } else {
                    Toast.makeText(MainActivity.this, "Gagal memuat komunitas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Community>>> call, @NonNull Throwable t) {
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(MainActivity.this,
                        "Tidak bisa terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }
}
