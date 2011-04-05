package edu.stanford.mobisocial.hive;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.net.Uri;
import android.os.Bundle;
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
import org.json.JSONException;
import org.json.JSONObject;

public class ChatActivity extends ListActivity implements OnItemClickListener{

	private ChatCursorAdapter mMessages;
	private NotificationManager mNotificationManager;
	private Cursor mCursor;
	private DBHelper mDb;
    private String mFeedName;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mDb = new DBHelper(this);
        setContentView(R.layout.chat);
		Intent intent = getIntent();
        mFeedName = intent.getStringExtra("feedName");
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mCursor = BeetleAPI.getFeed(this, mFeedName, "chat");
		mMessages = new ChatCursorAdapter(this, mCursor);
		setListAdapter(mMessages);
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        registerForContextMenu(lv);
		lv.setOnItemClickListener(this);
		Button button = (Button)findViewById(R.id.add_msg_button);
		button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ChatActivity.this);
                    alert.setMessage("Message:");
                    final EditText input = new EditText(ChatActivity.this);
                    alert.setView(input);
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String msg = input.getText().toString();
                                mCursor.requery();
                                try{
                                    JSONObject obj = new JSONObject();
                                    obj.put("type", "chat");
                                    obj.put("text", msg);

                                    BeetleAPI.addToFeed(
                                        ChatActivity.this, 
                                        mFeedName,
                                        obj);
                                }
                                catch(JSONException e){}
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
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Menu");
        menu.add(Menu.NONE, 0, 0, "Delete");
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        Cursor cursor = (Cursor)mMessages.getItem(position);
    }


    private class ChatCursorAdapter extends CursorAdapter {
        public ChatCursorAdapter (Context context, Cursor c) {
            super(context, c);
        }
        @Override
        public View newView(Context context, Cursor c, ViewGroup parent){
            final LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.chat_item, parent, false);
            bindView(v, context, c);
            return v;
        }
        @Override
        public void bindView(View v, Context context, Cursor c) {
            try{
                JSONObject chat = new JSONObject(c.getString(c.getColumnIndexOrThrow(BeetleAPI.OBJ_JSON)));
                String name = chat.optString("text");
                TextView textView = (TextView) v.findViewById(R.id.text);
                textView.setText(name);
            }
            catch(JSONException e){}
        }
    }


    @Override
    public void finish() {
        super.finish();
        mDb.close();
    }


}
