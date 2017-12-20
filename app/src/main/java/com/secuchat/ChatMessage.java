package com.secuchat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;

/**
 * Created by ArnoldB on 10/17/2014.
 */
public class ChatMessage {
    // Object that handles making objects ready for firebase
    private String msg,author;

    public ChatMessage(String stringJObj)
    {

        try {
            /*
            * Code reference:
            * http://stackoverflow.com/questions/21544973/convert-jsonobject-to-map
            * */
            HashMap<String, Object> msgMap = new ObjectMapper().readValue(stringJObj, HashMap.class);
            this.author = (String) msgMap.get("author");
            this.msg = (String) msgMap.get("message");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ChatMessage(String author, String msg) {
        super();
        this.msg = msg;
        this.author = author;
    }

    public String getRaw() {
        return msg;
    }
    public String getAuthor() {
        return author;
    }

}
