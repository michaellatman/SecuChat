package com.secuchat.DBObjects;

import android.util.Base64;
import android.util.Log;
import com.firebase.client.Firebase;
import com.orm.SugarRecord;
import com.secuchat.ChatMessage;
import com.secuchat.Utils.CryptoHelper;
import com.secuchat.SecuChatApp;
import org.json.JSONObject;
import java.util.HashMap;

/**
 * Created by michael on 10/19/14.
 */
public class ChatRoomRecord extends SugarRecord<ChatRoomRecord> {
    String uid;
    String aesKey;

    String label = "Untitled";

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ChatRoomRecord(String uid, String aesKey, String label)
    {
        this.uid = uid;
        try {
            this.aesKey = aesKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.label = label;

    }

    public ChatRoomRecord(){
        //Generate UID.
        this.uid = SecuChatApp.getFirebaseMainRef().child("messages").push().getKey();
            try {
                //Get new AES key.
                this.aesKey = Base64.encodeToString(CryptoHelper.getNewAESKey(), Base64.CRLF);
                Log.d("AES-Key", aesKey);
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public byte[] getAesKey() {
        return Base64.decode(aesKey,Base64.CRLF);
    }

    public Firebase getRef(){
        return  SecuChatApp.getFirebaseMainRef().child("messages/" + this.uid);
    }
    public void sendMessage(ChatMessage messageObj){

        //send the message to server
        HashMap<String,Object> msgMap = new HashMap<String, Object>();
        String salt = CryptoHelper.generateSalt(); // for security and repeat attacks.
        try {
            msgMap.put("author", messageObj.getAuthor());
            msgMap.put("message", messageObj.getRaw());
            msgMap.put("salt", salt);
            //Add random bytes so that messages always look different even if content is the same.
            getRef().push().setValue(stringEncrypt(new JSONObject(msgMap).toString())); // Push to db
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public String stringEncrypt(String cleartext) throws Exception {
        byte[] result = CryptoHelper.encrypt(getAesKey(), cleartext.getBytes());
        return Base64.encodeToString(result, Base64.CRLF);
    }
    public String stringDecrypt(String encrypted) throws Exception {
        byte[] enc = Base64.decode(encrypted, Base64.CRLF);
        byte[] result = CryptoHelper.decrypt(getAesKey(), enc);
        return new String(result);
    }


    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }
}
