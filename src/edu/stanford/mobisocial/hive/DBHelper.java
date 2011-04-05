package edu.stanford.mobisocial.hive;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;
import edu.stanford.mobisocial.hive.model.Hive;
import edu.stanford.mobisocial.hive.util.Util;
import java.util.Arrays;
import java.util.Date;


public class DBHelper extends SQLiteOpenHelper {
	public static final String TAG = "DBHelper";
	public static final String DB_NAME = "HIVE_DB";
	public static final int VERSION = 1;
    private final Context mContext;

	public DBHelper(Context context) {
		super(
		    context, 
		    DB_NAME, 
		    new SQLiteDatabase.CursorFactory() {
		    	@Override
		    	public Cursor newCursor(
                    SQLiteDatabase db, 
                    SQLiteCursorDriver masterQuery, 
                    String editTable, 
                    SQLiteQuery query) {
		    		return new SQLiteCursor(db, masterQuery, editTable, query);
		    	}
		    }, 
		    VERSION);
        mContext = context;
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
        // enable locking so we can safely share 
        // this instance around
        db.setLockingEnabled(true);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
              + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + Hive.TABLE);
        onCreate(db);
    }

    private void createTable(SQLiteDatabase db, String tableName, String... cols){
        assert cols.length % 2 == 0;
        String s = "CREATE TABLE " + tableName + " (";
        for(int i = 0; i < cols.length; i += 2){
            s += cols[i] + " " + cols[i + 1];
            if(i < (cols.length - 2)){
                s += ", ";
            }
            else{
                s += " ";
            }
        }
        s += ")";
        Log.i(TAG, s);
        db.execSQL(s);
    }

    private void createIndex(SQLiteDatabase db, 
                             String type, 
                             String name, String tableName, String col){
        String s = "CREATE " + type + " " + name + " on " + tableName + " (" + col + ")";
        Log.i(TAG, s);
        db.execSQL(s);
    } 

    
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();

        createTable(db, Hive.TABLE,
                    Hive._ID, "INTEGER PRIMARY KEY",
                    Hive.NAME, "TEXT",
                    Hive.LAST_UPDATED, "INTEGER",
                    Hive.LAST_CHECKED, "INTEGER",
                    Hive.DB_FEED, "STRING");

        db.setVersion(VERSION);
        db.setTransactionSuccessful();
        db.endTransaction();
        this.onOpen(db);
	}


    long insertHive(String hiveName, String dbFeedName) {
    	try{
            ContentValues cv = new ContentValues();
            cv.put(Hive.NAME, hiveName);
            cv.put(Hive.DB_FEED, dbFeedName);
            long t = (new Date()).getTime();
            cv.put(Hive.LAST_CHECKED, t);
            cv.put(Hive.LAST_UPDATED, t);
            return getWritableDatabase().insertOrThrow(Hive.TABLE, null, cv);
    	}
    	catch(Exception e){
    		e.printStackTrace(System.err);
    		return -1;
    	}
    }


    public static String joinWithSpaces(String... strings) {
        return Util.join(Arrays.asList(strings), " ");
    }

    public static String projToStr(String[] strings) {
        if(strings == null) return "*";
        return Util.join(Arrays.asList(strings), ",");
    }

    public static String andClauses(String A, String B) {
        if(A == null && B == null) return "1 = 1";
        if(A == null) return B;
        if(B == null) return A;
        return A + " AND " + B;
    }

    public static String[] concat(String[] A, String[] B) {
        String[] C = new String[A.length + B.length];
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);
        return C;
    }

}
