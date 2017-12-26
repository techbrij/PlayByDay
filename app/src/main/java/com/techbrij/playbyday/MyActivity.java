package com.techbrij.playbyday;

import java.io.File;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.View;

import android.widget.Toast;


public class MyActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener {


//service
    private MusicService musicSrv;
    private Intent playIntent;
    //binding
    private boolean musicBound=false;
    public static boolean isCountDirty = false;

    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;

    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView playListTitle;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;

    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();
    private Utilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds

    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private DBRepo repo;

    //GridView
    GridView gridView;
    ArrayList<ListItem> gridArray = new ArrayList<ListItem>();
    CustomGridViewAdapter customGridAdapter;
    private int playingDayId;
    private int selectedDayId;
    private int playingSongIndex;
    public static String[] shortWeekdays;

    public String[] getShortWeekdays() {
        String[] namesOfDays =  DateFormatSymbols.getInstance().getWeekdays();
        return Arrays.copyOfRange(namesOfDays, 1, namesOfDays.length);
    }

    public static int GetListItemWidth(Context context, int spacingBetweenListItem, int gridHorzPadding){
        //Get Screen Size
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;
        float ratio =(float) height/width;
        int ret =0;
        if(ratio > 2.0){
            ret =   (width -gridHorzPadding -spacingBetweenListItem*3) /4;
        }
        else if (ratio > 1.0){
            ret =   (width -gridHorzPadding -spacingBetweenListItem*3)/4;
        }
        else if (ratio > 0.5){
            ret =   (width -gridHorzPadding -spacingBetweenListItem*6)/7;
        }
        else {
            ret =  (width -gridHorzPadding -spacingBetweenListItem*6)/7;
        }
        return ret;
    }

    // To get available file list only
    public static ArrayList<HashMap<String, String>> GetAvailableFileList(ArrayList<HashMap<String, String>> allFiles){
        ArrayList<HashMap<String, String>> ret = new ArrayList<HashMap<String, String>>();
        // looping through playlist
        for (int i = 0; i < allFiles.size(); i++) {
            // creating new HashMap
            HashMap<String, String> song = allFiles.get(i);
            File file = new File(song.get("filepath"));
            if (file!=null && file.exists()){
                song.put("title", file.getName());
                // adding HashList to ArrayList
                ret.add(song);
            }
        }
        return ret;
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);

        //Music service start
        //Intent mService = new Intent(this, MusicService.class);
        //startService(mService);
        //Set action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle( " " + this.getString(R.string.app_name));  //To set space between icon and text
        actionBar.show();

