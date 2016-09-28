package com.example.anastasiyaverenich.audiovkontakte.singleton;

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

import com.example.anastasiyaverenich.audiovkontakte.application.AudioApplication;
import com.example.anastasiyaverenich.audiovkontakte.interfaces.MediaPlayerControlInterface;
import com.example.anastasiyaverenich.audiovkontakte.modules.Audio;
import com.example.anastasiyaverenich.audiovkontakte.modules.MediaPlayerState;
import com.example.anastasiyaverenich.audiovkontakte.service.MusicService;

import java.util.ArrayList;
import java.util.List;

public class MediaPlayerControl {
    public static int INIT_POSITION = 0;
    Messenger service = null;
    public boolean musicBound = false;
    boolean isBound;
    ArrayList<MediaPlayerControlInterface> mediaPlayerControlInterfacesArray = new ArrayList<>();
    AudioApplication context;
    public int position;
    public boolean isPlaying;
    private static MediaPlayerControl ourInstance = new MediaPlayerControl();
    List<Audio.Track> listAudio;
    int pressedPosition;
    int isNextOrPrev;
    int nextPosition = 0;
    MediaPlayerLoader mediaPlayerLoader;
    Intent playIntent;
    boolean isLoadData;
    int currentPosition = -1;
    boolean isEnd;
    int currentSize;
    public int positionSeekbar;
    public int duration;
    private MediaPlayerState mediaPlayerState;

    public static MediaPlayerControl getInstance() {
        return ourInstance;
    }

    private MediaPlayerControl() {
        mediaPlayerState = new MediaPlayerState();
        listAudio = MediaPlayerLoader.getInstance().getListAudio();
        mediaPlayerLoader = MediaPlayerLoader.getInstance();
        mediaPlayerLoader.addCallback(mediaPlayerLoaderCallback);
    }

    final Messenger messenger = new Messenger(new MediaPlayerControlHandler());

