package com.example.finstatest.api;

/**
 * Singleton class that provides a single instance of the ApiService.
 * This prevents redundant Retrofit object creation across the app.
 */
public class ApiServiceInstance {
    private static final String BASE_URL = "http://10.0.2.2:3000/";
    private static ApiService apiService;
    public static ApiService getService() {
        if (apiService == null) {
            apiService = RetrofitClient.getClient(BASE_URL).create(ApiService.class);
        }
        return apiService;
    }
}
