package com.example.multisporttrainer.api;

import com.example.multisporttrainer.models.AuthResponse;
import com.example.multisporttrainer.models.GenerateRouteRequest;
import com.example.multisporttrainer.models.GenerateRouteResponse;
import com.example.multisporttrainer.models.HistoryResponse;
import com.example.multisporttrainer.models.LatestResultResponse;
import com.example.multisporttrainer.models.LeaderboardResponse;
import com.example.multisporttrainer.models.LoginRequest;
import com.example.multisporttrainer.models.RegisterRequest;
import com.example.multisporttrainer.models.SaveResultRequest;
import com.example.multisporttrainer.models.SaveResultResponse;
import com.example.multisporttrainer.models.SaveRouteRequest;
import com.example.multisporttrainer.models.SaveRouteResponse;
import com.example.multisporttrainer.models.StartTrainingRequest;
import com.example.multisporttrainer.models.StartTrainingResponse;
import com.example.multisporttrainer.models.StatisticsResponse;
import com.example.multisporttrainer.models.UpdateUserRequest;
import com.example.multisporttrainer.models.UserProfileResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @POST("api/Auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/Auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @GET("api/Users/{userId}")
    Call<UserProfileResponse> getUserProfile(@Path("userId") int userId);

    @PUT("api/Users/{userId}")
    Call<UserProfileResponse> updateUserProfile(
            @Path("userId") int userId,
            @Body UpdateUserRequest request
    );

    @GET("api/History/{userId}")
    Call<List<HistoryResponse>> getHistory(@Path("userId") int userId);

    @GET("api/Statistics/{userId}")
    Call<StatisticsResponse> getStatistics(@Path("userId") int userId);

    @GET("api/Statistics/latest/{userId}")
    Call<LatestResultResponse> getLatestResult(@Path("userId") int userId);

    @GET("api/Leaderboard")
    Call<List<LeaderboardResponse>> getLeaderboard();

    @POST("api/Training/start")
    Call<StartTrainingResponse> startTraining(@Body StartTrainingRequest request);

    @POST("api/Routes/save")
    Call<SaveRouteResponse> saveRoute(@Body SaveRouteRequest request);

    @POST("api/Routes/generate")
    Call<GenerateRouteResponse> generateRoute(@Body GenerateRouteRequest request);

    @POST("api/Results/save")
    Call<SaveResultResponse> saveResult(@Body SaveResultRequest request);
}