package com.secuchat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.secuchat.DBObjects.ChatRoomRecord;
import com.secuchat.Utils.ResizeListener;
import com.secuchat.Utils.ResizeView;
import com.secuchat.adapters.ChatViewAdapter;
import java.util.ArrayList;
import java.util.List;


public class ChatActivity extends Activity {

    ArrayList<ChatMessage> messageArray = new ArrayList<ChatMessage>();

    ChatRoomRecord selectedRoom;
    public static Boolean paused = false;
    static ChatViewAdapter chatViewListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SecuChatApp.currentActivity = this;
       // setActionBar(toolbar);
        paused=false;
        //Get the chatroom
        Intent i = getIntent();
        setContentView(R.layout.activity_chat);
        String roomId = i.getStringExtra("ID"); // Our room ID to open
        Log.d("Chatroom","Entering "+roomId);
        //Find the ROOM
        List<ChatRoomRecord> id = ChatRoomRecord.find(ChatRoomRecord.class, "uid = ?",roomId);//do a query on the db for the item using the id
        selectedRoom = id.get(0); //get the only item in the List (since all Uid will be unique)
        setTitle(selectedRoom.getLabel()); // Set title
        getActionBar().setSubtitle("2 participants");//Static for now
        FrameLayout frame = new FrameLayout(this);

        Button button = new Button(this); // Create unread button
        button.setId(R.id.unreadButton);
        button.setVisibility(View.GONE);
        FrameLayout.LayoutParams params =  new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT|Gravity.BOTTOM);
        params.setMargins(0,0,0,200);
        frame.addView(button,params);

        addContentView(frame, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ResizeView view = (ResizeView) findViewById(R.id.rootView);

                final TextView msgBox = (TextView) findViewById(R.id.editText);
        final ListView lv = (ListView) findViewById(R.id.listView);

        if(chatViewListAdapter == null||i.getBooleanExtra("new",false)) chatViewListAdapter = new ChatViewAdapter(this, selectedRoom, lv, R.layout.chat_bubble_left, messageArray);
        chatViewListAdapter.setListView(lv);
        lv.setAdapter(chatViewListAdapter);
        //clear the textbox
        //Use our custom views onsizechange
        view.setSizeListener(new ResizeListener() {
            @Override
            public void viewSizeChanged(int w, int h, int oldw, int oldh) {
                Log.d("ScrollListener", "Scroll!");
                lv.clearFocus();
                lv.post(new Runnable() {
                    @Override
                    public void run() {
                        //Scroll if keyboard opened
                        lv.setSelection(chatViewListAdapter.getCount());
                    }
                });
            }
        });
        //send button event
        Button btnSend = (Button) findViewById(R.id.button);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send message!
                SharedPreferences dataStore = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                ChatMessage msg = new ChatMessage(dataStore.getString("nickname", ""), msgBox.getText().toString());
                selectedRoom.sendMessage(msg);
                msgBox.setText("");

            }
        });

    }



    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
        SecuChatApp.currentActivity = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        SecuChatApp.currentActivity = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}




