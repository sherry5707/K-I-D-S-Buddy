package com.kinstalk.her.qchatcomm.utils;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by liulinxiang on 4/7/2018.
 */

public class StringEncryption {
    static String TAG = "StringEncryption";
    private static final String HW_APP_ID = "szjywxapp";
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    private static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    public static String generateToken(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String cur_date = sdf.format(calendar.getTime());
        Log.d(TAG, "generateToken: cur_date "+ cur_date);

        try {
            String sha1String = SHA1(HW_APP_ID +"+"+ cur_date);
            Log.d(TAG, "generateToken: sha1 "+sha1String);

            String sha1StrWithDate = sha1String + "|" + cur_date;
            Log.d(TAG, "generateToken: sha1StrWitDate "+sha1StrWithDate);

            String token = Base64.encodeToString(sha1StrWithDate.getBytes(), Base64.DEFAULT);

            Log.d(TAG, "generateToken: token "+token);

            return token;

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
