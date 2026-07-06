package com.futclub.app.network;

import com.google.gson.annotations.SerializedName;

/**
 * Semua response dari backend PHP kita punya format yang sama:
 * { "success": true/false, "message": "...", "data": ... }
 * Class generic ini dipakai supaya Retrofit bisa langsung parse ke bentuk ini,
 * apapun tipe "data"-nya (User, List<Community>, dll) -> pakai <T>
 */
public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
