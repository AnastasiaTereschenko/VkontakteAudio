package com.example.anastasiyaverenich.audiovkontakte.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.anastasiyaverenich.audiovkontakte.R;
import com.example.anastasiyaverenich.audiovkontakte.activities.NotificationActivity;
import com.example.anastasiyaverenich.audiovkontakte.application.AudioApplication;
import com.example.anastasiyaverenich.audiovkontakte.receiver.NotificationDismissedReceiver;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {
    private MediaPlayer player;
    public int audioPosition;
    int i;
    String nameAudio;
    NotificationCompat.Builder notificationBuilder = null;
    final int NOTIFICATION_ID = 1;
    NotificationManager notificationManager;
    ArrayList<Messenger> clients = new ArrayList<Messenger>();
    String titleAudio;
    String artistAudio;
    boolean isEndAudio;
    int isNextOrPrev;
    boolean isDestroy;
    int secondProgress;
    String urlAudio;
    private Handler handler = new MessageHandler(this);
    private static final int SHOW_PROGRESS = 1;
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_PAUSE = 3;
    public static final int MSG_GET_POSITION_AND_DURATION = 4;
    public static final int MSG_SEEK_TO = 5;
    public static final int MSG_SET_URL_NAME_POSITION_AND_PLAY_AUDIO = 6;
    public static final int MSG_IS_END = 7;
    public static final int MSG_SET_SECONDARY_PROGRESS = 8;
    public static final int MSG_IS_TERMINATE_STATE = 9;
    public static final int MSG_IS_TOGGLE_MEDIAPLAYER = 10;
    public static final int MSG_UPDATE_UI_BY_TOGGLE = 11;
    public static final int MSG_IS_PLAYING = 12;
    //public static final int MSG_UPDATE_UI_BY_DISMISS_NOTIFICATION_AND_END_OF_LIST = 12;
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        initMusicPlayer();
        audioPosition = 0;
        i = 0;
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            toggleMediaPlayer(true);
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            toggleMediaPlayer(false);
        } else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            isNextOrPrev = 0;
            sendOnCompletionEventToClients();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            isNextOrPrev = 1;
            sendOnCompletionEventToClients();
        }
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setOnBufferingUpdateListener(this);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d("TAG", "Completion");
        isNextOrPrev = 1;
        isEndAudio = true;
        sendOnCompletionEventToClients();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d("TAG", "Prepared");
        mp.start();
        startProgressHandler();
    }

    private void startProgressHandler() {
        handler.removeCallbacksAndMessages(null);
        handler.sendEmptyMessage(SHOW_PROGRESS);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        secondProgress = percent;
        sendCurrentValueSecondaryProgress();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //player.stop();
        //player.release();
        return false;
    }

    public void pauseSong(boolean isHideNotification) {
        player.pause();
        buildNotification(generateAction(R.drawable.ic_play_arrow_black_24dp, " ", ACTION_PLAY));
        handler.removeCallbacksAndMessages(null);
        if (isHideNotification) {
            notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
        }
    }

    public void toggleMediaPlayer(boolean isHideNotification) {
        if (player.isPlaying()) {
            pauseSong(isHideNotification);
        } else {
            playBackSong();
        }
        Bundle dataToggleMediaPlayer = new Bundle();
        dataToggleMediaPlayer.putBoolean("valueIsToggle", player.isPlaying());
        Message msg = Message.obtain(null,
                MSG_UPDATE_UI_BY_TOGGLE, 0, 0);
        msg.setData(dataToggleMediaPlayer);
        sendMessageToAllClients(msg);
    }

    public void seekTo(int progress) {
        player.seekTo(progress);
    }

    public void playBackSong() {
        player.start();
        startProgressHandler();
        buildNotification(generateAction(R.drawable.ic_pause_black_24dp, " ", ACTION_PAUSE));
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.setAction(intentAction);
        //intent.set
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("com.my.app.notificationId", notificationId);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notificationId, intent, 0);
        return pendingIntent;
    }

    public void buildNotification(NotificationCompat.Action action) {
        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();
        Intent intent = new Intent(this, NotificationDismissedReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);
        Bitmap bitmapIcon = BitmapFactory.decodeResource(getResources(), R.drawable.background_music);
        notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setTicker(artistAudio + " " + titleAudio)
                .setLargeIcon(bitmapIcon)
                .setContentTitle(titleAudio)
                .setContentText(artistAudio)
                .setWhen(0)
                .setStyle(style)
                .setDeleteIntent(pi);
        notificationBuilder.addAction(generateAction(R.drawable.ic_skip_previous_black_24dp, " ", ACTION_PREVIOUS));
        notificationBuilder.addAction(action);
        notificationBuilder.addAction(generateAction(R.drawable.ic_skip_next_black_24dp, " ", ACTION_NEXT));
        style.setShowActionsInCompactView(0, 1, 2, 3, 4);
        style.setShowCancelButton(true)
                .setCancelButtonIntent(NotificationActivity.getDismissIntent(NOTIFICATION_ID, AudioApplication.get()));
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notificationBuilder.build());
    }

    public void playSong(String urlAudio) {
        isNextOrPrev = -1;
        if (player == null) {
            player = new MediaPlayer();
        } else {
            player.reset();
        }
        try {
            player.setDataSource(urlAudio);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
        buildNotification(generateAction(R.drawable.ic_pause_black_24dp, " ", ACTION_PAUSE));
    }

    public void setNameAudio(String songAndArtistAudio) {
        nameAudio = songAndArtistAudio;
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    clients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    clients.remove(msg.replyTo);
                    break;
                case MSG_PAUSE:
                    player.stop();
                    break;
                case MSG_IS_TERMINATE_STATE:
                    terminateState();
                    //Bundle dataIsDestroy = msg.getData();
                    //isDestroy = dataIsDestroy.getBoolean("valueIsDestroy");
                    break;
                case MSG_SEEK_TO:
                    int newPosition = msg.arg1;
                    seekTo(newPosition);
                    break;
                case MSG_SET_URL_NAME_POSITION_AND_PLAY_AUDIO:
                    Bundle bundle = msg.getData();
                    titleAudio = (String) bundle.get("titleAudio");
                    Log.d("titleAudio Service", titleAudio);
                    artistAudio = (String) bundle.get("artistAudio");
                    Log.d("artistAudio Service", artistAudio);
                    setNameAudio(titleAudio + artistAudio);
                    urlAudio = (String) bundle.get("valueUrl");
                    Log.d("valueUrl Service", urlAudio);
                    playSong(urlAudio);
                    break;
                case MSG_IS_TOGGLE_MEDIAPLAYER:
                    toggleMediaPlayer(true);
                    break;
                case MSG_IS_PLAYING:
                    break;
            }
        }
    }

    public void terminateState() {
        Log.d("terminateState", "stop");
        player.release();
        player = null;
        handler.removeCallbacksAndMessages(null);
    }

    public void stateMediaPlayer() {

    }

    private void sendCurrentValueSecondaryProgress() {
        Message msg;
        Bundle dataSetProgress = new Bundle();
        dataSetProgress.putInt("valueSetSecondProgress", secondProgress);
        msg = Message.obtain(null,
                MSG_SET_SECONDARY_PROGRESS, secondProgress, 0);
        msg.setData(dataSetProgress);
        sendMessageToAllClients(msg);
    }

    private void sendOnCompletionEventToClients() {
        buildNotification(generateAction(R.drawable.ic_pause_black_24dp, " ", ACTION_PAUSE));
        Message msg;
        Bundle dataIsEnd = new Bundle();
        dataIsEnd.putBoolean("valueIsEndAudio", isEndAudio);
        msg = Message.obtain(null, MSG_IS_END, isNextOrPrev, 0, isEndAudio);
        msg.setData(dataIsEnd);
        sendMessageToAllClients(msg);
    }

    private void sendMessageToAllClients(Message message) {
        for (int i = clients.size() - 1; i >= 0; i--) {
            try {
                clients.get(i).send(Message.obtain(message));
            } catch (RemoteException e) {
                clients.remove(i);
            }
        }
    }

    private void sendCurrentMediaPlayerStatusToClients() {
        Message msg;
        if (player == null) {
            return;
        }
        if (clients.size() == 0) {
            Log.d("terminateState", String.valueOf(clients.size()));
            //terminateState();
            return;
        }
        int positionProgressBar = player.getCurrentPosition();
        int durationProgressBar = player.getDuration();
        for (int i = clients.size() - 1; i >= 0; i--) {
            try {
                msg = Message.obtain(null,
                        MSG_GET_POSITION_AND_DURATION, positionProgressBar, durationProgressBar);
                clients.get(i).send(Message.obtain(msg));
            } catch (RemoteException e) {
                clients.remove(i);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private static class MessageHandler extends Handler {
        private final WeakReference<MusicService> mService;

        MessageHandler(MusicService service) {
            mService = new WeakReference<MusicService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mService.get();
            if (service == null) {
                return;
            }
            switch (msg.what) {
                case SHOW_PROGRESS:
                    service.sendCurrentMediaPlayerStatusToClients();
                    msg = obtainMessage(SHOW_PROGRESS);
                    sendMessageDelayed(msg, 500);
                    break;
            }
        }
    }

}