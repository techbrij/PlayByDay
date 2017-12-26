package com.techbrij.playbyday;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Brij on 4/25/2015.
 */
public class DBRepo {

    private DBHelper dbHelper;

    public DBRepo(Context context) {
        dbHelper = new DBHelper(context);
    }

    //To display day wise count on home screen
    public int[] getCount() {

        int[] ret = new int[] {0,0,0,0,0,0,0 };

        //Open connection to read only
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                DBModel.KEY_dayid + ", Count(*) Total" +
                " FROM " + DBModel.TABLE + " group by " +  DBModel.KEY_dayid;

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                ret[ cursor.getInt(cursor.getColumnIndex(DBModel.KEY_dayid))] = cursor.getInt(cursor.getColumnIndex("Total" ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return ret;

    }

    //To display day wise songs
    public ArrayList<HashMap<String, String>>getList( int dayid) {
        //Open connection to read only
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                DBModel.KEY_ID + "," +
                DBModel.KEY_dayid + "," +
                DBModel.KEY_filepath + "," +
                DBModel.KEY_orderid +
                " FROM " + DBModel.TABLE + " WHERE " +  DBModel.KEY_dayid + " = " + dayid
                + " Order by " +  DBModel.KEY_orderid;

        //Student student = new Student();
        ArrayList<HashMap<String, String>> songList = new ArrayList<HashMap<String, String>>();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("id", cursor.getString(cursor.getColumnIndex(DBModel.KEY_ID)));
                song.put("filepath", cursor.getString(cursor.getColumnIndex(DBModel.KEY_filepath)));
                song.put("orderid", cursor.getString(cursor.getColumnIndex(DBModel.KEY_orderid)));
                songList.add(song);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return songList;

    }

    //To add new songs
    public boolean Add(int dayid,int startOrderIndex, String[] filepath) {

    //Open connection to write data
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    try {
        String sql ="";
        int orderIndex;
        for (int i = 0; i < filepath.length; i++) {
            orderIndex = startOrderIndex + i;
            sql = "INSERT INTO " + DBModel.TABLE +
                    " (" + DBModel.KEY_dayid +","+ DBModel.KEY_filepath +","+ DBModel.KEY_orderid + ")"
                    +" SELECT * FROM (SELECT "+ dayid +",'" + filepath[i]  + "'," + (startOrderIndex + i) + ")"+
                    " WHERE NOT EXISTS ("+
                    " SELECT 1 FROM " +  DBModel.TABLE  + " WHERE " + DBModel.KEY_dayid + " = " + dayid + " and "+ DBModel.KEY_filepath +" ='"+ filepath[i] +"');";
            db.execSQL(sql);
        }
    }
    catch (Exception e) {
        e.printStackTrace();
        return false;
    }
        finally {
            db.close();
        }
         // Closing database connection
        return true;
    }


    public void updateOrder(ArrayList<HashMap<String, String>> objList) {

        //Note: Bulk SQL statement ; separated not possible so need to do one by one

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
        int i=1;
        for(HashMap<String, String> obj : objList){
            ContentValues values = new ContentValues();
            values.put(DBModel.KEY_orderid, i++);
            // It's a good practice to use parameter ?, instead of concatenate string
            db.update(DBModel.TABLE, values, DBModel.KEY_ID + "= ?", new String[]{String.valueOf(obj.get("id"))});
        }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            db.close(); // Closing database connection
        }
    }





    public int insert(DBModel obj) {

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBModel.KEY_dayid, obj.dayid);
        values.put(DBModel.KEY_filepath, obj.filepath);
        values.put(DBModel.KEY_orderid, obj.dayid);

        // Inserting Row
        long dayinfo_Id = db.insert(DBModel.TABLE, null, values);
        db.close(); // Closing database connection
        return (int) dayinfo_Id;
    }

    public void delete(int dayinfo_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(DBModel.TABLE, DBModel.KEY_ID + "= ?", new String[] { String.valueOf(dayinfo_Id) });
        db.close(); // Closing database connection
    }

    public void update(DBModel obj) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DBModel.KEY_dayid, obj.dayid);
        values.put(DBModel.KEY_filepath, obj.filepath);
        values.put(DBModel.KEY_orderid, obj.dayid);

        // It's a good practice to use parameter ?, instead of concatenate string
        db.update(DBModel.TABLE, values, DBModel.KEY_ID + "= ?", new String[] { String.valueOf(obj.dayinfo_ID) });
        db.close(); // Closing database connection
    }





    public DBModel getById(int Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                DBModel.KEY_ID + "," +
                DBModel.KEY_dayid + "," +
                DBModel.KEY_filepath + "," +
                DBModel.KEY_orderid +
                " FROM " + DBModel.TABLE
                + " WHERE " +
                DBModel.KEY_ID + "=?";// It's a good practice to use parameter ?, instead of concatenate string

        int iCount =0;
        DBModel obj = new DBModel();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );

        if (cursor.moveToFirst()) {
            do {
                obj.dayinfo_ID =cursor.getInt(cursor.getColumnIndex(DBModel.KEY_ID));
                obj.dayid =cursor.getInt(cursor.getColumnIndex(DBModel.KEY_dayid));
                obj.filepath  =cursor.getString(cursor.getColumnIndex(DBModel.KEY_filepath));
                obj.orderid =cursor.getInt(cursor.getColumnIndex(DBModel.KEY_orderid));

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return obj;
    }
}
