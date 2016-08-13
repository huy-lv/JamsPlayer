package com.jams.music.player.firebase;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.jams.music.player.DBHelpers.DBAccessHelper;
import com.jams.music.player.MainActivity.MainActivity;

/**
 * Created by HuyLV-CT on 10-Aug-16.
 */
public class SyncMusicTask extends AsyncTask<Void,Void,Integer> {
    Context context;

    public SyncMusicTask(Context c){
        context = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ((MainActivity) context).startSyncAnim();
    }

    @Override
    protected Integer doInBackground(Void... voids) {

        getLocalSongList();
        syncWithLocal();
        return 1;
    }

    private void syncWithLocal() {
        //check each server song
        for(Song s : Config.serverSongList){
            int temp = getSongExistsInLocal(s);
            if(temp == -1){
                //song not in local
                Config.songToDownload.add(s);
            }else{
                //song in local
                Config.localSongList.get(temp).availableOnServer = true;
            }
        }

        //delete local song
        for(Song s:Config.localSongList){
            if(!s.availableOnServer){
                Config.songToDelete.add(s);
            }
        }
    }


    private int getSongExistsInLocal(Song serverSong){
        for(int i=0;i<Config.localSongList.size();i++){
            if(serverSong.name.equalsIgnoreCase(Config.localSongList.get(i).name)){
                if(serverSong.artist.equalsIgnoreCase(Config.localSongList.get(i).artist)){
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    protected void onPostExecute(Integer aVoid) {
        super.onPostExecute(aVoid);
        Log.e("cxz","doneeeeeeeeeee");
        if(Config.songToDownload.size()>0 || Config.songToDelete.size()>0) {
            DownloadMusicTask downloadMusicTask = new DownloadMusicTask(context);
            downloadMusicTask.execute();
        } else {
            Toast.makeText(context, "Everything synced!", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocalSongList() {
        DBAccessHelper db = DBAccessHelper.getInstance(context);
        Cursor c = db.getAllSongs();
        //Retrieve data from the cursor.
        if (c.moveToFirst()) {
            do {
                String titleText = c.getString(c.getColumnIndex(Config.TITLE));
                String filePath = c.getString(c.getColumnIndex(Config.FILE_PATH));
                String artist = c.getString(c.getColumnIndex(Config.ARTIST));
                Config.localSongList.add(new Song(titleText, artist, filePath));
            } while (c.moveToNext());
        }
    }
}
