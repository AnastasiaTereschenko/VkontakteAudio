package com.example.anastasiyaverenich.audiovkontakte.modules;

import java.util.List;

public class Audio {
    public List<Track> response;

    public static class Track {
        public String artist;
        public String title;
        public int duration;
        public String url;

        public Track(String artist, String title, int duration, String url) {
            this.artist = artist;
            this.title = title;
            this.duration = duration;
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public long getDuration() {

            return duration;
        }

        public String getArtist() {

            return artist;
        }

        public String getUrl() {
            return url;
        }
    }

}
