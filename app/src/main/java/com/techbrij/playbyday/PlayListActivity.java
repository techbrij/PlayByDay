package com.techbrij.playbyday;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


import android.content.Intent;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class PlayListActivity extends  ActionBarActivity {

    // Songs list
    public ArrayList<HashMap<String, String>> songsList;
    ArrayList<HashMap<String, String>> songsListData;
    private DragSortListView  listView;
    private  int dayid;
    DBRepo repo;
    //ArrayAdapter<HashMap<String,String>> adapter;
    SimpleAdapter adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);


        songsListData = new ArrayList<HashMap<String, String>>();
        //Get the bundle
        Bundle bundle = getIntent().getExtras();
        dayid =  bundle.getInt("dayid");
        String title =  bundle.getString("dayTitle");

        //Set action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(" " + title);
        actionBar.show();

        repo = new DBRepo(this);

        listView = (DragSortListView)findViewById(R.id.playlist);
        PrepareSongList();

        if (songsListData.isEmpty()){
            Toast.makeText(getApplicationContext(), "No song found.", Toast.LENGTH_SHORT).show();
        }

        // Adding menuItems to ListView
        adapter = new SimpleAdapter(this, songsListData,
               R.layout.playlist_item, new String[] { "title","filepath" }, new int[] {
                R.id.songTitle });

       // adapter = new ArrayAdapter<HashMap<String,String>>(this, R.layout.playlist_item, R.id.songTitle, songsListData);

        listView.setAdapter(adapter);
        listView.setDropListener(onDrop);
        listView.setRemoveListener(onRemove);
        // selecting single ListView item


        // listening to single listitem click
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting listitem index
                int songIndex = position;

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        MyActivity.class);
                // Sending songIndex to PlayerActivity
                in.putExtra("songIndex", songIndex);
                setResult(100, in);
                // Closing PlayListView
                finish();
            }
        });

    }

    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            if (from != to) {
                HashMap<String, String> item = (HashMap<String, String>)adapter.getItem(from);
                songsListData.remove(item);
                songsListData.add(to,item);

                repo.updateOrder(songsListData);

                adapter.notifyDataSetChanged();
                //Mark count dirty and need refresh
                MyActivity.isCountDirty = true;
            }
        }
    };

    private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
        @Override
        public void remove(int which) {

            HashMap<String, String> item =(HashMap<String, String>)adapter.getItem(which);

            repo.delete(Integer.parseInt(item.get("id")));

            songsListData.remove(item);
            adapter.notifyDataSetChanged();
            //Mark count dirty and need refresh
            MyActivity.isCountDirty = true;

        }
    };


    void PrepareSongList(){
        if (songsList !=null)      songsList.clear();
        if (songsListData !=null)  songsListData.clear();
        songsList = repo.getList(dayid);
        songsListData.addAll(MyActivity.GetAvailableFileList(songsList));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.play_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_playlist_play) {
            if (!songsListData.isEmpty()){
                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        MyActivity.class);
                // Sending songIndex to PlayerActivity
                in.putExtra("songIndex", 0);
                setResult(100, in);
                // Closing PlayListView
                finish();
            }
            else{
                Toast.makeText(getApplicationContext(), "No song found.", Toast.LENGTH_SHORT).show();
            }
        }
        if (id == R.id.action_playlist_add) {
            Intent intent = new Intent(getBaseContext(), FileSelectionActivity.class);
            startActivityForResult(intent, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode == RESULT_OK){
            ArrayList<File> Files = (ArrayList<File>) data.getSerializableExtra(FileSelectionActivity.FILES_TO_UPLOAD); //file array list
            String [] files_paths = new String[Files.size()]; //string array
            int i = 0;

            for(File file : Files){
                //String fileName = file.getName();
                String uri = file.getAbsolutePath();
                files_paths[i] = uri.toString(); //storing the selected file's paths to string array files_paths
                i++;
            }
            int startOrderIndex = 1;
            if (songsListData!= null && !songsListData.isEmpty()){
                String orderid =  songsListData.get(songsListData.size()-1).get("orderid");
                if (orderid!=null &&  !orderid.isEmpty()) {
                    startOrderIndex = Integer.parseInt(orderid);
                }
            }
            repo.Add(dayid,startOrderIndex, files_paths);
            PrepareSongList();
            adapter.notifyDataSetChanged();

            if (!Files.isEmpty()){
                MyActivity.isCountDirty = true;
            }
        }
        else{
        }
    }
}
