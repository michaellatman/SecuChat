package com.secuchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.orm.SugarApp;
import com.secuchat.DBObjects.ChatRoomRecord;
import com.secuchat.Utils.CryptoHelper;

import java.util.Arrays;

/**
 * Created by ArnoldB on 10/22/2014.
 */
public class SecuChatApp extends SugarApp {

    static Firebase firebaseMainRef;

    public static Firebase getFirebaseMainRef() {
        return firebaseMainRef;
    }
    public static Activity currentActivity = null;
    @Override
    public void onCreate() {
        Firebase.setAndroidContext(this);
        firebaseMainRef = new Firebase("https://secuchat.firebaseio.com");
        super.onCreate();


        final SharedPreferences dataStore = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String nickname = dataStore.getString("nickname", "");
        //Listen for invites everywhere
        final Firebase ref = SecuChatApp.getFirebaseMainRef().child("invites").child(nickname);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                String senderNickname = dataSnapshot.getKey();
                if(currentActivity!=null) {
                    String encJsonInviteString = dataSnapshot.getValue().toString();
                    byte[] decJsonInvite = null;
                    try {
                        decJsonInvite = CryptoHelper.RSADecryptPrivate(Base64.decode(encJsonInviteString,Base64.CRLF), dataStore.getString("privKey", ""));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(decJsonInvite != null) {
                        Log.d("Invite", new String(decJsonInvite));

                        final Invite invite = new Invite(new String(decJsonInvite));
                        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
                        builder.setMessage("Chat Invite by " + invite.getSender() + " !")
                                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //Add the chatroom we were invited to. To the local DB.
                                                ChatRoomRecord room = new ChatRoomRecord(invite.getChatId(), invite.getAesKey(), invite.getLabel());
                                                room.save();

                                                if (currentActivity instanceof MyChatRooms) {
                                                    MyChatRooms chatRoomInstance = (MyChatRooms) currentActivity;
                                                    chatRoomInstance.refresh();
                                                }
                                                dataSnapshot.getRef().setValue(null);
                                            }

                                        }
                                ).setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                dataSnapshot.getRef().setValue(null);
                            }
                        });
                        builder.setTitle("New Invitation");
                        builder.show();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
}