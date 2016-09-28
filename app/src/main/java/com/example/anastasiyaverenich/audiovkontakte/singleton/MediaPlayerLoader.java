package com.example.anastasiyaverenich.audiovkontakte.singleton;

import android.util.Log;

import com.example.anastasiyaverenich.audiovkontakte.activities.LoginActivity;
import com.example.anastasiyaverenich.audiovkontakte.gsonFactories.RecipeTypeAdapterFactory;
import com.example.anastasiyaverenich.audiovkontakte.modules.Audio;
import com.example.anastasiyaverenich.audiovkontakte.modules.IApiMethods;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VKList;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class MediaPlayerLoader {
    public static final String API_URL = "https://api.vk.com";
    public static int OWNER_ID = 113577371;
    public static int OFFSET = 0;
    public static final int COUNT = 15;
    public static final String VERSION = "5.53";
    public static final String ACCESS_TOKEN = "50c73e07eab2d57a19d4bae6db74df59d9e5283cc9051157ac851784180b7a6fec810ab0d0975f6b9743a";
    private IApiMethods methods;
    private Callback<Audio> callback;
    public final List<Audio.Track> listAudio = new ArrayList<>();
    private List<MediaPlayerLoaderCallback> callbacks = new ArrayList<>();
    private static MediaPlayerLoader ourInstance = new MediaPlayerLoader();

    public static MediaPlayerLoader getInstance() {
        return ourInstance;
    }

    private MediaPlayerLoader() {
    }

    public void getResponse(int count, int offset) {
        VKParameters params = new VKParameters();
        params.put(VKApiConst.OFFSET, offset);
        params.put(VKApiConst.COUNT, count);
        VKRequest requestAudio = VKApi.audio().get(params);
        requestAudio.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Log.d("TAG", "Complete");
                int size = ((VKList<VKApiAudio>) response.parsedModel).size();
                Log.d("TAG", "" + size);
                //listAudio.clear();
                for (int i = 0; i < size; i++) {
                    VKApiAudio vkApiAudio = ((VKList<VKApiAudio>) response.parsedModel).get(i);
                    listAudio.add(new Audio.Track(vkApiAudio.artist, vkApiAudio.title, vkApiAudio.duration, vkApiAudio.url));
                }
                for (int i = 0; i < callbacks.size(); i++) {
                    Log.d("MediaPlayerLoader", String.valueOf(callbacks.size()));
                    callbacks.get(i).onSuccess(listAudio);
                }
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                Log.d("VkDemoApp", "attemptFailed " + request + " " + attemptNumber + " " + totalAttempts);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Log.d("VkDemoApp", "onError: " + error);
                for (int i = 0; i < callbacks.size(); i++) {
                    callbacks.get(i).onError();
                }
            }

            @Override
            public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
                super.onProgress(progressType, bytesLoaded, bytesTotal);
                Log.d("VkDemoApp", "onProgress " + progressType + " " + bytesLoaded + " " + bytesTotal);
            }
        });
    }

    public void loadingAudio() {
        Gson gson = new GsonBuilder().
                registerTypeAdapterFactory(new RecipeTypeAdapterFactory()).create();
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setConverter(new GsonConverter(gson))
                .build();
        methods = restAdapter.create(IApiMethods.class);
        callback = new Callback<Audio>() {
            @Override
            public void success(Audio results, Response response) {
                Log.e("TAG", "SUCCESS " + results.response.size());
                listAudio.addAll(results.response);
                for (int i = 0; i < callbacks.size(); i++) {
                    Log.d("MediaPlayerLoader", String.valueOf(callbacks.size()));
                    callbacks.get(i).onSuccess(listAudio);
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e("TAG", "ERROR ");
                retrofitError.printStackTrace();
                for (int i = 0; i < callbacks.size(); i++) {
                    callbacks.get(i).onError();
                }
            }
        };
        methods.getAudio(OWNER_ID, OFFSET, COUNT, VERSION, ACCESS_TOKEN, callback);
    }

    public List<Audio.Track> getListAudio() {
        return listAudio;
    }

    public void loadMore() {
        OFFSET = OFFSET + COUNT;
        if (LoginActivity.useAudioVkontakteWithSdk)
            getResponse(COUNT, OFFSET);
        else {
            //loadingAudio(listAudio,adapter);
            methods.getAudio(OWNER_ID, OFFSET, COUNT, VERSION, ACCESS_TOKEN, callback);
        }
    }

    public void addCallback(MediaPlayerLoaderCallback mediaPLayerLoaderCallback) {
        callbacks.add(mediaPLayerLoaderCallback);
    }

    public void removeCallback(MediaPlayerLoaderCallback mediaPLayerLoaderCallback) {
        callbacks.remove(mediaPLayerLoaderCallback);
    }

    public interface MediaPlayerLoaderCallback {
        void onSuccess(List<Audio.Track> audioList);

        void onError();
    }
}
