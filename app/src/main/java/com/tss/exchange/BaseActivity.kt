package com.tss.exchange

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity: AppCompatActivity() {
    override fun onBackPressed() {
        super.onBackPressed()
        displayResult("onBackPressed")
    }

    fun displayResult(msg: String?) {
        val output = Intent()
        output.putExtra("error", msg)
        setResult(Activity.RESULT_CANCELED, output)
        finish()
    }
}