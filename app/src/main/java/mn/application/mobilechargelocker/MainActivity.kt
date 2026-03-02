package mn.application.mobilechargelocker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.felhr.usbserial.UsbSerialInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.*
import mn.mpf.system.constants.ConstVariables
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import androidx.lifecycle.lifecycleScope

class MainActivity : AppCompatActivity() {
    private lateinit var loadingViewManager: DataUtils.LoadingViewManager
    private var sysNavHider: SystemUiVisibilityHider? = null
    private lateinit var courseGRV: GridView
    private lateinit var lockerList: List<GridViewModal>

    var arduinoDevice: SerialManager? = null
    var piDevice: SerialManager? = null
    var currentLocker: GridViewModal? = null
    var rxBuffer: ByteArray? = null
    var currentPassword: String? = null
    var lastpan: String? = null
    var settingsCounter: Int = 0
    var timer_mode: String? = null

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_main)

        timer_mode = App.storage.getSwitch()

        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timer_mode = App.storage.getSwitch()
                println("\n***** mode : $timer_mode\n")
                timer_mode = if (timer_mode == ""){
                    "On-Day Mode"
                } else {
                    "On-Day Mode"
                }
            }
            override fun onFinish() {
                ////// timer_mode == "On-Day Mode"
                if (timer_mode == "On-Day Mode") {
                    for (num in 1..10) {
                        var locker = App.storage.getLocker(num)
                        if (locker.getBoolean("used")) {
                            println("*****  locker date_time: " + locker.getString("date_time") + ", id: " + num.toString())
                            val dateFormat = SimpleDateFormat("HH:mm")
                            var removedate = dateFormat.format(Date())
                            println("*****  removeLocker_date : $removedate")
                            if (locker.getString("date_time") == removedate.toString()) {
                                App.storage.removeLocker(num)
                                setLockers()
                            }
                        }
                    }
                }
                ////// timer_mode == "On-Night Mode"
                else {
                    var timeNow = SimpleDateFormat("HH:mm")
                    val currentDate = timeNow.format(Date())
                    if (currentDate.toString() == "03:15"){
                        for (num in 1..10) {
                            var locker = App.storage.getLocker(num)
                            if(locker.getBoolean("used")) {
                                App.storage.removeLocker(num)
                                setLockers()
                            }
                        }
                    }
                    start()
                }
            }
        }.start()

        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        courseGRV = findViewById(R.id.idGRV)
        val loadingView = findViewById<View>(R.id.loadingView)
        loadingViewManager = DataUtils.LoadingViewManager(loadingView, lifecycleScope)

        setLockers()

        arduinoDevice = SerialManager()
        arduinoDevice?.init(this, this@MainActivity, mArduinoCallback)

        piDevice = SerialManager()
        piDevice?.init(this, this@MainActivity, mPiCallback)

        val settingsBtn = findViewById<ImageView>(R.id.settings_btn)

