package com.example.anastasiyaverenich.audiovkontakte.modules;

public class AudioModel {
    private final String artist;
    private final String title;
    private final int duration;

    public AudioModel(String artist, String title, int duration) {
        this.artist = artist;
        this.title = title;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {

        return duration;
    }

    public String getArtist() {

        return artist;
    }
}
