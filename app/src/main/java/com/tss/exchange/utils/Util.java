package com.tss.exchange.utils;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Base64;

import com.tss.exchange.configulations.TssSharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Util {

    private static final String KEY_UUID = "KEY_UUID";

    public static String getUUID() {
        SharedPreferences sharedPreferences = TssSharedPreferences.getSharedpreferences();
        String uuid = sharedPreferences.getString(KEY_UUID, "");
        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = TssSharedPreferences.getSharedpreferences().edit();
            editor.putString(KEY_UUID, uuid);
            editor.apply();
        }
        return uuid;
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int height = bmpOriginal.getHeight();
        int width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.0F);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0.0F, 0.0F, paint);
        return bmpGrayscale;
    }

    public static String toOneLinePrint(String one, String two, String three, String four) {
        StringBuilder result = new StringBuilder();
        result.append(one);
        for (int s = one.length();s<4;s++) {
            result.append("_");
        }

        for (int s = two.length();s<7;s++) {
            result.append("_");
        }
        result.append(two);

        for (int s = three.length();s<7;s++) {
            result.append("_");
        }
        result.append(three);

        for (int s = four.length();s<10;s++) {
            result.append("_");
        }
        result.append(four);

        return result.toString();
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    public static String toDateFormat(String data) {
        String myFormat = "dd-MMM-yy";
        String finalString = "";
        try {
            DateFormat formatter = new SimpleDateFormat("yyMMdd");
            Date date = formatter.parse(data);
            SimpleDateFormat newFormat = new SimpleDateFormat(myFormat);
            finalString = newFormat.format(date);
            return finalString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}
