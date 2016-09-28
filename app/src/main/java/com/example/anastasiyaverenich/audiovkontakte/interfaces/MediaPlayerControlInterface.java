package com.example.anastasiyaverenich.audiovkontakte.interfaces;

import com.example.anastasiyaverenich.audiovkontakte.modules.Audio;

import java.util.List;

public interface MediaPlayerControlInterface {
    void updateUiByDismissNotification();

    void updateUiByToggle(boolean isPlaying);

    void updateUiByComplete(boolean isEnd, int isNextOrPrev);

    void setProgress(int position, int duration);

    void setSecondProgress(int percent);

    void setNameAndArtistAudio(List<Audio.Track> listAudio, int position);

}
