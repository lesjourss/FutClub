package com.futclub.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.futclub.app.R;
import com.futclub.app.databinding.ActivityRoleSelectionBinding;
import com.futclub.app.model.User;
import com.futclub.app.network.ApiClient;
import com.futclub.app.network.ApiResponse;
import com.futclub.app.network.RequestModels;
import com.futclub.app.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * RoleSelectionActivity - user memilih jadi "Admin Komunitas" atau "Olahragawan".
 * Pilihan ini dikirim ke backend (PUT set_role.php) supaya tersimpan permanen di database.
 *
 * - Kalau pilih Admin -> diarahkan ke CreateCommunityActivity untuk isi data komunitas
 * - Kalau pilih Olahragawan -> langsung diarahkan ke MainActivity (Home)
 */
public class RoleSelectionActivity extends AppCompatActivity {

    private ActivityRoleSelectionBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoleSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        binding.cardOlahragawan.setOnClickListener(v -> updateRole("user"));
        binding.cardAdmin.setOnClickListener(v -> updateRole("admin"));
    }

    private void updateRole(String role) {
        RequestModels.SetRoleRequest request = new RequestModels.SetRoleRequest(sessionManager.getUserId(), role);

        ApiClient.getInstance(getString(R.string.base_url)).getApi().setRole(request)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            sessionManager.updateRole(role);

                            Intent intent;
                            if ("admin".equals(role)) {
                                intent = new Intent(RoleSelectionActivity.this, CreateCommunityActivity.class);
                            } else {
                                intent = new Intent(RoleSelectionActivity.this, MainActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RoleSelectionActivity.this, "Gagal menyimpan pilihan role", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                        Toast.makeText(RoleSelectionActivity.this,
                                "Tidak bisa terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