//        showLockerInputDialog()
        settingsBtn.setThrottleClickListener({
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }, 2000)

        sysNavHider = SystemUiVisibilityHider(window)
        sysNavHider?.run()
    }

    override fun onBackPressed() {
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        sysNavHider?.run()
        return super.onTouchEvent(event)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setLockers() {
        currentPassword = null
        lockerList = ArrayList<GridViewModal>()

        lockerList = lockerList + GridViewModal(1, "F1", false)
        lockerList = lockerList + GridViewModal(6, "F6", false)
        lockerList = lockerList + GridViewModal(2, "F2", false)
        lockerList = lockerList + GridViewModal(7, "F7", false)
        lockerList = lockerList + GridViewModal(3, "F3", false)
        lockerList = lockerList + GridViewModal(8, "F8", false)
        lockerList = lockerList + GridViewModal(4, "F4", false)
        lockerList = lockerList + GridViewModal(9, "F9", false)
        lockerList = lockerList + GridViewModal(5, "F5", false)
        lockerList = lockerList + GridViewModal(10, "F0", false)

        lockerList.forEach { locker ->
            val lockerObject = App.storage.getLocker(locker.lockerId)
            lockerList[lockerList.indexOf(locker)].isUsed = lockerObject.getBoolean("used")
        }
        val courseAdapter = GridRVAdapter(lockerList = lockerList, this@MainActivity)

        courseGRV.adapter = courseAdapter
        courseGRV.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val lockerObject = App.storage.getLocker(lockerList[position].lockerId)
            if(lockerObject.getBoolean("used")) {
//                showLockerInputDialog(lockerList[position])
                showAlertDialog(lockerList[position], "used")
            } else {
                showAlertDialog(lockerList[position], "new")
//                showLockerCreateDialog(lockerList[position])
            }
        }

    }

    var mArduinoCallback = UsbSerialInterface.UsbReadCallback { arg0 ->
        var data: String? = null
        try {
            data = String(arg0)
            display(data)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    var mPiCallback =
        UsbSerialInterface.UsbReadCallback { arg0 ->
            if(rxBuffer == null) {
                rxBuffer = byteArrayOf()
            }
            try {
                rxBuffer = if(String(arg0) == "\n") {
                    rxBuffer?.let { buffer ->
                        val response = JSONObject(String(buffer))
                        if(response.has("status")) {
                            if(response.getBoolean("status")) {
                                display("TRANSACTION SUCCESS !")
                                val respBody = response.getJSONObject("response")
                                val pan = respBody.getString("pan")
                                lastpan = pan.substring(pan.length - 4, pan.length)
                                if(currentLocker != null) {
                                    if (arduinoDevice != null && arduinoDevice?.hasDevice() == true) {
                                        if(currentLocker!!.isUsed){
                                            arduinoDevice?.serialWrite("${currentLocker!!.command}\n")
                                            sendHexToSerialPort("${currentLocker!!.command}\n")
                                            display("PWD OPEN COMMAND SEND !!!")
                                            App.storage.removeLocker(currentLocker!!.lockerId)
                                            currentLocker = null
//                                            setLockers()
                                            ////////////
                                            this@MainActivity.runOnUiThread(Runnable {
                                                loadingViewManager.hide()
                                                setLockers()
                                            })
                                        } else {
                                            val dateFormat = SimpleDateFormat("HH:mm")
                                            val date = Date()
                                            val c = Calendar.getInstance()
                                            c.time = date
                                            c.add(Calendar.HOUR, 0)
                                            c.add(Calendar.MINUTE, +3)
                                            var removedate = dateFormat.format(c.time)
                                            arduinoDevice?.serialWrite("${currentLocker!!.command}\n")
                                            sendHexToSerialPort("${currentLocker!!.command}\n")
                                            currentPassword?.let {
                                                App.storage.setLocker(currentLocker!!.lockerId,
                                                    "$it-$lastpan-$removedate"
                                                )
                                            }
                                            currentLocker = null
                                            display("OPEN COMMAND SEND !")
                                            this@MainActivity.runOnUiThread(Runnable {
                                                loadingViewManager.hide()
                                                setLockers()
                                            })
                                        }
                                    } else {
                                        this@MainActivity.runOnUiThread(Runnable {
                                            loadingViewManager.hide()
                                            setLockers()
                                        })
                                        display("002 ARDUINO FAILED !!!")
                                    }
                                } else {
                                    this@MainActivity.runOnUiThread(Runnable {
                                        loadingViewManager.hide()
                                        setLockers()
                                    })
                                    display("003 LOCKER FAILED !!!")
                                }
                            } else {
                                this@MainActivity.runOnUiThread(Runnable {
                                    loadingViewManager.hide()
                                    setLockers()
                                })
                                display("005 TRANSACTION FAILED !!!")
                            }
                        } else {
                            this@MainActivity.runOnUiThread(Runnable {
                                loadingViewManager.hide()
                                setLockers()
                            })
                            display("006 FAILED TO READ RESPONSE !")
                        }
                    }
                    null
                } else {
                    rxBuffer?.plus(arg0)
                }
            } catch (e: UnsupportedEncodingException) {
                display(e.printStackTrace().toString())
            }
        }

    override fun onStop() {
        super.onStop()
        arduinoDevice?.onSerialStop()
        piDevice?.onSerialStop()
        loadingViewManager.hide()
    }

    override fun onResume() {
        super.onResume()
        sysNavHider?.run()
        settingsCounter = 0
        val productId = App.storage.getArduinoDevice()
        if (productId != null && productId != 0) {
            arduinoDevice?.onSerialStart(productId, ConstVariables.ARDUINO_DEVICE)
        }

        val productPiId = App.storage.getPiDevice()
        if (productPiId != null && productPiId != 0) {
            piDevice?.onSerialStart(productPiId, ConstVariables.PI_DEVICE)
        }

        loadingViewManager.hide()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showLockerCreateDialog(locker: GridViewModal) {
        currentPassword = null
        val dialog = BottomSheetDialog(this)

        val view = layoutInflater.inflate(R.layout.bottom_sheet_create_pwd_dialog, null)
        val btnSave = view.findViewById<Button>(R.id.idBtnSave)
        val pwCreateEditText = view.findViewById<EditText>(R.id.pwCreateEditText)
        val pwCreateEditText2 = view.findViewById<EditText>(R.id.pwCreateEditText2)

        btnSave.setOnClickListener {
            if(pwCreateEditText.text.isEmpty()) {
                return@setOnClickListener display("Нууц үг хоосон")
            }

            if(pwCreateEditText2.text.isEmpty()) {
                return@setOnClickListener display("Нууц үг давтах хоосон")
            }

            if(pwCreateEditText.text.length < 4 || pwCreateEditText2.text.length < 4) {
                return@setOnClickListener display("Нууц багадаа 4 оронтой байна")
            }

            if(pwCreateEditText.text.toString() != pwCreateEditText2.text.toString()) {
                return@setOnClickListener display("Нууц үг таарахгүй байна")
            }

            val dateFormat = SimpleDateFormat("HH:mm")
            val date = Date()
            val c = Calendar.getInstance()
            c.time = date
            c.add(Calendar.HOUR, 0)
            c.add(Calendar.MINUTE, +3)
            val removedate = dateFormat.format(c.time)

            currentLocker = locker
            App.storage.setLocker(currentLocker!!.lockerId, "0000-0000-$removedate")
            setLockers()

//            currentPassword = pwCreateEditText.text.toString()
//            currentLocker = locker
//            sendHexToSerialPort("${currentLocker!!.command}\n")
//            dialog.dismiss()
//            if(pwCreateEditText.text.toString() == formattedCode){
//                currentPassword = pwCreateEditText.text.toString()
//                currentLocker = locker
//                sendHexToSerialPort("${currentLocker!!.command}\n")
//                dialog.dismiss()
//            }

//            if(piDevice != null && piDevice?.hasDevice() == true) {
//                piDevice?.serialWrite("sale")
//                display("TRANSACTION COMMAND SEND !")
//            }

            currentPassword = pwCreateEditText.text.toString()
            currentLocker = locker
//            loadingViewManager.showWithTimeout(50000L)
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.setContentView(view)
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showLockerInputDialog(locker: GridViewModal, formattedCode: String) {
        currentPassword = null
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_input_pwd_dialog, null)
        val btnOpen = view.findViewById<Button>(R.id.idBtnOpen)
        val pwInputEditText = view.findViewById<EditText>(R.id.pwInputEditText)
        val lockerObject = App.storage.getLocker(locker.lockerId)

        btnOpen.setOnClickListener {
            if(pwInputEditText.text.isEmpty()) {
                return@setOnClickListener display("Нууц үг хоосон")
            }

            if(pwInputEditText.text.length < 4) {
                return@setOnClickListener display("Таны нууц багадаа 4 оронтой")
            }

            var dayNow: String = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
            var id = locker.lockerId.toString()

            if(id.length < 2) id = "0$id"
            if(dayNow.length < 2) dayNow = "0$dayNow"
            val code = "$dayNow$id"

            if(lockerObject.getString("password") != pwInputEditText.text.toString()) {
                if(lockerObject.getString("password_r") != pwInputEditText.text.toString()) {
                    if(formattedCode == pwInputEditText.text.toString()){
                        currentLocker = locker
                        sendHexToSerialPort("${currentLocker!!.command}\n")
                        dialog.dismiss()
                    }

                    if (code != pwInputEditText.text.toString()) {
                        return@setOnClickListener display(
                            "Таны үүсгэсэн нууц үг таарахгүй байна. Хэрэв мартсан бол картын сүүлийн 4 оронг оруулна уу."
                        )
                        // On TEST
                        // display("OPEN COMMAND SEND REMOVE !")
                        // App.storage.removeLocker(locker.lockerId)
                        // setLockers()
                    } else if (code == pwInputEditText.text.toString()) {
                        if(piDevice != null && piDevice?.hasDevice() == true) {
                            piDevice?.serialWrite("sale")
                            display("PWD TRANSACTION COMMAND SEND !!!")
                            currentLocker = locker
                            loadingViewManager.showWithTimeout(50000L)
                            dialog.dismiss()
                        }
                    }
                } else {
                    if (arduinoDevice != null && arduinoDevice?.hasDevice() == true) {
                        arduinoDevice?.serialWrite("${locker.command}\n")
                        sendHexToSerialPort("${currentLocker!!.command}\n")
                        display("OPEN COMMAND SEND !")
                        App.storage.removeLocker(locker.lockerId)
                        setLockers()
                    }
                }
            } else {
                if (arduinoDevice != null && arduinoDevice?.hasDevice() == true) {
                    arduinoDevice?.serialWrite("${locker.command}\n")
                    sendHexToSerialPort("${currentLocker!!.command}\n")
                    display("OPEN COMMAND SEND !")
                    App.storage.removeLocker(locker.lockerId)
                    setLockers()
                }
            }

//            if (arduinoDevice != null && arduinoDevice?.hasDevice() == true) {
//                arduinoDevice?.serialWrite("${locker.command}\n")
//                display("OPEN COMMAND SEND !")
//                App.storage.removeLocker(locker.lockerId)
//                setLockers()
//            }

            dialog.dismiss()
        }
        dialog.setCancelable(true)
        dialog.setContentView(view)
        dialog.show()
    }

    private fun display(message: String) {
        this@MainActivity.runOnUiThread(Runnable {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAlertDialog(locker: GridViewModal, mode: String) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        alertDialog.setTitle("Error!!!")
        val randInt = Random.nextInt(1, 46)
        val randCode = ((randInt+1)*randInt + 1)
        val formattedCode = randCode.toString().padStart(4, '0')

        if (mode == "used") {
            alertDialog.setIcon(R.drawable.ic_baseline_warning_24)
            alertDialog.setTitle("Та утасаа бүр мөсөн авах гэж байна. ($randInt)")
            alertDialog.setMessage("'Тийм' товч дарсанаар таны сонгосон нүд нээгдэх болно.")
            alertDialog.setPositiveButton(
                "Үгүй"
            ) { _, _ ->}

            alertDialog.setNegativeButton(
                "Тийм"
            ) { _, _ ->
                showLockerInputDialog(locker, formattedCode)
            }
        } else if(mode == "new") {
            alertDialog.setIcon(R.drawable.ic_baseline_battery_charging_full_24)
            alertDialog.setTitle("Утасаа цэнэглэх ($randInt)")
            alertDialog.setMessage("'Тийм' товч дарсанаар карт уншуулж таны сонгосон нүд нээгдэх болно.")
            alertDialog.setPositiveButton(
                "Үгүй"
            ) { _, _ ->}

            alertDialog.setNegativeButton(
                "Тийм"
            ) { _, _ ->
                showLockerCreateDialog(locker)
            }
        }

        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    private fun sendHexToSerialPort(cmd: String) {
        println("\nsendHexToSerialPort cmd : $cmd\n")
        var hexData = byteArrayOf()
        if (cmd[1] == '1') {
            hexData = byteArrayOf(0xFF.toByte(), 0x01, 0x01, 0x02, 0x00, 0x00, 0x00, 0xFD.toByte())
        } else if (cmd[1] == '2') {
            hexData = byteArrayOf(0xFF.toByte(), 0x01, 0x02, 0x02, 0x00, 0x00, 0x00, 0xFE.toByte())
        } else if (cmd[1] == '3') {
            hexData = byteArrayOf(0xFF.toByte(), 0x01, 0x03, 0x02, 0x00, 0x00, 0x00, 0xFF.toByte())
        } else if (cmd[1] == '4') {
            hexData = byteArrayOf(0xFF.toByte(), 0x01, 0x04, 0x02, 0x00, 0x00, 0x00, 0xF8.toByte())
        } else if (cmd[1] == '5') {
            hexData = byteArrayOf(0xFF.toByte(), 0x01, 0x05, 0x02, 0x00, 0x00, 0x00, 0xF9.toByte())
        } else if (cmd[1] == '6') {
            hexData = byteArrayOf(0xFF.toByte(), 0x01, 0x06, 0x02, 0x00, 0x00, 0x00, 0xFA.toByte())
        } else if (cmd[1] == '7') {
            hexData = byteArrayOf(0xFF.toByte(), 0x01, 0x07, 0x02, 0x00, 0x00, 0x00, 0xFB.toByte())
        } else if (cmd[1] == '8') {
            hexData = byteArrayOf(0xFF.toByte(), 0x01, 0x08, 0x02, 0x00, 0x00, 0x00, 0xF4.toByte())
        } else if (cmd[1] == '9') {
            hexData = byteArrayOf(0xFF.toByte(), 0x01, 0x09, 0x02, 0x00, 0x00, 0x00, 0xF5.toByte())
        } else if (cmd[1] == '0') {
            hexData = byteArrayOf(0xFF.toByte(), 0x01, 0x0A, 0x02, 0x00, 0x00, 0x00, 0xF6.toByte())
        } else {
            Toast.makeText(this, "Lockeriin Dugaar Buruu Baina.!!! ", Toast.LENGTH_SHORT).show()
        }

        val portPath = "/dev/ttyS7"
        try {
            val serialPort = File(portPath)
            val outputStream = FileOutputStream(serialPort)
            outputStream.write(hexData)
            outputStream.flush()
            outputStream.close()

            val hexString = hexData.joinToString("") { "%02X".format(it) }
            Toast.makeText(this, "Sent to $cmd -> $hexString", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
