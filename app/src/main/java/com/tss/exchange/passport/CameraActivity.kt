package com.tss.exchange.passport


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.centerm.centermposoversealib.thailand.AidlIdCardThaListener
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import com.google.gson.Gson
import com.tss.exchange.R
import com.tss.exchange.entity.MRZModel
import com.tss.exchange.utils.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CameraActivity : AppCompatActivity() {
    private var takePictureButton: Button? = null
    private var textureView: TextureView? = null
    private var cameraId: String? = null
    protected var cameraDevice: CameraDevice? = null
    protected lateinit var cameraCaptureSessions: CameraCaptureSession
    protected var captureRequest: CaptureRequest? = null
    protected lateinit var captureRequestBuilder: CaptureRequest.Builder
    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null
    private val file: File? = null
    private val mFlashSupported: Boolean = false
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    var mrzModel: MRZModel? = null
    internal var textureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            //open your camera here
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            // Transform you image captured size according to the surface width and height
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }
    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened")
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice!!.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        textureView = findViewById(R.id.texture) as TextureView
        assert(textureView != null)
        textureView!!.surfaceTextureListener = textureListener
        takePictureButton = findViewById(R.id.btn_takepicture) as Button
        assert(takePictureButton != null)
        takePictureButton!!.setOnClickListener { takePicture() }
        mrzModel = MRZModel()
    }

    protected fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    protected fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    protected fun takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null")
            displayResult("cameraDevice is null")
            return
        }
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(cameraDevice!!.id)
            var jpegSizes: Array<Size>? = null
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                    .getOutputSizes(ImageFormat.JPEG)
            }
            var width = 640
            var height = 480
            if (jpegSizes != null && 0 < jpegSizes.size) {
                width = jpegSizes[0].width
                height = jpegSizes[0].height
            }
            val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurfaces = ArrayList<Surface>(2)
            outputSurfaces.add(reader.surface)
            outputSurfaces.add(Surface(textureView!!.surfaceTexture))
            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            // Orientation
            val rotation = windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
            val readerListener = object : ImageReader.OnImageAvailableListener {
                override fun onImageAvailable(reader: ImageReader) {
                    var image: Image? = null
                    try {
                        image = reader.acquireLatestImage()
                        val buffer = image!!.planes[0].buffer
                        val bytes = ByteArray(buffer.capacity())
                        buffer.get(bytes)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        val matrix = Matrix()
                        matrix.preScale(-1.0f, 1.0f)
                        val bOutput = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                        val bOutputMRZ = Bitmap.createBitmap(bOutput, 0, (bOutput.height / 20) * 9, bOutput.width, bOutput.height / 9)
                        val image = Util.bitmapToBase64(bOutput)
                        mrzModel = MRZModel()
                        mrzModel?.image = image
                        initiateOCR(bOutputMRZ, mrzModel!!)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        displayResult("FileNotFoundException msg : "+e.message)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        displayResult("IOException msg : "+e.message)
                    } catch (e: StringIndexOutOfBoundsException) {
                        e.printStackTrace()
                        displayResult("StringIndexOutOfBoundsException msg : "+e.message)
                    } finally {
                        image?.close()
                    }
                }
            }
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
            val captureListener = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    super.onCaptureCompleted(session, request, result)
