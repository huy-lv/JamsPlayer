package com.jams.music.player.firebase;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by HuyLV-CT on 09-Aug-16.
 */
public class Config {
    public static final String Firebase_Url = "https://jams-eae78.firebaseio.com/";
    public static final String CSN_URL = "http://chiasenhac.vn/";
    public static ArrayList<Song> localSongList = new ArrayList<>();
    public static ArrayList<Song> serverSongList = new ArrayList<>();
    public static ArrayList<Song> songToDownload = new ArrayList<>();
    public static ArrayList<Song> songToDelete = new ArrayList<>();

    public static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public static final String TITLE = "title";
    public static final String FILE_PATH = "file_path";
    public static final String ARTIST = "artist";
}
