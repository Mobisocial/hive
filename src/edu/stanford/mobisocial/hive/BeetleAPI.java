package edu.stanford.mobisocial.hive;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import java.util.Collection;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class BeetleAPI{

    public static final String AUTHORITY = 
        "edu.stanford.mobisocial.dungbeetle.DungBeetleContentProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String INTENT_ACTION_INVITE = 
        "edu.stanford.mobisocial.dungbeetle.INVITE";

    public static final String OBJ_TYPE = "type";
    public static final String OBJ_JSON = "json";
    public static final String OBJ_DESTINATION = "destination";
    public static final String INVITE_OBJ_TYPE = "invite";
    public static final String SUBSCRIBE_REQ_OBJ_TYPE = "subscribe_req";

    public static final String SUBSCRIBER_CONTACT_ID = "contact_id";
    public static final String SUBSCRIBER_FEED_NAME = "feed_name";
    
    public static final long CONTACT_MY_ID = -1;

    public static Intent inviteIntent(String feedName, String appPackage){
        Intent i = new Intent();
        i.setAction(INTENT_ACTION_INVITE);
        i.putExtra("feedName", feedName);
        i.putExtra("packageName", appPackage);
        return i;
    }

    public static Cursor getFeed(Context context, String feedName, String type){
        return context.getContentResolver().query(
            Uri.parse(CONTENT_URI + "/feeds/" + feedName),
            null, 
            OBJ_TYPE + "=?", new String[]{ type },
            "timestamp ASC");
    }

    public static Uri addToFeed(Context context, String feedName, JSONObject obj){
        Uri url = Uri.parse(CONTENT_URI + "/feeds/" + feedName);
        ContentValues values = new ContentValues();
        values.put(OBJ_JSON, obj.toString());
        values.put(OBJ_TYPE, obj.optString(OBJ_TYPE));
        return context.getContentResolver().insert(url, values); 
    }


    public static Uri addSubscriber(Context c, 
                                    long contactId, 
                                    String feedName){
        ContentValues values = new ContentValues();
        values.put(SUBSCRIBER_CONTACT_ID, contactId);
        values.put(SUBSCRIBER_FEED_NAME, feedName);
        Uri url = Uri.parse(CONTENT_URI + "/subscribers");
        return c.getContentResolver().insert(url, values);
    }


    public static void sendSubscribeRequests(Context c, 
                                             Collection<Long> contactIds, 
                                             String feedName){
        Uri url = Uri.parse(CONTENT_URI + "/out");
        ContentValues values = new ContentValues();
        JSONObject obj = new JSONObject();
        try{
            obj.put("feedName", feedName);
        }catch(JSONException e){}
        values.put(OBJ_JSON, obj.toString());
        values.put(OBJ_DESTINATION, buildAddresses(contactIds));
        values.put(OBJ_TYPE, SUBSCRIBE_REQ_OBJ_TYPE);
        c.getContentResolver().insert(url, values);
    }

    private static String buildAddresses(Collection<Long> contactIds){
        String to = "";
        Iterator<Long> it = contactIds.iterator();
        while(it.hasNext()){
            Long c = it.next();
            if(it.hasNext()){
                to += c + ",";
            }
            else{
                to += c;
            }
        }
        return to;
    }


}