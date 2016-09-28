package com.example.anastasiyaverenich.audiovkontakte.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.anastasiyaverenich.audiovkontakte.R;
import com.example.anastasiyaverenich.audiovkontakte.adapters.AudioRecyclerAdapter;
import com.example.anastasiyaverenich.audiovkontakte.interfaces.MediaPlayerControlInterface;
import com.example.anastasiyaverenich.audiovkontakte.modules.Audio;
import com.example.anastasiyaverenich.audiovkontakte.singleton.MediaPlayerControl;
import com.example.anastasiyaverenich.audiovkontakte.singleton.MediaPlayerSetUi;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class DetailsAudioFragment extends android.support.v4.app.Fragment implements MediaPlayerControlInterface {
    public static int IS_NEXT = 1;
    public static int IS_PREV = 0;
    public static boolean IS_END = false;
    public ImageView skipPrev;
    public ImageView skipNext;
    public ImageView playPause;
    public TextView currentPlayText;
    public TextView endPlayText;
    public SeekBar seekBar;
    public TextView nameAudio;
    public TextView artistAudio;
    public TextView line;
    public ImageView backgroundImage;
    StringBuilder formatBuilder;
    Formatter formatter;
    public int pressedPosition = -1;
    public List<Audio.Track> listAudio;
    public AudioRecyclerAdapter adapter;
    private MediaPlayerSetUi mediaPlayerSetUi;
    private MediaPlayerControl mediaPlayerControl;

    public DetailsAudioFragment(int position, List<Audio.Track> listAudio, AudioRecyclerAdapter adapter) {
        //super();
        this.listAudio = listAudio;
        this.pressedPosition = position;
        this.adapter = adapter;
        mediaPlayerSetUi = MediaPlayerSetUi.getInstance();
        mediaPlayerControl = MediaPlayerControl.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_audio, container, false);
        MediaPlayerControl.getInstance().subscribeToUpdatesFromMediaPlayerControl(this);

        backgroundImage = (ImageView) v.findViewById(R.id.background_image);
        playPause = (ImageView) v.findViewById(R.id.play_pause);
        skipNext = (ImageView) v.findViewById(R.id.next);
        skipPrev = (ImageView) v.findViewById(R.id.prev);
        currentPlayText = (TextView) v.findViewById(R.id.startText);
        endPlayText = (TextView) v.findViewById(R.id.endText);
        seekBar = (SeekBar) v.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(MediaPlayerControl.getInstance().seekListener);
        seekBar.setMax(1000);
        nameAudio = (TextView) v.findViewById(R.id.line1);
        artistAudio = (TextView) v.findViewById(R.id.line2);
        line = (TextView) v.findViewById(R.id.line3);
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        setNameAndArtistAudio(listAudio, pressedPosition);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayerControl.getInstance().isToggleMediaPlayer();
            }
        });
        skipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressedPosition = mediaPlayerControl.playNextOrPrevAudio(IS_NEXT);
                setNameAndArtistAudio(listAudio, pressedPosition);
                updateUiByToggle(true);
            }
        });
        skipPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressedPosition = mediaPlayerControl.playNextOrPrevAudio(IS_PREV);
                setNameAndArtistAudio(listAudio, pressedPosition);
                updateUiByToggle(true);
            }
        });
        return v;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        formatBuilder.setLength(0);
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayerControl.unsubscribeToUpdatesFromMediaPlayerControl(this);
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        //MediaPlayerControl.getInstance().doUnbindService();
    }

    @Override
    public void updateUiByDismissNotification() {
        playPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
    }

    @Override
    public void updateUiByToggle(boolean isPlaying) {
        playPause.setImageResource(!isPlaying ? R.drawable.ic_play_arrow_black_24dp : R.drawable.ic_pause_black_24dp);
    }

    @Override
    public void updateUiByComplete(boolean isEnd, int isNextOrPrev) {
        int position = mediaPlayerControl.getCurrentPosition();
        Log.d("TAG", String.valueOf(position));
        setNameAndArtistAudio(listAudio, position);
        updateUiByToggle(true);
    }

    @Override
    public void setProgress(int position, int duration) {
        mediaPlayerSetUi.setProgress(seekBar, position, duration);
        if (endPlayText != null)
            endPlayText.setText(stringForTime(duration));
        if (currentPlayText != null)
            currentPlayText.setText(stringForTime(position));
    }

    @Override
    public void setSecondProgress(int percent) {
        mediaPlayerSetUi.setSecondProgress(seekBar, percent);
    }

    @Override
    public void setNameAndArtistAudio(List<Audio.Track> listAudio, int position) {
        mediaPlayerSetUi.setNameAndArtistAudio(listAudio, position, nameAudio, artistAudio);
    }
}
