package com.secuchat;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by ArnoldB on 11/11/2014.
 */
//Makes invite object ready for firebase.
public class Invite {

    String chatId;
    String aesKey;
    String label;
    String sender;

    public Invite(String jsonObj)
    {
        try {
            HashMap<String, Object> roomInfo = new ObjectMapper().readValue(jsonObj, HashMap.class);
            this.chatId = (String)roomInfo.get("uid");
            this.aesKey = (String)roomInfo.get("aesKey");
            this.label = (String)roomInfo.get("label");
            this.sender = (String)roomInfo.get("sender");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getChatId() {
        return chatId;
    }

    public String getAesKey() {
        return aesKey;
    }

    public String getLabel() {
        return label;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
