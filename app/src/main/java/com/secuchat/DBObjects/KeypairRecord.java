package com.secuchat.DBObjects;

import android.util.Base64;
import com.orm.SugarRecord;

/**
 * Created by michael on 10/25/14.
 */
public class KeypairRecord extends SugarRecord<KeypairRecord> {
    //Allows for saving and retrieving Keypairs from the internal DB
    String publicKey;
    String privateKey;
    boolean primaryKeyPair = false;
    public KeypairRecord(){

    }

    public Boolean getPrimary() {
        return primaryKeyPair;
    }

    public void setPrimary(Boolean primary) {
        this.primaryKeyPair = primary;
    }

    public byte[] getPublicKey() {

        return Base64.decode(publicKey,Base64.CRLF);
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = Base64.encodeToString(publicKey,Base64.CRLF);
    }

    public byte[] getPrivateKey() {
        return Base64.decode(privateKey,Base64.CRLF);
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = Base64.encodeToString(privateKey,Base64.CRLF);
    }

}
