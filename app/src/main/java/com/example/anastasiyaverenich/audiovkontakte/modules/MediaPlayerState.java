package com.example.anastasiyaverenich.audiovkontakte.modules;

public class MediaPlayerState {
    private boolean isPlaying;
    private int position;
    private int positionSeekBar;
    private boolean isSetAudio;

    public MediaPlayerState() {
    }

    public boolean getIsSetAudio() {
        return isPlaying;
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public int getPositionSeekBar() {
        return positionSeekBar;
    }

    public int getPosition() {
        return position;
    }

    public void setIsSetAudio(boolean isSetAudio) {
        this.isSetAudio = isSetAudio;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setPositionSeekBar(int positionSeekBar) {
        this.positionSeekBar = positionSeekBar;
    }

}
