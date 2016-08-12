package com.jams.music.player.firebase;

/**
 * Created by HuyLV-CT on 10-Aug-16.
 */
public class Song {
    public String name;
    public String artist;
    public String filePath;
    public boolean availableOnServer;

    public Song(String name, String artist, String filePath) {
        this.name = name;
        this.artist = artist;
        this.filePath = filePath;
        availableOnServer=false;
    }

    public Song(String name, String artist) {
        this.name = name;
        this.artist = artist;
        availableOnServer=false;
    }

    public Song() {

    }

    @Override
    public String toString() {
        return "Song{" +
                "name='" + name + '\'' +
                ", artist='" + artist + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
