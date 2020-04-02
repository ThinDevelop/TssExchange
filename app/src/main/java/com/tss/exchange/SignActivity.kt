package com.tss.exchange

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.centerm.centermposoversealib.util.Utility
import com.github.gcacace.signaturepad.views.SignaturePad
import com.tss.exchange.utils.Util
import kotlinx.android.synthetic.main.activity_sign.*


class SignActivity : BaseActivity() {
    var mSignaturePad: SignaturePad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        clearsign_btn.setOnClickListener{
            signature_pad.clear()
        }
        mSignaturePad = findViewById(R.id.signature_pad) as SignaturePad
        mSignaturePad!!.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
            }

            override fun onSigned() {
                save_btn.setEnabled(true)
                clearsign_btn.setEnabled(true)
            }

            override fun onClear() {
                save_btn.setEnabled(false)
                clearsign_btn.setEnabled(false)
            }
        })
        save_btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                var signatureBitmap: Bitmap = mSignaturePad!!.getSignatureBitmap()
                signatureBitmap = Bitmap.createScaledBitmap(signatureBitmap, 100, 100, false)
                signatureBitmap = Utility.toGrayscale(signatureBitmap)
                val base64 = Util.bitmapToBase64(signatureBitmap)
                val output = Intent()
                output.putExtra("signature", base64)
                setResult(Activity.RESULT_OK, output)
                finish()
            }
        })
    }

}