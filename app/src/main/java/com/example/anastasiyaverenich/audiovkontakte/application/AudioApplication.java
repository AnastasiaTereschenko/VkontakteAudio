package com.example.anastasiyaverenich.audiovkontakte.application;

import android.content.Intent;
import android.widget.Toast;

import com.example.anastasiyaverenich.audiovkontakte.activities.LoginActivity;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

public class AudioApplication extends android.app.Application{
    private static AudioApplication instance;
    public static AudioApplication get(){
        return instance;
    }
    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                Toast.makeText(AudioApplication.this, "AccessToken invalidated", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(AudioApplication.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this).withPayments();
    }
}
