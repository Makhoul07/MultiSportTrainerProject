package com.example.multisporttrainer.api;

import android.content.Context;

import com.example.multisporttrainer.SessionPreferences;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.206.240.14:5" +
            "062/";

    private static Retrofit retrofit;
    private static Context appContext;

    /**
     * Capture the application context once (from {@code MultiSportTrainerApp}) so the
     * auth interceptor can read the persisted JWT token without a per-call context.
     */
    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Retrofit getInstance() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();

                        String token = appContext == null
                                ? ""
                                : SessionPreferences.getToken(appContext);

                        if (token == null || token.isEmpty()) {
                            return chain.proceed(original);
                        }

                        Request authed = original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();

                        return chain.proceed(authed);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
