package com.secuchat.adapters;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.Date;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.secuchat.ChatActivity;
import com.secuchat.DBObjects.ChatRoomRecord;
import com.secuchat.R;
import com.secuchat.ChatMessage;

/**
 * Created by ArnoldB on 10/17/2014.
 */
public class ChatViewAdapter extends ArrayAdapter<ChatMessage> {

    private final Context context;
    private final ArrayList<ChatMessage> messageArray;
    private ListView listView;
    ChatRoomRecord chatRoom;
    long startTime;
    MediaPlayer mMediaPlayer;
    String nickname = "";
    public ChatViewAdapter(Context context, ChatRoomRecord chatRoom, ListView listView, int layoutResourceId, ArrayList<ChatMessage> messageArray) {

        super(context, layoutResourceId, messageArray);
        startTime = new Date().getTime();
        this.context = context;
        this.messageArray = messageArray;
        this.chatRoom = chatRoom;
        this.listView = listView;
        SharedPreferences dataStore = PreferenceManager.getDefaultSharedPreferences(context);
        nickname = dataStore.getString("nickname", "");
        //To play sounds
         mMediaPlayer = MediaPlayer.create(context, R.raw.pop);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(false);
        mMediaPlayer.start();
        chatRoom.getRef().limitToLast(50).addChildEventListener(messageListener);
    }
    public void setListView(ListView lv){
        this.listView = lv;
    }
    ChildEventListener messageListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            // New message
            String stringJObj = null;
            try {
                stringJObj = chatRoom.stringDecrypt(dataSnapshot.getValue().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            ChatMessage msg = new ChatMessage(stringJObj);
            messageArray.add(msg);
            ChatViewAdapter.this.notifyDataSetChanged();
            ChatActivity activity = (ChatActivity) context;
            if(activity.paused) {
                //if paused.. Show notification
                Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Notification.Builder mBuilder =
                        new Notification.Builder(context)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("New Message!")
                                .setAutoCancel(true)
                                .setVibrate(new long[] { 1000})
                                .setSound(defaultRingtoneUri)
                                .setContentText("New message from " + msg.getAuthor());
                Intent resultIntent = new Intent(context, ChatActivity.class);
                resultIntent.putExtra("ID",chatRoom.getUid());

                mBuilder.setContentIntent(PendingIntent.getActivity(context, 22, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));

                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                mNotificationManager.notify(2, mBuilder.getNotification());
            }
            else if(new Date().getTime()-startTime>2000){

                mMediaPlayer.start();
            }
            final Button btn = (Button) activity.findViewById(R.id.unreadButton);
            if(getCount()-listView.getLastVisiblePosition()<5||new Date().getTime()-startTime<2000) {
                // Logic to handle smart scrolling
                if(new Date().getTime()-startTime>2000)listView.smoothScrollToPosition(messageArray.size());
                else listView.smoothScrollToPositionFromTop(messageArray.size(),messageArray.size()-10);
                btn.setVisibility(View.GONE);
            }
            else{
                //If scrolled to the top don't autoscroll.. Show button to jump to bottom
                btn.setVisibility(View.VISIBLE);
                btn.setText("New Messages");
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listView.smoothScrollToPosition(messageArray.size());
                        btn.setVisibility(View.GONE);
                    }
                });
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView;
        if(nickname.equals(messageArray.get(position).getAuthor())) rowView = inflater.inflate(R.layout.chat_bubble_right, parent, false);
        else rowView = inflater.inflate(R.layout.chat_bubble_left, parent, false);
        // 3. Get the two text view from the rowView
        TextView user = (TextView) rowView.findViewById(R.id.user);
        TextView message = (TextView) rowView.findViewById(R.id.secondLine);

        // 4. Set the text for textView
        if (messageArray.size() > 0) {
            user.setText(messageArray.get(position).getAuthor());
            message.setText(messageArray.get(position).getRaw());
        }

        // 5. return rowView
        return rowView;
    }
}
