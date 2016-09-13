package com.example.anastasiyaverenich.audiovkontakte.application;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.SeekBar;

import com.example.anastasiyaverenich.audiovkontakte.MediaPlayerControlInterface;
import com.example.anastasiyaverenich.audiovkontakte.MusicService;
import com.example.anastasiyaverenich.audiovkontakte.activities.AudioActivity;
import com.example.anastasiyaverenich.audiovkontakte.modules.Audio;

import java.util.ArrayList;
import java.util.List;

public class MediaPlayerControl {
    Messenger service = null;
    boolean isPlaying;
    int position;
    int duration;
    public boolean musicBound = false;
    boolean isEnd;
    public Intent playIntent;
    boolean isBound;
    ArrayList<MediaPlayerControlInterface> mediaPlayerControlInterfacesArray = new ArrayList<>();
    AudioApplication context;

    private static MediaPlayerControl ourInstance = new MediaPlayerControl();

    public static MediaPlayerControl getInstance() {
        return ourInstance;
    }

    private MediaPlayerControl() {
    }

    final Messenger messenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MusicService.MSG_IS_PLAYING:
                    Bundle dataIsPlaying = msg.getData();
                    isPlaying = dataIsPlaying.getBoolean("valueIsPlaying");
                    break;
                case MusicService.MSG_UPDATE_UI_BY_DISMISS_NOTIFICATION:
                    for (int i = mediaPlayerControlInterfacesArray.size() - 1; i >= 0; i--) {
                        mediaPlayerControlInterfacesArray.get(i).updateUiByDismissNotification();
                    }
                    break;
                case MusicService.MSG_UPDATE_UI_BY_TOGGLE:
                    Bundle dataIsToggle = msg.getData();
                    isPlaying = dataIsToggle.getBoolean("valueIsToggle");
                    //// TODO: 02.09.2016 run updateUiByToggle
                    for (int i = mediaPlayerControlInterfacesArray.size() - 1; i >= 0; i--) {
                        mediaPlayerControlInterfacesArray.get(i).updateUiByToggle(isPlaying);
                    }
                    break;
                case MusicService.MSG_GET_POSITION_AND_DURATION:
                    position = msg.arg1;
                    duration = msg.arg2;
                    //// TODO: 02.09.2016 run setProgress
                    for (int i = mediaPlayerControlInterfacesArray.size() - 1; i >= 0; i--) {
                        mediaPlayerControlInterfacesArray.get(i).setProgress(position, duration);
                    }
                    break;
                case MusicService.MSG_SET_SECONDARY_PROGRESS:
                    int percent = msg.arg1;
                    //// TODO: 02.09.2016 run setSecondProgress
                    for (int i = mediaPlayerControlInterfacesArray.size() - 1; i >= 0; i--) {
                        mediaPlayerControlInterfacesArray.get(i).setSecondProgress(percent);
                    }
                    break;
                case MusicService.MSG_IS_END:
                    int isNextOrPrev = msg.arg1;
                    Bundle dataIsEndAudio = msg.getData();
                    isEnd = dataIsEndAudio.getBoolean("valueIsEndAudio");
                    //// TODO: 02.09.2016 run playNextAudioByComplete
                    for (int i = mediaPlayerControlInterfacesArray.size() - 1; i >= 0; i--) {
                        mediaPlayerControlInterfacesArray.get(i).playNextAudioByComplete(isEnd, isNextOrPrev);
                    }
                    break;
            }
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("TAG", "mmm");
            MediaPlayerControl.this.service = new Messenger(service);
            try {
                Message msg = Message.obtain(null,
                        MusicService.MSG_REGISTER_CLIENT);
                msg.replyTo = messenger;
                MediaPlayerControl.this.service.send(msg);
                msg = Message.obtain(null,
                        MusicService.MSG_IS_PLAYING, 0, 0);
                MediaPlayerControl.this.service.send(msg);

            } catch (RemoteException e) {

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
            service = null;
        }
    };

    public void isDestroyActivity(boolean isDestroy) {
        Bundle dataIsEnd = new Bundle();
        dataIsEnd.putBoolean("valueIsDestroy", isDestroy);
        Message msg = Message.obtain(null, MusicService.MSG_IS_DESTROY, 0, 0, isDestroy);
        msg.setData(dataIsEnd);
        try {
            service.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void subscribeToUpdatesFromSingleton(AudioActivity item) {
        mediaPlayerControlInterfacesArray.add(item);
        if (!mediaPlayerControlInterfacesArray.isEmpty()) {
            doBindService();
        }
    }

    public void doBindService() {
        if (playIntent == null) {
            playIntent = new Intent(AudioApplication.get(), MusicService.class);
        }
        AudioApplication.get().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
       /// isBound = true;
    }

    public void doUnbindService() {
        if (isBound) {
            if (service != null) {
                try {
                    Message msg = Message.obtain(null, MusicService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = messenger;
                    service.send(msg);
                } catch (RemoteException e) {
                }
            }
            AudioApplication.get().unbindService(musicConnection);
            isBound = false;
        }
    }

    public void pauseAudio() {
        try {
            Message msg = Message.obtain(null,
                    MusicService.MSG_PAUSE, 0, 0);
            service.send(msg);
        } catch (RemoteException e) {
        }
    }

    public void updateUiByDismissNotiification(){
        try {
            Message msg = Message.obtain(null,
                    MusicService.MSG_IS_DISMISS_NOTIFICATION, 0, 0);
            service.send(msg);
        } catch (RemoteException e) {
        }
    }

    public void playAudio(Messenger service) {
        try {
            Message msg = Message.obtain(null,
                    MusicService.MSG_PLAY, 0, 0);
            service.send(msg);
        } catch (RemoteException e) {
        }
    }

    public void isToggleMediaPlayer() {
        try {
            Message msg = Message.obtain(null,
                    MusicService.MSG_IS_TOGGLE_MEDIAPLAYER, 0, 0);
            service.send(msg);
        } catch (RemoteException e) {
        }
    }

    public void songPicked(List<Audio.Track> listAudio, int position) {
        String titleAudio = listAudio.get(position).title;
        String artistAudio = listAudio.get(position).artist;
        Message msg = Message.obtain(null,
                MusicService.MSG_SET_URL_NAME_POSITION, 0, 0);
        Bundle bundle = new Bundle();
        bundle.putString("valueUrl", listAudio.get(position).url);
        bundle.putString("titleAudio", titleAudio);
        bundle.putString("artistAudio", artistAudio);
        msg.setData(bundle);
        try {
            service.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        MediaPlayerControl.getInstance().playAudio(service);
    }

    public SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                return;
            }
            long newposition = (duration * progress) / 1000L;
            Message msg = Message.obtain(null, MusicService.MSG_SEEK_TO, Long.valueOf(newposition).intValue(), 0);
            Bundle bundle = new Bundle();
            bundle.putLong("valueNewPosition", newposition);
            msg.setData(bundle);
            try {
                service.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onStopTrackingTouch(SeekBar bar) {
        }
    };

}
