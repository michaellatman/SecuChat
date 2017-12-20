package com.secuchat.Utils;

import android.util.Base64;
import android.util.Log;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by michael on 10/23/14.
 *
 * Code reference in StackOverflow
 * http://stackoverflow.com/questions/12471999/rsa-encryption-decryption-in-android
 */
public class CryptoHelper {
    public static byte[] getNewAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // for example
        SecretKey secretKey = keyGen.generateKey();
        return secretKey.getEncoded();
    }
    public static String generateSalt(){
        return new BigInteger(130, new SecureRandom()).toString(32);
    }


    public static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        Log.d("AES ", new String(raw));
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    public static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static byte[] RSAEncrypt(String rawString, String key) throws  Exception
    {
        Cipher cipher = Cipher.getInstance("RSA");
        byte[] pubKeyBytes = Base64.decode(key, Base64.CRLF);
        PublicKey publicKey =
                KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKeyBytes));

        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        byte[] encryptedBytes = cipher.doFinal(rawString.getBytes());
        return encryptedBytes;
    }

    public static byte[] RSADecrypt(String rawString, String key) throws  Exception
    {
        Cipher cipher = Cipher.getInstance("RSA");
        byte[] pubKeyBytes = Base64.decode(key, Base64.CRLF);
        PublicKey publicKey =
                KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKeyBytes));

        cipher.init(Cipher.DECRYPT_MODE,publicKey);
        byte[] encryptedBytes = cipher.doFinal(rawString.getBytes());
        return encryptedBytes;
    }
    public static byte[] RSADecryptPrivate(byte[] rawString, String key) throws  Exception
    {
        Cipher cipher = Cipher.getInstance("RSA");
        byte[] pubKeyBytes = Base64.decode(key, Base64.CRLF);
        PrivateKey privateKey =
                KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pubKeyBytes));

        cipher.init(Cipher.DECRYPT_MODE,privateKey);
        byte[] encryptedBytes = cipher.doFinal(rawString);
        return encryptedBytes;
    }
}
