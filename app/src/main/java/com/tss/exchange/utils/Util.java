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
        if (one.length() == 3) {
            result.append(one);
            result.append(' ');
            result.append(' ');
        } else {
            result.append(one);
        }
        result.append(' ');


        result.append(getSpace(two.length()));
        result.append(two);

        result.append(getSpace3(three.length()));
        result.append(three);

        result.append(getSpace4(four.length()));
        result.append(four);

        return result.toString();
    }

    private static String getSpace(int length) {
        StringBuilder result = new StringBuilder();
        if (length == 7) {
            result.append(' ');

        } else if (length == 6) {
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');

        } else if (length == 5) {
            result.append(' ');
            result.append(' ');
            result.append(' ');


            result.append(' ');

            result.append(' ');
            result.append(' ');
        } else if (length == 4) {
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
        } else if (length == 3) {
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
        } else if (length == 2) {
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
        } else if (length == 1) {
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
        }

        return result.toString();
    }


    private static String getSpace3(int length) {
        StringBuilder result = new StringBuilder();
        if (length == 9) {
            result.append(' ');
        } else if (length == 8) {
            result.append(' ');

        } else if (length == 7) {
            result.append(' ');
            result.append(' ');

            result.append(' ');



            result.append(' ');
        } else if (length == 6) {

            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');

            result.append(' ');

            result.append(' ');
        } else if (length == 5) {
            result.append(' ');
            result.append(' ');

            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
        } else if (length == 4) {
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');

            result.append(' ');
            result.append(' ');
        } else if (length == 3) {

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
        } else if (length == 2) {
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
        } else if (length == 1) {
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
        }

        return result.toString();
    }

    private static String getSpace4(int length) {
        StringBuilder result = new StringBuilder();
        if (length == 9) {
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
        } else if (length == 8) {
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
        } else if (length == 7) {
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
        } else if (length == 6) {

            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');

            result.append(' ');

            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
        } else if (length == 5) {
            result.append(' ');
            result.append(' ');

            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
        } else if (length == 4) {
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
        } else if (length == 3) {

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
        } else if (length == 2) {
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
        } else if (length == 1) {
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');

            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
            result.append(' ');
        }

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
