package com.tss.exchange;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.centerm.centermposoversealib.thailand.AidlIdCardTha;
import com.centerm.centermposoversealib.thailand.AidlIdCardThaListener;
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen;
import com.centerm.smartpos.aidl.iccard.AidlICCard;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.tss.exchange.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class IcCardActivity extends BaseActivity1 {
    private String TAG = getClass().getSimpleName();
    private AidlIdCardTha aidlIdCardTha;
    private AidlICCard aidlIcCard;
    public long time1;
    public static long timestart;
    private boolean aidlReady = false;
    ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private ProgressDialog mLoading;
    private MediaPlayer mediaPlayer;private List<String> months_eng = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    private List<String> months_th = Arrays.asList("ม.ค.", "ก.พ.", "มี.ค.", "เม.ษ.", "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.");
    public static String pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idcard);
        mLoading = new ProgressDialog(this);
        mLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLoading.setCanceledOnTouchOutside(false);
        mLoading.setMessage("Reading...");
        Log.i("C", "Create2");
        bindService();
    }

    @Override
    protected void bindService() {
        super.bindService();
        Intent intent = new Intent();
        intent.setPackage("com.centerm.centermposoverseaservice");
        intent.setAction("com.centerm.CentermPosOverseaService.MANAGER_SERVICE");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }




    @Override
    public void onDeviceConnected(AidlDeviceManager deviceManager) {
        try {
            IBinder device = deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_ICCARD);
            if (device != null) {
                aidlIcCard = AidlICCard.Stub.asInterface(device);
                if (aidlIcCard != null) {
                    Log.e("MY", "IcCard bind success!");
                    //This is the IC card service object!!!!
                    //I am do nothing now and it is not null.
                    //you can do anything by yourselef later.
                    d();
                } else {
                    Log.e("MY", "IcCard bind fail!");
                }
            }
            device = deviceManager.getDevice(com.centerm.centermposoversealib.constant.Constant.OVERSEA_DEVICE_CODE.OVERSEA_DEVICE_TYPE_THAILAND_ID);
            if (device != null) {
                aidlIdCardTha = AidlIdCardTha.Stub.asInterface(device);
                aidlReady = aidlIdCardTha != null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrintDeviceConnected(AidlDeviceManager deviceManager) {

    }


    private boolean stTestContinue = false;
    private long invokStart = 0;
    private int invokCount = 0;
    private long totalTime = 0;

    private void startStabilityTest() throws RemoteException {
        invokStart = System.currentTimeMillis();
        aidlIdCardTha.searchIDCard(60000, test);
    }

    AidlIdCardThaListener test = new AidlIdCardThaListener.Stub() {
        @Override
        public void onFindIDCard(ThiaIdInfoBeen idInfoThaBean) {
            totalTime += (System.currentTimeMillis() - invokStart);
            invokCount++;
            if (stTestContinue) {
                if (checkInfo(idInfoThaBean)) {
                    displayResult("Testing...");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                startStabilityTest();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                                displayResult("Exception...");
                            }
                        }
                    });
                } else {
                    displayResult("Check Info ERROR:");
                }
            } else {
                displayResult("Cancel:");
            }
        }

        @Override
        public void onTimeout() {
            displayResult("Timeout:");
        }

        @Override
        public void onError(int i, String s) {
            displayResult("Error:" + i + " " + s);
        }
    };

    private boolean save = false;
    private String jsonStr;

    private boolean checkInfo(ThiaIdInfoBeen info) {
        if (save) {
            if (jsonStr.equals(info.toJSONString())) {
                return true;
            } else {
                return false;
            }
        } else {
            save = true;
            jsonStr = info.toJSONString();
//            showMsg(jsonFormat(jsonStr));
        }
        return true;
    }


    private String getDateFromJson(String date, String reg) {

        String _day = "" + Integer.parseInt(date.substring(0, 2));
        String _month_eng;
        String _month_th;
        String _year_th;
        String _year_eng;
        String _birth_eng;
        String _birth_th;
        if (reg.equals("en")) {
            _month_eng = months_eng.get(Integer.parseInt(date.substring(2, 4)) - 1);
            _year_eng = "" + (Integer.parseInt(date.substring(4, 8)) - 543);
            _year_eng = _year_eng.substring(0, 2) + "XX";
            _birth_eng = _day + " " + _month_eng + " " + _year_eng;
            return _birth_eng;
        } else if (reg.equals("th")) {
            _month_th = months_th.get(Integer.parseInt(date.substring(2, 4)) - 1);
            _year_th = date.substring(4, 8);
            _year_th = _year_th.substring(0, 2) + "XX";
            _birth_th = _day + " " + _month_th + " " + _year_th;
            return _birth_th;
        }

        return "";
    }

    private String jsonFormat(String s) {
        int level = 0;
        StringBuffer jsonForMatStr = new StringBuffer();
        for (int index = 0; index < s.length(); index++) {
            //获取s中的每个字符
            char c = s.charAt(index);
            //level大于0并且jsonForMatStr中的最后一个字符为\n,jsonForMatStr加入\t
            if (level > 0 && '\n' == jsonForMatStr.charAt(jsonForMatStr.length() - 1)) {
                jsonForMatStr.append(getLevelStr(level));
            }
            //遇到"{"和"["要增加空格和换行，遇到"}"和"]"要减少空格，以对应，遇到","要换行
            switch (c) {
                case '{':
                case '[':
                    jsonForMatStr.append(c + "\n");
                    level++;
                    break;
                case ',':
                    jsonForMatStr.append(c + "\n");
                    break;
                case '}':
                case ']':
                    jsonForMatStr.append("\n");
                    level--;
                    jsonForMatStr.append(getLevelStr(level));
                    jsonForMatStr.append(c);
                    break;
                default:
                    jsonForMatStr.append(c);
                    break;
            }
        }
        return jsonForMatStr.toString();
    }

    private static String getLevelStr(int level) {
        StringBuffer levelStr = new StringBuffer();
        for (int levelI = 0; levelI < level; levelI++) {
            levelStr.append("\t");
        }
        return levelStr.toString();
    }

    public void d() throws InterruptedException, ExecutionException {
//        final MediaPlayer beep = MediaPlayer.create(this, beep_sound);
//        beep.start();
        Runnable job = new Runnable() {

            boolean _read = false;

            @Override
            public void run() {
                try {

                    aidlIcCard.open();
                    if (aidlIcCard.status() == 1) {
                        if (!_read) {
                            _read = true;
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        if (!(IcCardActivity.this).isFinishing()) {
                                            try {
                                                mLoading.show();
                                            } catch (WindowManager.BadTokenException e) {
                                                Log.e("WindowManagerBad ", e.toString());
                                            }
                                        }

//                                        mLoading.show();
//                                        resultText.setText("");

//                                        iVphoto.setImageBitmap(null);
                                        time1 = System.currentTimeMillis();
                                        timestart = time1;
//                                        showMsg("Reading...");
                                        aidlIdCardTha.searchIDCard(6000, new AidlIdCardThaListener.Stub() {
                                            @Override
                                            public void onFindIDCard(final ThiaIdInfoBeen been) throws RemoteException {
                                                mLoading.dismiss();
                                                try {
                                                    String s = been.toJSONString();
                                                    String msg = jsonFormat(s);
                                                    JSONObject jObject = new JSONObject(msg);
                                                    jObject.putOpt("photo", Util.bitmapToBase64(been.getPhoto()));
                                                    Intent output = new Intent();
                                                    output.putExtra("iccard", jObject.toString());
                                                    setResult(Activity.RESULT_OK, output);
                                                    finish();
                                                } catch (JSONException e) {
                                                    displayResult(e.getMessage());
                                                }
                                            }

                                            @Override
                                            public void onTimeout() throws RemoteException {
                                                log("TIME OUT");
                                                mLoading.dismiss();
                                                displayResult("onTimeout");
                                            }

                                            @Override
                                            public void onError(int i, String s) throws RemoteException {
                                                log("ERROR CODE:" + i + " msg:" + s);
                                                mLoading.dismiss();
                                                displayResult("ERROR CODE:" + i + " msg:" + s);
                                            }
                                        });
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                        displayResult( "msg:" + e.getMessage());
                                    }
                                }
                            });
                        }

                    } else {
                        _read = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                c();
                                mLoading.dismiss();
                            }
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    displayResult( "msg:" + ex.getMessage());
                }
            }
        };
        scheduledExecutor.scheduleAtFixedRate(job, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scheduledExecutor.shutdownNow();
        if (aidlIcCard != null) {
            try {
                aidlIcCard.close();
            } catch (Exception e) {
                e.printStackTrace();
                displayResult( "msg:" + e.getMessage());
            }
        }
    }
}
