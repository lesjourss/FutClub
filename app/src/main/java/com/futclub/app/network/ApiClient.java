package com.futclub.app.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton class untuk membuat instance Retrofit sekali saja lalu dipakai berulang.
 * Cara pakai di Activity:
 *   ApiService api = ApiClient.getInstance(context).getApi();
 *   api.getCategories().enqueue(new Callback<...>() { ... });
 */
public class ApiClient {

    private static ApiClient instance;
    private final ApiService apiService;

    private ApiClient(String baseUrl) {
        // Logging interceptor supaya bisa lihat request/response di Logcat (tag: OkHttp), berguna buat debugging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized ApiClient getInstance(String baseUrl) {
        if (instance == null) {
            instance = new ApiClient(baseUrl);
        }
        return instance;
    }

    public ApiService getApi() {
        return apiService;
    }
}
