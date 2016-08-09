package com.example.anastasiyaverenich.audiovkontakte;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.anastasiyaverenich.audiovkontakte.modules.Audio;

import java.util.List;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    //media player
    private MediaPlayer player;
    //song list
    private List<Audio.Track> songs;
    //current position
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        initMusicPlayer();
        songPosn = 0;
    }

    public void initMusicPlayer() {

        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d("TAG", "Prepared");
        mp.start();

    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    public void setList(List<Audio.Track> listAudio) {
        songs = listAudio;
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public void pauseSong() {
        if (player.isPlaying()) {
            player.pause();
        }
    }

    public void seekTo(int progress) {
        player.seekTo(progress);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void playBackSong() {
        player.start();
    }

    public void playSong() {
        player.reset();
        Audio.Track playSong = songs.get(songPosn);
        try {
            player.setDataSource(playSong.getUrl());

        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public int getDuration() {
        return player.getDuration();
    }

    public void playNextOrPrevSong() {
        // player.stop();
        player.reset();
        Audio.Track playSong;
        playSong = songs.get(songPosn);
        try {
            player.setDataSource(playSong.getUrl());

        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
        player.start();
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }


}