//                    isDone = true
                    val dialog = AlertDialog.Builder(this@CameraActivity)
                    dialog.setTitle("Please check data!!")
                    dialog.setMessage("firstName : " + mrzModel?.firstName +
                                              "\nlastName : " + mrzModel?.lastName +
                                              "\nbirthDate : " + Util.toDateFormat(mrzModel?.birthDate) +
                                              "\nexpire : " + Util.toDateFormat(mrzModel?.expire) +
                                              "\nPassportId : " + mrzModel?.passportId +
                                              "\nGender : " + mrzModel?.gender +
                                              "\nCountry Code : " + mrzModel?.countryCode)
                    dialog.setPositiveButton("ยืนยัน", DialogInterface.OnClickListener { dialogInterface, i ->
                        val data: String = Gson().toJson(mrzModel)
                        val output = Intent()
                        output.putExtra("passport", data)
                        setResult(Activity.RESULT_OK, output)
                        finish()
                    })
                    dialog.setNegativeButton("สแกนใหม่", DialogInterface.OnClickListener { dialogInterface, i ->
                        createCameraPreview()
                    })
                    dialog.setCancelable(false)
                    dialog.show()
                }
            }
            cameraDevice!!.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun initiateOCR(bitmap: Bitmap, mrzModel: MRZModel) {
        // Reset the text area.
        text_mrz.setText("")
        if (bitmap == null) {
            Toast.makeText(this, "Unable to get a valid image.", Toast.LENGTH_SHORT)
                .show()
            text_mrz.setText("Unable to get a valid image (bitmap is null)")
            return
        }
        val textRecognizer = TextRecognizer.Builder(this@CameraActivity)
            .build()
        val frame = Frame.Builder()
            .setBitmap(bitmap)
            .build()
        val textBlocks = textRecognizer.detect(frame)
        var blocks = ""
        var lines = ""
        var words = ""

        Log.d(TAG, "*textBlocks.size: " + textBlocks.size())
        var printStr = ""
        for (index2 in 0 until textBlocks.size()) {
            //extract scanned text blocks here
            val tBlock = textBlocks.valueAt(index2)
            blocks = blocks + tBlock.value + "\n" + "\n"
            try {
                for (index in 0 until tBlock.components.size) {
                    //extract scanned text lines here
                    var line = tBlock.components[index]
                    Log.d(TAG, "****line: " + line.value)

                    lines = lines + line.value + "\n"
                    if (index == 0) {
                        val firstLine = line.value.replace(" ", "")
                        val nationality = firstLine.substring(2, 5)
                        val x = firstLine.indexOf("<", 2)
                        val lastName = firstLine.substring(5, x)
                        val xx = firstLine.indexOf("<", x + 2)
                        val firstName = firstLine.substring(x + 2, xx)
                        mrzModel.firstName = firstName
                        mrzModel.lastName = lastName
                        mrzModel.countryCode = nationality

                        printStr = firstName + "\n" + lastName + "\n" + nationality
                    } else {
                        val secondLine = line.value
                        var desc_a = secondLine.replace(" ", "")
                        var a = desc_a.indexOf("THA", 0)
                        var passportId = desc_a.substring(0, a)
                        var birthDate = desc_a.substring(a + 3, a + 9)
                        var sex = desc_a.substring(a + 10, a + 11)
                        var expire = desc_a.substring(a + 11, a + 17)
                        printStr = printStr + "\n" + birthDate + "\n" + expire + "\n" + passportId + "\n" + sex
                        mrzModel.birthDate = birthDate
                        mrzModel.expire = expire
                        mrzModel.passportId = passportId
                        mrzModel.gender = sex
                    }
//                    for (element in line.components) {
//                        //extract scanned text words here
//                        printStr = printStr + "\n****word: " + element.value
//                        Log.d(TAG, "****word: " + element.value)
//                        words = words + element.value + ", "
//                    }
                }
            } catch (e: StringIndexOutOfBoundsException) {
                e.printStackTrace()
            }
        }
        Log.d(TAG, "printStr: " + printStr)
    }

    protected fun createCameraPreview() {
        try {
            val texture = textureView!!.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            cameraDevice!!.createCaptureSession(Arrays.asList(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession
                    updatePreview()
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    Toast.makeText(this@CameraActivity, "Configuration change", Toast.LENGTH_SHORT)
                        .show()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            displayResult("CameraAccessException msg : "+e.message)
        }
    }

    private fun openCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.e(TAG, "is camera open")
        try {
            cameraId = manager.cameraIdList[0]
            val characteristics = manager.getCameraCharacteristics(cameraId!!)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this,
                                                   Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@CameraActivity,
                                                  arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                                  REQUEST_CAMERA_PERMISSION)
                return
            }
            manager.openCamera(cameraId!!, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            displayResult("CameraAccessException msg : "+e.message)
        }

        Log.e(TAG, "openCamera X")
    }

    protected fun updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return")
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        if (null != cameraDevice) {
            cameraDevice!!.close()
            cameraDevice = null
        }
        if (null != imageReader) {
            imageReader!!.close()
            imageReader = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(this@CameraActivity, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG)
                    .show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
//        startBackgroundThread()
        if (textureView!!.isAvailable) {
            openCamera()
        } else {
            textureView!!.surfaceTextureListener = textureListener
        }
    }
//    override fun onPause() {
//        Log.e(TAG, "onPause")
//        //closeCamera();
//        stopBackgroundThread()
//        super.onPause()
//    }
    override fun onDestroy() {
        super.onDestroy()
        closeCamera()
    }

    companion object {
        private val TAG = "AndroidCameraApi"
        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        private val REQUEST_CAMERA_PERMISSION = 200
    }

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