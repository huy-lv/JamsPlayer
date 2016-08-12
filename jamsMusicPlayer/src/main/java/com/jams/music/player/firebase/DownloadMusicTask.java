package com.jams.music.player.firebase;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by HuyLV-CT on 11-Aug-16.
 */
public class DownloadMusicTask extends AsyncTask<Void,Void,String> {
    private String songName;
    private String artist;
    private Context context;
    private ArrayList<String> linkList= new ArrayList<>();

    public DownloadMusicTask(Context c){
        context = c;
    }

    @Override
    protected String doInBackground(Void... voids) {
        for(int i=0;i<1;i++) {
            Song s = Config.songToDownload.get(i);
            Log.e("cxz", "download "+ s);
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
                        linkList.add(link);
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


        String destPath = Environment.getExternalStorageDirectory() + File.separator + songName+"-"+artist+".mp3";

        for(String l:linkList){
            Log.e("cxz","___link:"+l);
        }

        Uri downloadUri = Uri.parse(linkList.get(0));
        Uri destinationUri = Uri.parse(context.getExternalCacheDir().toString()+"/test.mp3");
        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                .addCustomHeader("Auth-Token", "YourTokenApiKey")
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setStatusListener(new DownloadStatusListenerV1() {
                    @Override
                    public void onDownloadComplete(DownloadRequest downloadRequest) {
                        Log.e("cxz","done e");
                    }

                    @Override
                    public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {

                    }

                    @Override
                    public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {

                    }
                });
        ThinDownloadManager thinDownloadManager = new ThinDownloadManager();
        thinDownloadManager.add(downloadRequest);

    }
}
