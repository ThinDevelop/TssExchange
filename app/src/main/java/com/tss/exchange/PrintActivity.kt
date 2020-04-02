package com.tss.exchange

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.RemoteException
import android.util.Base64
import android.util.Log
import com.centerm.centermposoversealib.util.Utility
import com.centerm.smartpos.aidl.printer.AidlPrinter
import com.centerm.smartpos.aidl.printer.AidlPrinterStateChangeListener
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.centerm.smartpos.aidl.sys.AidlDeviceManager
import com.centerm.smartpos.constant.Constant
import com.tss.exchange.entity.CurrencyModel
import com.tss.exchange.entity.ParamModel
import com.tss.exchange.utils.Util

class PrintActivity: BaseActivity1() {
    private var printDev: AidlPrinter? = null
    private val callback = PrinterCallback()
    var paramModel: ParamModel? = null
    var currencyModelList: List<CurrencyModel>? =null

    private inner class PrinterCallback : AidlPrinterStateChangeListener.Stub() {
        @Throws(RemoteException::class)
        override fun onPrintError(arg0: Int) {
            displayResult("onPrintError code :"+arg0)
        }

        @Throws(RemoteException::class)
        override fun onPrintFinish() {
            val output = Intent()
            output.putExtra("msg", "print is finish")
            setResult(Activity.RESULT_OK, output)
            this@PrintActivity.finish()
        }

        @Throws(RemoteException::class)
        override fun onPrintOutOfPaper() {
            displayResult("onPrintOutOfPaper")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print)
        Log.e("panyaa","onCreate")
        paramModel = intent.getParcelableExtra("data")
        currencyModelList = intent.getParcelableArrayListExtra("currencyList")
    }

    override fun onDeviceConnected(deviceManager: AidlDeviceManager?) {

    }

    override fun onPrintDeviceConnected(deviceManager: AidlDeviceManager?) {
        try {
            Log.e("panyaa","onPrintDeviceConnected")

            printDev = AidlPrinter.Stub.asInterface(deviceManager?.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_PRINTERDEV))
            print()
        } catch (e: RemoteException) {
            e.printStackTrace()
            displayResult("onPrintDeviceConnected error : "+ e.message)
        }
    }

    fun print() {
        val textList = ArrayList<PrinterParams>()

        var printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("\n\nบริษัท xyz จำกัด")
//        textList.add(printerParams1)
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("\nที่อยู่ 293/117 หมู่บ้านเพอร์เฟคพาร์ค สุวรรณภูมิ เฟส4 แขวงมีนบุรี เขตมีนบุรี กรุงเทพฯ 10510\nเลขประจำตัวผู้เสียภาษี 0001290008\nเลขที่ใบอนุญาติ xxx45678")
//        textList.add(printerParams1)
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(21)
//        printerParams1.setText("==============================")
//        textList.add(printerParams1)
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("เลขที่ "+ paramModel?.id + " วันที่ "+ paramModel?.date)
//        textList.add(printerParams1)
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("ลูกค้า "+ paramModel?.customerName)
//        textList.add(printerParams1)
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("เลขที่เอกสาร "+ paramModel?.docNumber)
//        textList.add(printerParams1)
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("ที่อยู่ "+ paramModel?.address)
//        textList.add(printerParams1)
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("โทรศัพท์ "+ paramModel?.phone)
//        textList.add(printerParams1)
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("วัตถุประสงค์ "+ paramModel?.objective)
//        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(21)
        printerParams1.setText("------------------------------")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(21)
        printerParams1.setText("สกุลเงิน จำนวน  ราคา  จำนวนเงิน")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(21)
        printerParams1.setText("------------------------------")
        textList.add(printerParams1)
        Log.e("panyaa","currencyModelList : "+currencyModelList)

        for (currencyModel in currencyModelList!!) {
            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(25)
            printerParams1.setText(Util.toOneLinePrint(currencyModel.currencyName,currencyModel.number,currencyModel.price,currencyModel.summary))
            textList.add(printerParams1)
        }

//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(21)
//        printerParams1.setText("==============================")
//        textList.add(printerParams1)
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("รวมเป็นเงิน  "+paramModel?.summary+"")
//        textList.add(printerParams1)
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(21)
//        printerParams1.setText("==============================")
//        textList.add(printerParams1)
//
//        val imageBytes = Base64.decode(paramModel?.adminSign, Base64.DEFAULT)
//        var decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//        decodedImage = Bitmap.createScaledBitmap(decodedImage, 120, 120, false)
//        decodedImage = Utility.toGrayscale(decodedImage)
//
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
//        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
//        printerParams1.setBitmap(decodedImage)
//        textList.add(printerParams1)
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
//        printerParams1.setTextSize(21)
//        printerParams1.setText("Administrator")
//        textList.add(printerParams1)
//
//        val imageBytesCus = Base64.decode(paramModel?.customerSign, Base64.DEFAULT)
//        var decodedImageCus = BitmapFactory.decodeByteArray(imageBytesCus, 0, imageBytesCus.size)
//        decodedImageCus = Bitmap.createScaledBitmap(decodedImageCus, 120, 120, false)
//        decodedImageCus = Utility.toGrayscale(decodedImageCus)
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
//        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
//        printerParams1.setBitmap(decodedImageCus)
//        textList.add(printerParams1)
//
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
//        printerParams1.setTextSize(21)
//        printerParams1.setText("ลายเซ็นลูกค้า")
//        textList.add(printerParams1)
//
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
//        printerParams1.setTextSize(18)
//        printerParams1.setText("\n\n***โปรดตรวจสอบจำนวนเงินให้ถูกต้อง***")
//        textList.add(printerParams1)
//
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(21)
//        printerParams1.setText("\n\n\n\n\n")
//        textList.add(printerParams1)


        printDev?.printDatas(textList, callback)
    }
}