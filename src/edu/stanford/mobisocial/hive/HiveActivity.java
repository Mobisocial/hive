package edu.stanford.mobisocial.hive;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import edu.stanford.mobisocial.hive.model.Hive;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;

public class HiveActivity extends ListActivity implements OnItemClickListener{

	private HiveCursorAdapter mHives;
	private NotificationManager mNotificationManager;
	private SQLiteCursor mCursor;
	private DBHelper mDb;
    public static final String TAG = "HiveActivity";


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mDb = new DBHelper(this);
        setContentView(R.layout.hives);
		Intent intent = getIntent();
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mCursor = (SQLiteCursor)mDb.getReadableDatabase().query(
            Hive.TABLE, 
            new String[]{
                Hive._ID,
                Hive.NAME,
                Hive.LAST_UPDATED,
                Hive.LAST_CHECKED,
                Hive.DB_FEED},
            null, null, 
            null, null, 
            null);
		mHives = new HiveCursorAdapter(this, mCursor);
		setListAdapter(mHives);
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        registerForContextMenu(lv);
		lv.setOnItemClickListener(this);

		Button button = (Button)findViewById(R.id.add_hive_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(HiveActivity.this);
                    alert.setMessage("Topic of chat:");
                    final EditText input = new EditText(HiveActivity.this);
                    alert.setView(input);
                    alert.setPositiveButton("Pick Contacts", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String hiveName = input.getText().toString();
                                if(hiveName.length() > 0){
                                    String feedName = "feed" + Math.random();
                                    mDb.insertHive(hiveName, feedName);
                                    mCursor.requery();
                                    HiveActivity.this.startActivity(
                                        BeetleAPI.inviteIntent(
                                            feedName, 
                                            "edu.stanford.mobisocial.hive"));
                                }
                            }
                        });

                    alert.setNegativeButton(
                        "Cancel", 
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });
                    alert.show();
				}
			});

        if(intent != null && intent.getStringExtra("type") != null && 
           intent.getStringExtra("type").equals("invite_app_feed")){
            handleInviteIntent(intent);
        }
    }

    private void handleInviteIntent(Intent intent){
        Log.i(TAG, "Processing intent " + intent);
        long contactId = intent.getLongExtra("sender", -1);
        String feedName = intent.getStringExtra("sharedFeedName");
        long[] participants = intent.getLongArrayExtra("participants");
        BeetleAPI.addSubscriber(this, contactId, feedName);
        List<Long> parts = new ArrayList<Long>();
        for(int i = 0; i < participants.length; i++) {
            Log.i(TAG, "Subscribing to " + participants[i]);
            parts.add(participants[i]);
        }
        BeetleAPI.sendSubscribeRequests(this, parts, feedName);
        mDb.insertHive(feedName, feedName);
        mCursor.requery();
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        menu.setHeaderTitle("Menu");
        menu.add(Menu.NONE, 0, 0, "Delete");
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        Cursor cursor = (Cursor)mHives.getItem(position);
        final Hive c = new Hive(cursor);
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("feedName", c.feedName);
        startActivity(i);
    }


    private class HiveCursorAdapter extends CursorAdapter {
        public HiveCursorAdapter (Context context, Cursor c) {
            super(context, c);
        }
        @Override
        public View newView(Context context, Cursor c, ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.hives_item, parent, false);
            bindView(v, context, c);
            return v;
        }
        @Override
        public void bindView(View v, Context context, Cursor c) {
            String name = c.getString(c.getColumnIndexOrThrow(Hive.NAME));
            TextView nameText = (TextView) v.findViewById(R.id.name_text);
            nameText.setText(name);
        }
    }

    @Override
    public void finish() {
        super.finish();
        mDb.close();
    }


}
