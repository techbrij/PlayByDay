package com.techbrij.playbyday;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
/**
 * Created by Brij on 4/25/2015.
 */
public class CustomFileListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] web;
    String ParentFolder;
    public CustomFileListAdapter(Activity context, String[] web,String path) {
        super(context, R.layout.custom_file_list_adapter, web);
        this.context = context;
        this.web = web;
        ParentFolder = path;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.custom_file_list_adapter, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(web[position]);
        imageView.setImageResource(R.drawable.document);
        /*Picasso.with(context).load(
                new File(
                        ParentFolder+"/"+web[position]
                )).placeholder(R.drawable.document).resize(50, 50).into(imageView);*/
        return rowView;
    }
}
