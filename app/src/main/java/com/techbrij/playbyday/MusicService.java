package com.techbrij.playbyday;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener {


    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<HashMap<String, String>> songsList;
    //current position
    private int songPosn;
    //binder
    private final IBinder musicBind = new MusicBinder();
    //title of current song
    public String songTitle="";
    private String songPath;
    //notification id
    private static final int NOTIFY_ID=1;
    //shuffle flag and random
    private boolean shuffle=false, isRepeat = false;
    private Random rand;

    // indicates the state our service:
    public enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped, // media player is stopped and not prepared to play
        Preparing, // media player is preparing...
        Playing, // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        Paused
        // playback paused (media player ready!)
    };

    public State playerState = State.Retrieving;

    public void onCreate(){
        //create the service
        super.onCreate();
        //initialize position
        songPosn=0;
        //random
        rand=new Random();
        //create player
        player = new MediaPlayer();
        //initialize
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        //player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        player.setOnPreparedListener(this);
        //  player.setOnCompletionListener(this);
        //  player.setOnErrorListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        //Message msg = mServiceHandler.obtainMessage();
        //msg.arg1 = startId;
        //mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    public MediaPlayer getMediaPlayer(){
        return player;
    }

    //pass song list
    public void setList(ArrayList<HashMap<String, String>> theSongs){
        songsList =theSongs;
    }


    //binder
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    //activity will bind to service
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    //release resources when unbind
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    //play a song
    public void playSong(){
        if (songsList.size() > songPosn) {
            //get song
            HashMap<String, String> playSong = songsList.get(songPosn);
            //get title

            songPath =  playSong.get("filepath");
            songTitle =  new File(songPath).getName();

            //set the data source
            try {
                player.reset();
                player.setDataSource(songPath);
            } catch (Exception e) {
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }


            player.prepareAsync();
            playerState = State.Preparing;
        }
    }

    //set the song
    public void playSong(int songIndex){
        songPosn=songIndex;
        playSong();
    }





    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
        playerState = State.Playing;

        //notification
        Intent notIntent = new Intent(this, MyActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("PlayByDay")
                .setContentText(songTitle);
        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);
    }

    //playback methods
    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public void reset(){
         player.reset();
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public void pausePlayer(){

        if (playerState.equals(State.Playing)) {
            player.pause();
            playerState = State.Paused;
        }
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void start(){
        if (!playerState.equals(State.Preparing) &&!playerState.equals(State.Retrieving)) {
            player.start();
            playerState = State.Playing;
        }
    }

    //skip to previous track
    public void playPrev(){
        songPosn--;
        if(songPosn<0) songPosn= songsList.size()-1;
        playSong();
    }

    //skip to next
    public void playNext(){
        if(shuffle){
            int newSong = songPosn;
            while(newSong==songPosn){
                newSong=rand.nextInt(songsList.size());
            }
            songPosn=newSong;
        }
        else{
            songPosn++;
            if(songPosn>= songsList.size()) songPosn=0;
        }
        playSong();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        playerState = State.Retrieving;
    }


    //toggle shuffle
    public void setShuffle(boolean _shuffle ){
      shuffle=_shuffle;
    }

    public void setRepeat(boolean _isRepeat){
      isRepeat  =_isRepeat;
    }

}