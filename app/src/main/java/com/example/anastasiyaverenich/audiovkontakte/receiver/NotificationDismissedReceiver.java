package com.example.anastasiyaverenich.audiovkontakte.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.anastasiyaverenich.audiovkontakte.singleton.MediaPlayerControl;


public class NotificationDismissedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("onReceive", "onReceive");
        //MediaPlayerControl.getInstance().pauseAudio();
        MediaPlayerControl.getInstance().dismissNotificationOrEndOfList();
    }
}
