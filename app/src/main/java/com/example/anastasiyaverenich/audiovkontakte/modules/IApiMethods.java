package com.example.anastasiyaverenich.audiovkontakte.modules;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface IApiMethods {
    @GET("/method/audio.get")
    void getAudio(
            @Query("owner_id") int owner,
            @Query("offset") int offset,
            @Query("count") int count,
            @Query("version") String version,
            @Query("access_token") String access_token,
            Callback<Audio> cb
    );
}
