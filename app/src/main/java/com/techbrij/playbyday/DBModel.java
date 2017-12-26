package com.techbrij.playbyday;

/**
 * Created by Brij on 4/25/2015.
 */
public class DBModel {
    // Labels table name
    public static final String TABLE = "tblDayInfo";

    // Labels Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_dayid = "dayid";
    public static final String KEY_filepath = "filepath";
    public static final String KEY_orderid = "orderid";

    // property help us to keep data
    public int dayinfo_ID;
    public int dayid, orderid;
    public String filepath;
}
