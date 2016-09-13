package com.example.anastasiyaverenich.audiovkontakte;

public interface MediaPlayerControlInterface {
    void updateUiByDismissNotification();

    void updateUiByToggle(boolean isPlaying);

    int setProgress(int position, int duration);

    void setSecondProgress(int percent);

    void playNextAudioByComplete(boolean isEnd, int isNextOrPrev);
}
