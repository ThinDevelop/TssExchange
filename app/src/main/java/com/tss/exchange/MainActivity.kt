package com.tss.exchange

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import app.TssServerApplication
import com.tss.exchange.configulations.TinyWebServer
import com.tss.exchange.entity.CurrencyModel
import com.tss.exchange.entity.ParamModel
import com.tss.exchange.passport.CameraActivity
import com.tss.exchange.utils.Util
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity(), TinyWebServer.CallListener {
    val KEY_CAMERA = "camera"
    val KEY_PASSPORT = "passport"
    val KEY_CHIPCARD = "chipcard"
    val KEY_SIGNING = "signing"
    val KEY_SWIPECARD = "swipecard"
    val KEY_PRINT = "print"
    val REQUEST_KEY_CAMERA = 101
    val REQUEST_KEY_PASSPORT = 102
    val REQUEST_KEY_CHIPCARD = 103
    val REQUEST_KEY_SIGNING = 104
    val REQUEST_KEY_SWIPECARD = 105
    val REQUEST_KEY_PRINT = 105
    val PERMISSION_REQUEST_CODE = 201
    var canResponse: Boolean = false
    var response = ""
    lateinit var fullWakeLock: WakeLock
    lateinit var partialWakeLock: WakeLock
    override fun onMethodCall(methodName: String?): String {
        Log.e("panya", "onMethodCall : " + methodName)
        if (methodName.equals(KEY_CAMERA)) {
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            startActivityForResult(intent, REQUEST_KEY_CAMERA)
        } else if (methodName.equals(KEY_PASSPORT)) {
            val intent = Intent(this@MainActivity, CameraActivity::class.java)
            startActivityForResult(intent, REQUEST_KEY_PASSPORT)
        } else if (methodName.equals(KEY_CHIPCARD)) {
            val intent = Intent(this@MainActivity, IcCardActivity::class.java)
            startActivityForResult(intent, REQUEST_KEY_CHIPCARD)
        } else if (methodName.equals(KEY_SIGNING)) {
            val intent = Intent(this@MainActivity, SignActivity::class.java)
            startActivityForResult(intent, REQUEST_KEY_SIGNING)
        } else if (methodName.equals(KEY_PRINT)) {
//            val intent = Intent(this@MainActivity, PrintActivity::class.java)
//            startActivityForResult(intent, REQUEST_KEY_PRINT)
            canResponse = true
            response = "missing parameter!!"
        }
        while (!canResponse) {
            Thread.sleep(1000)
        }
        canResponse = false
        return response
    }

    override fun onPause() {
        super.onPause()
        partialWakeLock?.acquire()
    }

    @SuppressLint("InvalidWakeLockTag")
    protected fun createWakeLocks() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        fullWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                                                "Loneworker - FULL WAKE LOCK")
        partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Loneworker - PARTIAL WAKE LOCK")
    }

    // Called implicitly when device is about to wake up or foregrounded
    override fun onResume() {
        super.onResume()
        if (fullWakeLock.isHeld()) {
            fullWakeLock.release()
        }
        if (partialWakeLock.isHeld()) {
            partialWakeLock.release()
        }

    }

    fun wakeDevice() {
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            data?.let {
                val errorMsg = data?.getExtras()
                    ?.get("error") as String
                val rootObject = JSONObject()
                rootObject.put("error", "1112")
                rootObject.put("msg", errorMsg)
                data.putExtra("response", rootObject.toString())
            }
        } else {
            when (requestCode) {
                REQUEST_KEY_CAMERA -> {
                    println("REQUEST_KEY_CAMERA")
                    val bitmap = data?.getExtras()
                        ?.get("data") as Bitmap
                    val matrix = Matrix()
                    matrix.preScale(-1.0f, 1.0f)
                    val bOutput = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    val matrixa = Matrix()
                    matrixa.postRotate(180f)
                    val rotatedBitmap = Bitmap.createBitmap(bOutput, 0, 0, bOutput.width, bOutput.height, matrixa, true)
                    val base64 = Util.bitmapToBase64(rotatedBitmap)
                    val obj = JSONObject()
                    obj.put("status", "success")
                    obj.put("image", base64)
                    data.putExtra("response", obj.toString())
                }
                REQUEST_KEY_CHIPCARD -> {
                    println("REQUEST_KEY_CHIPCARD")
                    val cardData = data?.getExtras()
                        ?.get("iccard") as String
                    data.putExtra("response", cardData)
                }
                REQUEST_KEY_PASSPORT -> {
                    println("REQUEST_KEY_PASSPORT")
                    val passportData = data?.getExtras()
                        ?.get("passport") as String
                    data.putExtra("response", passportData)
                }
                REQUEST_KEY_SIGNING -> {
                    println("REQUEST_KEY_SIGNING")
                    val base64 = data?.getExtras()
                        ?.getString("signature")
//                    val obj = JSONObject()
//                    obj.put("status", "success")
//                    obj.put("image", base64)
                    data?.putExtra("response", base64)
                }
                REQUEST_KEY_PRINT -> {
                    println("REQUEST_KEY_PRINT")
                    val obj = JSONObject()
                    obj.put("status", "success")
                    data?.apply { this.putExtra("response", obj.toString()) }
                }
                REQUEST_KEY_SWIPECARD -> {
                    println("REQUEST_KEY_SWIPECARD")
                }
                else -> println("ERROR_CASE")
            }
        }
        response = data?.getStringExtra("response")
            .toString()
        canResponse = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        wakeDevice()
        createWakeLocks()
        txt_ip.text = TssServerApplication.ipAddress
        TinyWebServer.startServer(TssServerApplication.ipAddress, 9000, "/web/public_html", this)
        if (!checkPermission()) {
            requestPermission()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("stepanya", "onDestroy")
        TinyWebServer.stopServer()
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE)
        val result2 = ContextCompat.checkSelfPermission(applicationContext, CAMERA)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, CAMERA), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0) {
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                val camera = grantResults[2] == PackageManager.PERMISSION_GRANTED

                if (!write || !read || !camera) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(arrayOf(ACCESS_FINE_LOCATION, CAMERA), PERMISSION_REQUEST_CODE)
                            }
                        }
                        return
                    }
                }
            }
        }
    }

    override fun onMethodCallWithParam(methodName: String?, paramModel: ParamModel?, currencyModelList: ArrayList<CurrencyModel>?): String? {
        if (methodName.equals(KEY_PRINT)) {
            val intent = Intent(this@MainActivity, PrintActivity::class.java)
            intent.putExtra("data", paramModel)
            intent.putParcelableArrayListExtra("currencyList", currencyModelList!!)
            Log.e("panyaa","onMethodCallWithParam")

            startActivityForResult(intent, REQUEST_KEY_PRINT)
        }
        while (!canResponse) {
            Thread.sleep(1000)
        }
        canResponse = false
        return response
    }
}
           