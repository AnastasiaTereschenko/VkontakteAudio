package com.example.anastasiyaverenich.audiovkontakte;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.anastasiyaverenich.audiovkontakte.application.MediaPlayerControl;


public class NotificationDismissedReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("onReceive", "onReceive");
        MediaPlayerControl.getInstance().pauseAudio();
        MediaPlayerControl.getInstance().updateUiByDismissNotiification();
    }
}
