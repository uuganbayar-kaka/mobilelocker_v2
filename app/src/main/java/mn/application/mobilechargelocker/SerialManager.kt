package mn.application.mobilechargelocker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.USB_SERVICE
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.IBinder
import android.widget.Toast
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import java.lang.reflect.Method


class SerialManager {
    val ACTION_USB_PERMISSION = "mn.application.mobilechargelocker.USB_PERMISSION"

    private var context: Context? = null
    private var activity: Activity? = null
    private var device: UsbDevice? = null
    private var callback: UsbSerialInterface.UsbReadCallback? = null

    var serialPort: UsbSerialDevice? = null
    var connection: UsbDeviceConnection? = null

    fun init(context: Context, activity: Activity, callback: UsbSerialInterface.UsbReadCallback) {
        this.context = context
        this.activity = activity
        this.callback = callback
    }

    fun hasDevice(): Boolean {
        return device != null
    }

    fun onSerialStart(productId: Int, key: String) {
        val usbManager = this.context?.getSystemService(USB_SERVICE) as UsbManager
        val usbDevices: HashMap<String, UsbDevice> = usbManager.deviceList
        var deviceFound = false
        usbDevices.values.forEach { device ->
            if(device.productId == productId) {
                deviceFound = true
            }
        }

        if (usbDevices.isEmpty() || !deviceFound) return display("DEVICE LIST OR SELECTED DEVICE UNPLUGGED !")
        var keep = true
        for ((_, value) in usbDevices.entries) {
            device = value
            val productID: Int = device!!.productId
            if (productID == productId) //Arduino Vendor ID
            {
                keep = false
            } else {
                connection = null
                device = null
            }
            if (!keep) break
        }

        if(device == null) return display("DEVICE IS NULL !")
        if(!usbManager.hasPermission(device)) {
            App.storage.remove(key)
            return display("USB PERMISSION NOT GRANTED")
//            display("USB PERMISSION NOT GRANTED")
//            if(!grantAutomaticPermission(device)) {
//                return display("USB AUTO PERMISSION GRANT FAILED")
//            }
        }

        try {
            connection = usbManager.openDevice(device)
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection)

            if (serialPort != null) {
                if (serialPort!!.open()) { //Set Serial Connection Parameters.
                    serialPort!!.setBaudRate(9600)
                    serialPort!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
                    serialPort!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
                    serialPort!!.setParity(UsbSerialInterface.PARITY_NONE)
                    serialPort!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                    serialPort!!.read(this.callback)
                    this.context
                    display(device?.deviceName +  " SERIAL CONNECTION OPENED !")
                } else {
                    display(device?.deviceName + " PORT NOT OPEN !")
                }
            } else {
                display(device?.deviceName + " PORT IS NULL !")
            }
        } catch (e: Exception) {
            display(device?.deviceName + "USB MANAGER ERROR !")
        }
    }

    fun onSerialStop() {
        if(serialPort != null) {
            serialPort?.close()
        } else {
            display("SERIAL EMPTY !")
        }
    }

    fun serialWrite(message: String) {
        if(serialPort != null) {
            serialPort?.write(message.toByteArray(charset=Charsets.UTF_8))
        } else {
            display("SERIAL EMPTY !")
        }
    }

    private fun display(message: String){
        this.activity?.runOnUiThread(java.lang.Runnable {
            Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
        })
    }

    @SuppressLint("SoonBlockedPrivateApi")
    fun grantAutomaticPermission(usbDevice: UsbDevice?): Boolean {
        return try {
            val context: Context = this.context!!
            val pkgManager = context.packageManager
            val appInfo =
                pkgManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val serviceManagerClass = Class.forName("android.os.ServiceManager")
            val getServiceMethod: Method =
                serviceManagerClass.getDeclaredMethod("getService", String::class.java)
            getServiceMethod.isAccessible = true
            val binder = getServiceMethod.invoke(null, USB_SERVICE) as IBinder
            val iUsbManagerClass = Class.forName("android.hardware.usb.IUsbManager")
            val stubClass = Class.forName("android.hardware.usb.IUsbManager\$Stub")
            val asInterfaceMethod: Method =
                stubClass.getDeclaredMethod("asInterface", IBinder::class.java)
            asInterfaceMethod.isAccessible = true
            val iUsbManager: Any = asInterfaceMethod.invoke(null, binder)
            println("UID : " + appInfo.uid + " " + appInfo.processName + " " + appInfo.permission)
            val grantDevicePermissionMethod: Method = iUsbManagerClass.getDeclaredMethod(
                "grantDevicePermission",
                UsbDevice::class.java,
                Int::class.javaPrimitiveType
            )
            grantDevicePermissionMethod.isAccessible = true
            grantDevicePermissionMethod.invoke(iUsbManager, usbDevice, appInfo.uid)
            println("Method OK : $binder  $iUsbManager")
            true
        } catch (e: Exception) {
            System.err.println("Error trying to assing automatic usb permission : ")
            e.printStackTrace()
            false
        }
    }
}