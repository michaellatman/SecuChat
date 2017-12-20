package com.secuchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.secuchat.DBObjects.ChatRoomRecord;
import com.secuchat.DBObjects.KeypairRecord;
import com.secuchat.Utils.CryptoHelper;
import com.secuchat.adapters.ChatRoomsViewAdapter;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class MyChatRooms extends Activity {

    Context c = this;
    ArrayList<ChatRoomRecord> chatRoomArray = new ArrayList<ChatRoomRecord>();
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        SecuChatApp.currentActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_chat_rooms);

        refresh();

        Button btnInvite = (Button) findViewById(R.id.btnInvite);
        btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open invite dialog
                Dialog dialog = createDialog();
                dialog.show();

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        SecuChatApp.currentActivity = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        SecuChatApp.currentActivity = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_chat_rooms, menu);
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
            Iterator<ChatRoomRecord> iterator = ChatRoomRecord.findAll(ChatRoomRecord.class);
            while(iterator.hasNext()){
                iterator.next().delete();

            }
            Iterator<KeypairRecord> i = KeypairRecord.findAll(KeypairRecord.class);
            while(i.hasNext()){
                i.next().delete();

            }
            SharedPreferences dataStore = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            dataStore.edit().clear().commit();
            finish();
            System.exit(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayRooms(final ArrayList<ChatRoomRecord> roomIDs)
    {

        // Uses ChatRoomView adapter to populate listview with the chatrooms
        final ListView roomList = (ListView) findViewById(R.id.myChatRoomsList);
        ChatRoomsViewAdapter adapter = new ChatRoomsViewAdapter(c, roomIDs);
        roomList.setAdapter(adapter);
        //set the listener

       roomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               //Chatroom clicked we must enter that ChatAcivity
               Intent i = new Intent(getApplicationContext(), ChatActivity.class);
               i.putExtra("ID",roomIDs.get(position).getUid());
               i.putExtra("new",true);
               startActivity(i);
           }
       });
       roomList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
           @Override
           public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
               // Long held. Ask if they want to delete chatroom
               AlertDialog.Builder dialog = new AlertDialog.Builder(MyChatRooms.this);
               dialog.setTitle("Delete this chatroom ?");
               dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       ChatRoomRecord.deleteAll(ChatRoomRecord.class, "uid = ?", roomIDs.get(position).getUid());
                       //Delete this chatroom.. refresh to show changes to local database.
                       refresh();
                   }
               });

               dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                   }
               });
               dialog.create();
               dialog.show();
               return true;
           }
       });


    }
    public void refresh(){
        Iterator<ChatRoomRecord> iterator = ChatRoomRecord.findAll(ChatRoomRecord.class);
        //Get all chatrooms. Package in array pass to the displayer
        chatRoomArray = new ArrayList<ChatRoomRecord>();
        while (iterator.hasNext())
        {
            chatRoomArray.add(iterator.next());
        }

        displayRooms(chatRoomArray);
    }

    public void createChatRoom(final String label, final String receiverNickname, boolean isKeyIn)
    {
        if (isKeyIn)
        {
            //create room
            SecuChatApp.getFirebaseMainRef().child("users/"+receiverNickname).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if(snapshot.getValue() != null) {
                        ChatRoomRecord room = new ChatRoomRecord();
                        if (label.length() != 0) {
                            room.setLabel(label);
                        }
                        room.save();
                        //add invite in users invite
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        Firebase inviteRef = SecuChatApp.getFirebaseMainRef().child("invites").child(receiverNickname).push();
                        //add room in display
                        refresh();
                        //add invite in firebase invite

                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("uid", room.getUid());
                        map.put("aesKey",  Base64.encodeToString(room.getAesKey(),Base64.CRLF));
                        map.put("label", room.getLabel());
                        map.put("sender",prefs.getString("nickname", ""));
                        JSONObject jsonInvite = new JSONObject(map);
                        Log.d("InviteString:", Base64.encodeToString(jsonInvite.toString().getBytes(), Base64.CRLF));
                        try {

                            byte[] result = CryptoHelper.RSAEncrypt(jsonInvite.toString(), snapshot.getValue(String.class));
                            inviteRef.setValue(Base64.encodeToString(result, Base64.CRLF));
                            Log.d("InviteStringEnc:", Base64.encodeToString(result, Base64.CRLF));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

        }
    }
        /*
        * Code reference:
        * http://stackoverflow.com/questions/2115758/how-to-display-alert-dialog-in-android
        *
        * */
       public Dialog createDialog() {
           AlertDialog.Builder builder = new AlertDialog.Builder(this);
           final LayoutInflater inflater = this.getLayoutInflater();
           final View v = inflater.inflate(R.layout.chatroom_invite_popup,null);

           builder.setView(v)
                   .setPositiveButton(R.string.popup_ok_btn, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int id) {
                           final EditText et = (EditText) v.findViewById(R.id.room_name);
                           final EditText et1 = (EditText) v.findViewById(R.id.contact_field);
                           String label = et.getText().toString();
                           final String key = et1.getText().toString();
                           boolean isKeyIn;
                           if (key.length() != 0) {
                               isKeyIn = true;
                               createChatRoom(label,key, isKeyIn);
                               dialog.dismiss();
                           }
                       }
                   })
                   .setNegativeButton(R.string.popup_cancel_btn, new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           dialog.dismiss();
                       }
                   });
           builder.setTitle(R.string.title_chatroom_invite_popup);
           return builder.create();
       }

   }
