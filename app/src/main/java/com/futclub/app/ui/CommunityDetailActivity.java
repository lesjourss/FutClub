package com.futclub.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.futclub.app.R;
import com.futclub.app.adapter.MemberAdapter;
import com.futclub.app.databinding.ActivityCommunityDetailBinding;
import com.futclub.app.model.Community;
import com.futclub.app.model.GalleryPhoto;
import com.futclub.app.model.Member;
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
 * CommunityDetailActivity - halaman detail 1 komunitas.
 * Menampilkan: foto & nama komunitas, deskripsi, tombol join WhatsApp,
 * galeri kegiatan (maks 3 foto), dan daftar anggota (RecyclerView, mirip list grup WhatsApp).
 * Kalau yang login adalah ADMIN pemilik komunitas ini, muncul tombol "Edit".
 */
public class CommunityDetailActivity extends AppCompatActivity {

    private ActivityCommunityDetailBinding binding;
    private SessionManager sessionManager;
    private int communityId;
    private Community currentCommunity;
    private final List<Member> memberList = new ArrayList<>();
    private MemberAdapter memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        communityId = getIntent().getIntExtra("community_id", -1);

        if (communityId == -1) {
            Toast.makeText(this, "Komunitas tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnBack.setOnClickListener(v -> finish());

        setupMemberRecyclerView();
        loadCommunityDetail();
        loadMembers();
    }

    private void setupMemberRecyclerView() {
        memberAdapter = new MemberAdapter(memberList);
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMembers.setAdapter(memberAdapter);
    }

    private void loadCommunityDetail() {
        ApiClient.getInstance(getString(R.string.base_url)).getApi().getCommunityDetail(communityId)
                .enqueue(new Callback<ApiResponse<Community>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Community>> call, @NonNull Response<ApiResponse<Community>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            currentCommunity = response.body().getData();
                            renderCommunity(currentCommunity);
                        } else {
                            Toast.makeText(CommunityDetailActivity.this, "Gagal memuat detail komunitas", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Community>> call, @NonNull Throwable t) {
                        Toast.makeText(CommunityDetailActivity.this,
                                "Tidak bisa terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void renderCommunity(Community community) {
        binding.tvCommunityName.setText(community.getName());
        binding.tvDescription.setText(community.getDescription());
        binding.tvCategoryAndMembers.setText(
                community.getCategoryName() + " · " + community.getMemberCount() + " anggota");

        Glide.with(this)
                .load(community.getPhotoUrl())
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(binding.imgCommunityPhoto);

        // Tombol "Join Grup WhatsApp" cuma membuka link, karena user sudah pasti join dulu sebelum ini muncul
        binding.btnJoinWhatsapp.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(community.getWhatsappLink()));
            startActivity(intent);
        });

        // Tombol "Join Komunitas" - aksi pertama sebelum bisa lihat tombol WA
        binding.btnJoinCommunity.setOnClickListener(v -> joinCommunityRecord());

        // Tampilkan gallery (maksimal 3 foto)
        renderGallery(community.getGallery());

        // Refresh status tombol join, jaga-jaga kalau detail komunitas selesai dimuat duluan
        // sebelum daftar member selesai dimuat (race condition antara 2 API call)
        updateJoinButtonsState();

        // Tombol edit hanya untuk admin pemilik komunitas ini
        if (sessionManager.isAdmin() && sessionManager.getUserId() == community.getAdminId()) {
            binding.tvEditCommunity.setVisibility(android.view.View.VISIBLE);
            binding.tvEditCommunity.setOnClickListener(v -> {
                Intent intent = new Intent(CommunityDetailActivity.this, EditCommunityActivity.class);
                intent.putExtra("community_id", community.getId());
                startActivity(intent);
            });
        }
    }

    private void renderGallery(List<GalleryPhoto> gallery) {
        android.widget.ImageView[] slots = {
                binding.imgGallery1, binding.imgGallery2, binding.imgGallery3
        };

        for (int i = 0; i < slots.length; i++) {
            if (gallery != null && i < gallery.size()) {
                Glide.with(this).load(gallery.get(i).getPhotoUrl()).centerCrop().into(slots[i]);
            }
        }
    }

    private void loadMembers() {
        ApiClient.getInstance(getString(R.string.base_url)).getApi().getMembers(communityId)
                .enqueue(new Callback<ApiResponse<List<Member>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<Member>>> call,
                                            @NonNull Response<ApiResponse<List<Member>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            memberList.clear();
                            memberList.addAll(response.body().getData());
                            memberAdapter.notifyDataSetChanged();
                            updateJoinButtonsState();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<Member>>> call, @NonNull Throwable t) {
                        // Diamkan saja, tidak fatal kalau list member gagal dimuat
                    }
                });
    }

    /**
     * Cek apakah user yang sedang login sudah ada di dalam daftar member komunitas ini.
     * - Kalau BELUM join -> tampilkan tombol "Join Komunitas", sembunyikan tombol WhatsApp
     * - Kalau SUDAH join  -> sembunyikan tombol "Join Komunitas", tampilkan tombol "Join Grup WhatsApp"
     * - Kalau dia admin pemilik komunitas ini -> tidak perlu tombol join sama sekali
     */
    private void updateJoinButtonsState() {
        if (currentCommunity != null && sessionManager.getUserId() == currentCommunity.getAdminId()) {
            binding.btnJoinCommunity.setVisibility(android.view.View.GONE);
            binding.btnJoinWhatsapp.setVisibility(android.view.View.GONE);
            return;
        }

        boolean alreadyJoined = false;
        for (Member member : memberList) {
            if (member.getId() == sessionManager.getUserId()) {
                alreadyJoined = true;
                break;
            }
        }

        binding.btnJoinCommunity.setVisibility(alreadyJoined ? android.view.View.GONE : android.view.View.VISIBLE);
        binding.btnJoinWhatsapp.setVisibility(alreadyJoined ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void joinCommunityRecord() {
        binding.btnJoinCommunity.setEnabled(false);

        RequestModels.JoinRequest request = new RequestModels.JoinRequest(communityId, sessionManager.getUserId());
        ApiClient.getInstance(getString(R.string.base_url)).getApi().joinCommunity(request)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                        binding.btnJoinCommunity.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(CommunityDetailActivity.this, "Berhasil bergabung ke komunitas!", Toast.LENGTH_SHORT).show();
                            // Refresh member count & daftar anggota, tombol otomatis berganti jadi "Join Grup WhatsApp"
                            loadMembers();
                            loadCommunityDetail();
                        } else {
                            String msg = response.body() != null ? response.body().getMessage() : "Gagal join komunitas";
                            Toast.makeText(CommunityDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                        binding.btnJoinCommunity.setEnabled(true);
                        Toast.makeText(CommunityDetailActivity.this,
                                "Tidak bisa terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
