package edu.stanford.mobisocial.hive.model;
import android.database.Cursor;


public class Hive {
    public static final String TABLE = "hives";

    public static final String _ID = "_id";
    public static final String NAME = "name";
    public static final String LAST_UPDATED = "last_updated";
    public static final String LAST_CHECKED = "last_checked";
    public static final String DB_FEED = "db_feed";

    public final Long id;
    public final String name;
    public final String feedName;

    public Hive(Cursor c){
        id = c.getLong(c.getColumnIndexOrThrow(_ID));
        name = c.getString(c.getColumnIndexOrThrow(NAME));
        feedName = c.getString(c.getColumnIndexOrThrow(DB_FEED));
    }
}