        // All player buttons
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);

        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        playListTitle = (TextView) findViewById(R.id.playListTitle);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

        repo = new DBRepo(this);

        //Gridviewbind
        shortWeekdays = getShortWeekdays();
        int[] total = repo.getCount();
        for (int i = 0; i < 7; i++) {
            gridArray.add(new ListItem(shortWeekdays[i], total[i]));
        }
        gridView = (GridView) findViewById(R.id.gridView1);
        customGridAdapter = new CustomGridViewAdapter(this, R.layout.listitem, gridArray);
        int defaultSpacing = (int)(10 * getResources().getDisplayMetrics().density); // default -5dp both side
        int horzSpacing = 2* getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin) +defaultSpacing;
        int listItemSpacing = getResources().getDimensionPixelSize(R.dimen.listitem_spacing);
        gridView.setColumnWidth(GetListItemWidth(this,listItemSpacing,horzSpacing));
        gridView.setAdapter(customGridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                //int pos = ((CustomGridViewAdapter.RecordHolder)v.getTag()).position;

                selectedDayId = pos;
                //Code to show playlist
                Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
                i.putExtra("dayid",selectedDayId);
                i.putExtra("dayTitle",shortWeekdays[selectedDayId]);
                startActivityForResult(i, 100);
            }
});


        playingDayId = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        this.songsList =  MyActivity.GetAvailableFileList(repo.getList(playingDayId));

        // Mediaplayer

        utils = new Utilities();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important




        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (songsList.size() == 0){
                    btnPlay.setImageResource(R.drawable.btn_play);
                    Toast.makeText(getApplicationContext(), "No song found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check for already playing
                if(musicSrv.isPlaying()){
                    if(musicSrv!=null){
                        musicSrv.pausePlayer();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.btn_play);
                    }
                }else{
                    // Resume song
                    if(musicSrv!=null){
                        musicSrv.start();
                        if (musicSrv.isPlaying()) {
                            // Changing button image to pause button
                            btnPlay.setImageResource(R.drawable.btn_pause);
                        }
                    }
                }
            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         **/
        btnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (songsList.isEmpty()) {
                    return;
                }
                if (musicSrv!= null) {
                    // get current song position
                    int currentPosition = musicSrv.getPosn();
                    // check if seekForward time is lesser than song duration
                    if (currentPosition + seekForwardTime <= musicSrv.getDur()) {
                        // forward song
                        musicSrv.seek(currentPosition + seekForwardTime);
                    } else {
                        // forward to end position
                        musicSrv.seek(musicSrv.getDur());
                    }
                }
            }
        });

        /**
         * Backward button click event
         * Backward song to specified seconds
         **/
        btnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (songsList.isEmpty()) {
                    return;
                }

                if (musicSrv!= null) {
                    // get current song position
                    int currentPosition = musicSrv.getPosn();
                    // check if seekBackward time is greater than 0 sec
                    if (currentPosition - seekBackwardTime >= 0) {
                        // forward song
                        musicSrv.seek(currentPosition - seekBackwardTime);
                    } else {
                        // backward to starting position
                        musicSrv.seek(0);
                    }
                }

            }
        });

        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (songsList.isEmpty()) {
                    return;
                }
                else{
                    btnPlay.setImageResource(R.drawable.btn_pause);
                }
                musicSrv.playNext();
                playListTitle.setText(shortWeekdays[playingDayId]);
                songTitleLabel.setText(musicSrv.songTitle);
            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (songsList.isEmpty()) {
                    return;
                }
                else{
                    btnPlay.setImageResource(R.drawable.btn_pause);
                }
                musicSrv.playPrev();
                playListTitle.setText(shortWeekdays[playingDayId]);
                songTitleLabel.setText(musicSrv.songTitle);
            }
        });

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(isRepeat){
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }else{
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }
                musicSrv.setShuffle(isShuffle);
                musicSrv.setRepeat(isRepeat);
            }
        });

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(isShuffle){
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }else{
                    // make repeat to true
                    isShuffle= true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }
                musicSrv.setShuffle(isShuffle);
                musicSrv.setRepeat(isRepeat);
            }
        });
    }


    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();

            if (musicSrv!=null && !musicSrv.isPlaying()) {

                //Set Mediaplayer listners
                MediaPlayer mp = musicSrv.getMediaPlayer();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        //check if playback has reached the end of a track
                        if(musicSrv.getPosn()>0){
                            mediaPlayer.reset();
                            if(isRepeat){
                                // repeat is on play same song again
                                musicSrv.playSong();
                            }
                            else {
                                musicSrv.playNext();
                                playListTitle.setText(shortWeekdays[playingDayId]);
                                songTitleLabel.setText(musicSrv.songTitle);
                                //change title

                            }
                        }
                        /*if(musicSrv!=null && musicSrv.isPlaying()){
                            btnPlay.setImageResource(R.drawable.btn_pause);
                        }
                        else{
                            btnPlay.setImageResource(R.drawable.btn_play);
                        }*/
                    }
                });
                mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Log.v("MUSIC PLAYER", "Playback Error");
                        mp.reset();
                        return false;
                    }
                });

                //pass list
                musicSrv.setList(songsList);
                musicBound = true;
                playSong(0);



            }
            // By default play first song

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }

    };

    //start and bind the service when the activity starts
    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }







    /**
     * Function to play a song
     * @param songIndex - index of song
     * */
    public void  playSong(int songIndex){
        // Play song
        try {
            musicSrv.reset();
            songTitleLabel.setText("");
            if (songsList.size() == 0){
                btnPlay.setImageResource(R.drawable.btn_play);
                Toast.makeText(getApplicationContext(), "No song found.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (songsList.size() > songIndex) {

                String path = songsList.get(songIndex).get("filepath");
                musicSrv.playSong(songIndex);

                // Displaying Song title
                String songTitle = new File(path).getName();
                playListTitle.setText(shortWeekdays[playingDayId]);
                songTitleLabel.setText(songTitle);
                // Changing Button Image to pause image
                btnPlay.setImageResource(R.drawable.btn_pause);

                // set Progress bar values
                songProgressBar.setProgress(0);
                songProgressBar.setMax(100);

                // Updating progress bar
                updateProgressBar();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
           // long totalDuration = getDuration();
           // long currentDuration = getCurrentPosition();
            long totalDuration =0, currentDuration=0;

           if (musicSrv!=null) {

               try {
                   if (!musicSrv.playerState.equals(MusicService.State.Preparing) &&!musicSrv.playerState.equals(MusicService.State.Retrieving)) {
                       totalDuration = musicSrv.getDur();
                       currentDuration = musicSrv.getPosn();
                   }
               }
               finally{
                   //To ignore error due to mediaplayer loading
                   //Attempt to call getDuration without a valid mediaplayer
               }
           }

               // Displaying Total Duration time
               songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
               // Displaying time completed playing
               songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));

               // Updating progress bar
               int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
               //Log.d("Progress", ""+progress);
               songProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     * */
   @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration =musicSrv.getDur();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        musicSrv.seek(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     * */


    @Override
    public void onDestroy(){

     stopService(playIntent);
     unbindService(musicConnection);
     musicSrv = null;
     super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        if ( musicSrv.playerState == MusicService.State.Paused) {
            super.onBackPressed();
        }else {
            moveTaskToBack(true);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            AlertDialog diaBox = AboutDialog();
            diaBox.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Receiving song index from playlist view
     * and play the song
     * */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
            if (selectedDayId != playingDayId || MyActivity.isCountDirty)
            {
                songsList =  MyActivity.GetAvailableFileList(repo.getList(selectedDayId));
                musicSrv.setList(songsList);
                playingDayId = selectedDayId;

            }
            int  currentSongIndex = data.getExtras().getInt("songIndex");
            // play selected song
            playSong(currentSongIndex);
        }
        //If user added or deleted files in playlist then refresh the count
        if (MyActivity.isCountDirty){
            MyActivity.isCountDirty = false;
            String[] shortWeekdays = getShortWeekdays();
            int[] total = repo.getCount();
            gridArray.clear();
            for (int i = 0; i < 7; i++) {
                gridArray.add(new ListItem(shortWeekdays[i], total[i]));
            }
            customGridAdapter.notifyDataSetChanged();
        }
    }


    private AlertDialog AboutDialog()
    {
        AlertDialog aboutDialogBox =new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle("About")
                .setMessage("Developed by TechBrij. Visit us at TechBrij.Com for more information.")
                        //   .setIcon(R.drawable.delete)

                .setPositiveButton("Open", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //your deleting code
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://techbrij.com"));
                        startActivity(browserIntent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .create();
        return aboutDialogBox;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
           // setContentView(R.layout.player);

        } else {
           // setContentView(R.layout.player);
        }
        int defaultSpacing = (int)(10 * getResources().getDisplayMetrics().density); // default -5dp both side
        int horzSpacing = 2* getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin) +defaultSpacing;
        int listItemSpacing = getResources().getDimensionPixelSize(R.dimen.listitem_spacing);
        gridView.setColumnWidth(GetListItemWidth(this,listItemSpacing,horzSpacing));


    }
 }
