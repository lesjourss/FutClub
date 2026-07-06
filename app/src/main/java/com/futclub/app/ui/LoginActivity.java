package com.futclub.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.futclub.app.R;
import com.futclub.app.databinding.ActivityLoginBinding;
import com.futclub.app.model.User;
import com.futclub.app.network.ApiClient;
import com.futclub.app.network.ApiResponse;
import com.futclub.app.network.RequestModels;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LoginActivity - halaman pertama yang muncul saat buka app.
 * Alurnya:
 * 1. User tap tombol "Masuk dengan Google"
 * 2. Muncul dialog pilih akun Google (disediakan oleh Google Sign-In SDK)
 * 3. Hasil login Google dipakai untuk login ke Firebase Auth
 * 4. Kalau berhasil, data user (nama, email, foto) dikirim ke backend PHP kita (auth.php)
 *    supaya tersimpan/tercocokkan di database MySQL
 * 5. Kalau user BARU -> arahkan ke CategorySelectionActivity
 *    Kalau user LAMA (sudah pernah pilih kategori & role) -> langsung ke MainActivity
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        // Konfigurasi Google Sign-In: minta id token supaya bisa dipakai login ke Firebase
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Launcher untuk menangkap hasil dari activity pilih akun Google
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (data == null) {
                        showLoading(false);
                        return;
                    }
                    try {
                        GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                                .getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        showLoading(false);
                        Toast.makeText(this, "Login Google gagal: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                    }
                });

        binding.btnGoogleSignIn.setOnClickListener(v -> {
            showLoading(true);
            googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    registerToBackend(firebaseUser);
                }
            } else {
                showLoading(false);
                Toast.makeText(this, "Autentikasi Firebase gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Kirim data user hasil login Google ke backend PHP kita (auth.php),
     * supaya user tersimpan di database MySQL dan kita dapat user_id untuk dipakai
     * di seluruh aplikasi (create komunitas, join komunitas, dll)
     */
    private void registerToBackend(FirebaseUser firebaseUser) {
        String photoUrl = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null;

        RequestModels.AuthRequest request = new RequestModels.AuthRequest(
                firebaseUser.getUid(),
                firebaseUser.getDisplayName(),
                firebaseUser.getEmail(),
                photoUrl
        );

        ApiClient.getInstance(getString(R.string.base_url)).getApi().login(request)
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            User user = response.body().getData();

                            new com.futclub.app.util.SessionManager(LoginActivity.this).saveUser(
                                    user.getId(), user.getName(), user.getEmail(), user.getPhotoUrl(), user.getRole()
                            );

                            // Lanjut ke pemilihan kategori olahraga
                            startActivity(new Intent(LoginActivity.this, CategorySelectionActivity.class));
                            finish();
                        } else {
                            String msg = response.body() != null ? response.body().getMessage() : "Login gagal";
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this,
                                "Tidak bisa terhubung ke server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnGoogleSignIn.setEnabled(!loading);
    }
}
