package com.example.anastasiyaverenich.audiovkontakte.singleton;

import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.anastasiyaverenich.audiovkontakte.modules.Audio;

import java.util.List;

public class MediaPlayerSetUi {
    private static MediaPlayerSetUi ourInstance = new MediaPlayerSetUi();
    int currentPercent = 0;

    public static MediaPlayerSetUi getInstance() {
        return ourInstance;
    }

    private MediaPlayerSetUi() {

    }

    public int setProgress(SeekBar seekBar, int position, int duration) {
        if (seekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                seekBar.setProgress((int) pos);
            }
        }
        return position;
    }

    public void setSecondProgress(SeekBar seekBar, int percent) {
        if (percent > currentPercent) {
            int perc = percent * 10;
            seekBar.setSecondaryProgress(perc);
        }
        currentPercent = percent;
    }

    public void setNameAndArtistAudio(List<Audio.Track> listAudio, int position, TextView nameAudio, TextView artistAudio) {
        if (listAudio.size() <= position) {
            Log.d("setNameAndArtistAudio", String.valueOf(listAudio.size()));
            return;
        }
        if (position <= -1) {
            position = 0;
        }
        String artistAudioPos = listAudio.get(position).getArtist();
        String nameAudioPos = listAudio.get(position).getTitle();
        artistAudio.setText(artistAudioPos);
        nameAudio.setText(nameAudioPos);

    }
}
