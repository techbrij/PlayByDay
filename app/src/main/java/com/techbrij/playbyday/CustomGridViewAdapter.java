package com.techbrij.playbyday;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Brij on 4/23/2015.
 */
public class CustomGridViewAdapter extends ArrayAdapter<ListItem> {

    Context context;
    int layoutResourceId;
    ArrayList<ListItem> data = new ArrayList<ListItem>();

    public CustomGridViewAdapter(Context context, int layoutResourceId,
                                 ArrayList<ListItem> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new RecordHolder();
            holder.txtTotal = (TextView) row.findViewById(R.id.ListItemTotal);
            holder.txtTitle = (TextView) row.findViewById(R.id.ListItemTitle);
            holder.txtSuffix = (TextView) row.findViewById(R.id.ListItemSuffix);

            holder.position = position;
            row.setTag(holder);
        } else {
            holder = (RecordHolder) row.getTag();
        }

        ListItem item = data.get(position);
        holder.txtTitle.setText(item.title);
        holder.txtTotal.setText("" + item.total);
        if (item.total <2) {
            holder.txtSuffix.setText("Song");
        }
        else{
            holder.txtSuffix.setText("Songs");
        }
        return row;
    }

    static class RecordHolder {
        TextView txtTitle;
        TextView txtTotal;
        TextView txtSuffix;
        int position;
    }
}