    class MediaPlayerControlHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
               /* case MusicService.MSG_UPDATE_UI_BY_DISMISS_NOTIFICATION_AND_END_OF_LIST:
                    //pauseAudio();
                    break;*/
                case MusicService.MSG_UPDATE_UI_BY_TOGGLE:
                    Bundle dataIsToggle = msg.getData();
                    isPlaying = dataIsToggle.getBoolean("valueIsToggle");
                    for (int i = mediaPlayerControlInterfacesArray.size() - 1; i >= 0; i--) {
                        mediaPlayerControlInterfacesArray.get(i).updateUiByToggle(isPlaying);
                    }
                    mediaPlayerState.setIsPlaying(isPlaying);
                    break;
                case MusicService.MSG_GET_POSITION_AND_DURATION:
                    positionSeekbar = msg.arg1;
                    duration = msg.arg2;
                    for (int i = mediaPlayerControlInterfacesArray.size() - 1; i >= 0; i--) {
                        mediaPlayerControlInterfacesArray.get(i).setProgress(positionSeekbar, duration);
                    }
                    mediaPlayerState.setPositionSeekBar(positionSeekbar);
                    break;
                case MusicService.MSG_SET_SECONDARY_PROGRESS:
                    int percent = msg.arg1;
                    for (int i = mediaPlayerControlInterfacesArray.size() - 1; i >= 0; i--) {
                        mediaPlayerControlInterfacesArray.get(i).setSecondProgress(percent);
                    }
                    break;
                case MusicService.MSG_IS_END:
                    isNextOrPrev = msg.arg1;
                    Bundle dataIsEndAudio = msg.getData();
                    isEnd = dataIsEndAudio.getBoolean("valueIsEndAudio");
                    playNextOrPrevAudio(isNextOrPrev);
                    for (int i = mediaPlayerControlInterfacesArray.size() - 1; i >= 0; i--) {
                        mediaPlayerControlInterfacesArray.get(i).updateUiByComplete(isEnd, isNextOrPrev);
                    }
                    break;
            }
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("TAG", "Connected");
            MediaPlayerControl.this.service = new Messenger(service);
            try {
                Message msg = Message.obtain(null,
                        MusicService.MSG_REGISTER_CLIENT);
                msg.replyTo = messenger;
                MediaPlayerControl.this.service.send(msg);


            } catch (RemoteException e) {

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //musicBound = false;
            Log.e("TAG", "Disconnected");
            service = null;
        }
    };

    private MediaPlayerLoader.MediaPlayerLoaderCallback mediaPlayerLoaderCallback
            = new MediaPlayerLoader.MediaPlayerLoaderCallback() {
        @Override
        public void onSuccess(List<Audio.Track> audioList) {
            Log.d("AudioActivity", "OnSuccess");
            if (isLoadData) {
                if (listAudio.size() == currentSize) {
                    updatePosition(INIT_POSITION);
                    updateCurrentPosition(INIT_POSITION);
                    pressedPosition = 0;
                    nextPosition = 0;
                }
                selectAudio();
                for (int i = mediaPlayerControlInterfacesArray.size() - 1; i >= 0; i--) {
                    mediaPlayerControlInterfacesArray.get(i).setNameAndArtistAudio(listAudio, position);
                }
                isLoadData = false;
            }
        }

        @Override
        public void onError() {
            //listAudio.remove(listAudio.size() - 1);

        }
    };

    public void terminateStateService() {
        mediaPlayerState.setIsSetAudio(false);
        Log.d("terminateState", "stop");
        try {
            Message msg = Message.obtain(null,
                    MusicService.MSG_IS_TERMINATE_STATE, 0, 0);
            service.send(msg);
        } catch (RemoteException e) {
        }
    }

    public void subscribeToUpdatesFromMediaPlayerControl(MediaPlayerControlInterface item) {
        mediaPlayerControlInterfacesArray.add(item);
        if (!mediaPlayerControlInterfacesArray.isEmpty()) {
            doBindService();
        }
    }


    public void unsubscribeToUpdatesFromMediaPlayerControl(MediaPlayerControlInterface item) {
        mediaPlayerControlInterfacesArray.remove(item);
    }

    public void doBindService() {
        if (playIntent == null) {
            playIntent = new Intent(AudioApplication.get(), MusicService.class);
        }
        AudioApplication.get().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        isBound = true;
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

    public void dismissNotificationOrEndOfList() {
        terminateStateService();
        for (int i = mediaPlayerControlInterfacesArray.size() - 1; i >= 0; i--) {
            mediaPlayerControlInterfacesArray.get(i).updateUiByDismissNotification();
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

    public void setAndPlayAudio() {
       mediaPlayerState.setIsSetAudio(true);
        nextPosition = 0;
        pressedPosition = position;
        if (currentPosition == position) {
            isToggleMediaPlayer();
        } else {
            selectAudio();
        }
        updateCurrentPosition(position);
    }

    public int playNextOrPrevAudio(int isNextOrPrev) {
        Log.d("TAG", "playNextOrPrevAudio");
        if (nextPosition == 0) {
            nextPosition = pressedPosition;
        }
        if (isNextOrPrev == 1) {
            nextPosition = nextPosition + 1;
        } else {
            nextPosition = nextPosition - 1;
        }
        updatePosition(nextPosition);
        Log.d("TAG", String.valueOf(nextPosition));
        selectAudio();
        return nextPosition;
    }

    public void updatePosition(int newPosition) {
        position = newPosition;
        mediaPlayerState.setPosition(newPosition);
    }

    public void updateCurrentPosition(int newCurrentPosition) {
        currentPosition = newCurrentPosition;

    }

    public int getCurrentPosition() {
        Log.d("getPosition", String.valueOf(position));
        return position;
    }

    public void selectAudio() {
        mediaPlayerState.setIsPlaying(true);
        if (listAudio.size() <= position) {
            if (isLoadData) {
                return;
            }
            currentSize = listAudio.size();
            isLoadData = true;
            MediaPlayerLoader.getInstance().loadMore();
            return;
        }
        if (position <= -1) {
            position = 0;
        }
        Log.d("TAG", String.valueOf(listAudio.size()));
        String titleAudio = listAudio.get(position).title;
        String artistAudio = listAudio.get(position).artist;
        Message msg = Message.obtain(null,
                MusicService.MSG_SET_URL_NAME_POSITION_AND_PLAY_AUDIO, 0, 0);
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
