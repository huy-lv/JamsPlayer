package com.jams.music.player.firebase;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.jams.music.player.MainActivity.MainActivity;
import com.jams.music.player.Services.BuildMusicLibraryService;
import com.jams.music.player.WelcomeActivity.WelcomeActivity;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;

/**
 * Created by HuyLV-CT on 11-Aug-16.
 */
public class DownloadMusicTask extends AsyncTask<Void,Void,String> {
    BuildMusicLibraryService service;
    private String songName;
    private String artist;
    private Context context;
    private File rootFolder;
    private int ii;
    private int downloadedSong = 0;
    private int totalSong;

    public DownloadMusicTask(Context c){
        context = c;
    }

    @Override
    protected String doInBackground(Void... voids) {

        //create root directory
        rootFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "music");
        boolean success = true;
        if (!rootFolder.exists()) {
            success = rootFolder.mkdir();
        } else {
            Log.e("cxz", "root folder exist");
        }
        if (success) {
            Log.e("cxz", "root folder created");
        } else {
            Log.e("cxz", "create error");
        }

        totalSong = Config.songToDownload.size();
        //
        for (int i = 0; i < totalSong; i++) {
            Song s = Config.songToDownload.get(i);
            try {
                songName = s.name;
                artist = s.artist;
                Document doc = Jsoup.connect("http://search.chiasenhac.vn/search.php?s=" + songName).get();
//            Log.e("cxz","doc"+doc.head().toString());
                int stt = 0;

                String currentArtist;
                do {
                    stt += 1;
                    String cssPath = "body > div.mu-wrapper > div > div.m-left > div > div > div.pad > div.h-main > div.page-dsms > div.bod > table > tbody > tr:nth-child(" + (stt + 1) + ") > td:nth-child(2) > div > div > p:nth-child(2)";
                    currentArtist = doc.select(cssPath).first().text();
                } while (!currentArtist.equalsIgnoreCase(artist) && stt <= 5);

                if (stt != 6) {
                    String cssSong = "body > div.mu-wrapper > div > div.m-left > div > div > div.pad > div.h-main > div.page-dsms > div.bod > table > tbody > tr:nth-child(" + (stt + 1) + ") > td:nth-child(2) > div > div > p:nth-child(1) > a";
                    String songLink = doc.select(cssSong).attr("href");
                    String link =  getDownloadLink(songLink);
                    if(link!=null){
                        Config.songToDownload.get(i).downloadLink = link;
                    }else{
                        Log.e("cxz","null at "+i);
                    }

                } else {
                    Log.e("cxz", "not found" + songName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String getDownloadLink(String songLink) {
        String link=null;
        try {
            songLink = songLink.substring(0,songLink.length()-5);
            Document docSong = Jsoup.connect(Config.CSN_URL+songLink+"_download.html").get();
            String cssDownloadLink = "#downloadlink > b";
            Element e = docSong.select(cssDownloadLink).first();
            String tempHtml = e.html();
            int t1 = tempHtml.indexOf("document.write");
            int t2 = tempHtml.indexOf(".mp3");
            link = tempHtml.substring(t1+124,t2+4);

            Log.e("cxz","link:"+link);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return link;
    }

    @Override
    protected void onPostExecute(String aVoid) {
        super.onPostExecute(aVoid);
        Log.e("cxz","done2");

        ThinDownloadManager thinDownloadManager = new ThinDownloadManager();

        for (ii = 0; ii < totalSong; ii++) {
            final Song s = Config.songToDownload.get(ii);
            Log.e("cxz", "___link:" + s.downloadLink);
            Uri downloadUri = Uri.parse(s.downloadLink);
            Uri destinationUri = Uri.parse(rootFolder + "/" + s.name + "-" + s.artist + ".mp3");
            DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                    .setRetryPolicy(new DefaultRetryPolicy())
                    .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                    .setStatusListener(new DownloadStatusListenerV1() {
                        @Override
                        public void onDownloadComplete(DownloadRequest downloadRequest) {
                            Log.e("cxz", "download done:" + s);
                            if (downloadedSong == totalSong - 1) {
                                Log.e("cxz", "done e");
                                ((MainActivity) context).stopSyncAnim();
//                                Toast.makeText(context,"Sync done!",Toast.LENGTH_SHORT).show();
                                Config.songToDownload.clear();

                                Intent intent = new Intent(context, WelcomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("REFRESH_MUSIC_LIBRARY", true);
                                context.startActivity(intent);
                            }
                            downloadedSong++;
                        }

                        @Override
                        public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {

                        }

                        @Override
                        public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {

                        }
                    });

            thinDownloadManager.add(downloadRequest);
        }
    }
}
