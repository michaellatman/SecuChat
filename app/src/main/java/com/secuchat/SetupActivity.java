package com.secuchat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.firebase.client.Firebase;
import com.secuchat.DBObjects.ChatRoomRecord;
import com.secuchat.DBObjects.KeypairRecord;
import com.secuchat.Utils.KeyTaskCompletionListener;


public class SetupActivity extends Activity {

    KeypairRecord keypair;
    final SharedPreferences dataStore = null;
    boolean userExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Firebase.setAndroidContext(getApplicationContext());



        if(ChatRoomRecord.count(ChatRoomRecord.class,"",null)==0) {
            //We don't have a chatroom.. Let's make one and use that forever.
            //For test this is a public chat everyone knows the key.
            ChatRoomRecord chatroom = new ChatRoomRecord();
            chatroom.setUid("loltest");
            chatroom.setAesKey("rF+mnaAOgMT5X/zO6M9aFZVnpxxuKZxAOgwOLK4qhNo=");
            chatroom.save();
        }
        final Button btnContinue = (Button) findViewById(R.id.btnContinue);
        final EditText nicknameField = (EditText) findViewById(R.id.nicknameField);
        //create the keys
        Log.d("Record", ""+KeypairRecord.count(KeypairRecord.class, "", null));
        //Will generate RSA keys if there are none saved and go to ChatRooms
        final SharedPreferences dataStore = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(KeypairRecord.count(KeypairRecord.class,"",null)==0||!dataStore.contains("nickname")) {
            new BackgroundKeyGeneratorTask(this, new KeyTaskCompletionListener() {
                @Override
                public void finished(byte[] publicKey, byte[] privateKey) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Wait till generated.
                            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                            progressBar.setVisibility(View.GONE);
                            TextView generatingText = (TextView) findViewById(R.id.generatingText);
                            generatingText.setVisibility(View.GONE);
                            ImageView checkMark = (ImageView) findViewById(R.id.imageView);
                            checkMark.setVisibility(View.VISIBLE);
                            Button btnContinue = (Button) findViewById(R.id.btnContinue);
                            btnContinue.setVisibility(View.VISIBLE);
                        }
                    });

                    if (KeypairRecord.count(KeypairRecord.class, "", null) == 0){
                        keypair = new KeypairRecord();
                        keypair.setPublicKey(publicKey);
                        keypair.setPrivateKey(privateKey);
                        keypair.setPrimary(true);
                        keypair.save();
                        //Save the setup


                        Log.d("Setup", "Finished!");

                    }
                }
            }).execute("");


            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SharedPreferences.Editor editor = dataStore.edit();

                    if(nicknameField.getText().toString().trim().length()>0){
                        editor.putString("nickname", nicknameField.getText().toString());
                        //TODO:Put pubkey in the prefs or KeypairRecord
                        editor.putString("pubKey",Base64.encodeToString(keypair.getPublicKey(), Base64.CRLF));
                        editor.putString("privKey", Base64.encodeToString(keypair.getPrivateKey(),Base64.CRLF));
                        editor.commit(); // Save data to phone

                        Firebase ref = SecuChatApp.getFirebaseMainRef();
                        ref.child("users").child(dataStore.getString("nickname","")).setValue(Base64.encodeToString(keypair.getPublicKey(), Base64.CRLF));
                        Intent i = new Intent(getApplicationContext(), MyChatRooms.class);
                        startActivity(i);
                        //End this activity. Move to the main one. We will skip screen everytime after inital setup.
                    }
                    else{
                        nicknameField.setError("Please enter a nickname!");
                    }



                }
            });

        }
        //If there are keys, then only MyChatRooms is started
        else if(dataStore.contains("nickname")){
            startActivity(new Intent(getApplicationContext(), MyChatRooms.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_setup, menu);
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


