package mn.application.mobilechargelocker

import android.app.PendingIntent
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.Button
import java.io.File
import kotlin.system.exitProcess

class SettingsActivity : AppCompatActivity() {
    private var sysNavHider: SystemUiVisibilityHider? = null
    val ACTION_USB_PERMISSION = "mn.application.mobilechargelocker.USB_PERMISSION"
    private val usbDevicesList = ArrayList<UsbDevice?>()
    private val usbDevicesStringList = ArrayList<String?>()
    private var btnBack: ImageButton? = null
    lateinit var closeApplicationBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_settings)

        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        closeApplicationBtn = findViewById(R.id.idBtnCloseApplication)
        closeApplicationBtn.setOnClickListener {
            finishAffinity()
        }

        val languages = resources.getStringArray(R.array.Languages)
        val spinner3 = findViewById<Spinner>(R.id.mode_spinner)
        if (spinner3 != null) {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, languages)
            val position = App.storage.getSwitch()
            spinner3.adapter = adapter
            spinner3.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    println("***** Position: " + languages[position])
                    App.storage.setSwitch(languages[position])
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        usbDevicesStringList.add("Select device ...")

        val usbManager = getSystemService(USB_SERVICE) as UsbManager
        val usbDevices: HashMap<String, UsbDevice> = usbManager.deviceList
        if (usbDevices.isNotEmpty()) {
            for ((_, value) in usbDevices.entries) {
                usbDevicesList.add(value)
                usbDevicesStringList.add(value.toString())
            }
        }

        val spinner0 = findViewById<Spinner>(R.id.spinnerPorts)
        if (spinner0 != null) {
            val ports = getSerialPorts()
            println("\n#### ports : $ports\n")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ports)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val productId = App.storage.getSerialDevice()
            spinner0.adapter = adapter

            spinner0.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    if(position != 0) {
                        if(productId != usbDevicesList[position - 1]!!.productId) {
                            requestPermission(usbDevicesList[position - 1])
                            App.storage.setSerialDevice(usbDevicesList[position - 1]!!.productId)
                            Toast.makeText(this@SettingsActivity,
                                "selected: " + usbDevicesStringList[position], Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }

            if(productId != null && productId != 0) {
                usbDevicesList.forEach { device ->
                    if(device?.productId == productId) {
                        spinner0.setSelection(usbDevicesList.indexOf(device) + 1)
                    }
                }
            }
        }

        val spinner1 = findViewById<Spinner>(R.id.arduino_port_spinner)
        if (spinner1 != null) {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, usbDevicesStringList)
            val productId = App.storage.getArduinoDevice()
            spinner1.adapter = adapter

            spinner1.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
                    if(position != 0) {
                        if(productId != usbDevicesList[position - 1]!!.productId) {
                            requestPermission(usbDevicesList[position - 1])
                            App.storage.setArduinoDevice(usbDevicesList[position - 1]!!.productId)
                            Toast.makeText(this@SettingsActivity,
                                "selected: " + usbDevicesStringList[position], Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }

            if(productId != null && productId != 0) {
                usbDevicesList.forEach { device ->
                    if(device?.productId == productId) {
                        spinner1.setSelection(usbDevicesList.indexOf(device) + 1)
                    }
                }
            }
        }

        val spinner2 = findViewById<Spinner>(R.id.pi_port_spinner)
        if (spinner2 != null) {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, usbDevicesStringList)
            val productPiId = App.storage.getPiDevice()
            spinner2.adapter = adapter

            spinner2.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
                    if(position != 0) {
                        if(productPiId != usbDevicesList[position - 1]!!.productId) {
                            requestPermission(usbDevicesList[position - 1])
                            App.storage.setPiDevice(usbDevicesList[position - 1]!!.productId)
                            Toast.makeText(this@SettingsActivity,
                                "selected: " + usbDevicesStringList[position], Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }

            if(productPiId != null && productPiId != 0) {
                usbDevicesList.forEach { device ->
                    if(device?.productId == productPiId) {
                        spinner2.setSelection(usbDevicesList.indexOf(device) + 1)
                    }
                }
            }
        }

        btnBack = findViewById<ImageButton>(R.id.btn_back)
        btnBack?.setOnClickListener {
            finish()
        }

        sysNavHider = SystemUiVisibilityHider(window)
        sysNavHider?.run()
    }

    private fun getSerialPorts(): List<String> {
        val devDir = File("/dev")
        return devDir.listFiles()
            ?.filter { it.name.startsWith("tty") }
            ?.map { it.absolutePath }
            ?: emptyList()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        sysNavHider?.run()
        return super.onTouchEvent(event)
    }

    override fun onResume() {
        sysNavHider?.run()
        super.onResume()
    }

    fun requestPermission(device: UsbDevice?) {
        if(device != null) {
            val usbManager = getSystemService(USB_SERVICE) as UsbManager
            val pi = PendingIntent.getBroadcast(
                this, 0,
                Intent(ACTION_USB_PERMISSION), 0
            )
            usbManager.requestPermission(device, pi)
        }
    }
}