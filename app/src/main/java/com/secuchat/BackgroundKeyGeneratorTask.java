package com.secuchat;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Base64;
import com.secuchat.Utils.KeyTaskCompletionListener;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

/**
 * Created by ArnoldB on 10/15/2014.
 */
public class BackgroundKeyGeneratorTask extends AsyncTask<String, Void, String> {
    // Background task that generates public/private key without locking the UI thread.
    private PublicKey pubKey;
    private PrivateKey privKey;
    private Activity c;
    KeyTaskCompletionListener onComplete;
    public BackgroundKeyGeneratorTask(Activity context, KeyTaskCompletionListener onComplete) {
        super();
        c = context;
        this.onComplete = onComplete;
    }


    @Override
    protected String doInBackground(String... params) {

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048,new SecureRandom());
            KeyPair pair = generator.generateKeyPair();
            pubKey = pair.getPublic();
            privKey = pair.getPrivate();
            String pubKeyString = new String(Base64.encode(pubKey.getEncoded(), Base64.CRLF));
            String privKeyString = new String(Base64.encode(privKey.getEncoded(), Base64.CRLF));
            if(onComplete!=null) onComplete.finished(pubKey.getEncoded(),privKey.getEncoded());
            //Generate and  return.
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
















}
