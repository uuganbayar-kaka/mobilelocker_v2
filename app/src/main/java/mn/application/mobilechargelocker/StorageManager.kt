package mn.application.mobilechargelocker

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.util.Log
import mn.mpf.system.constants.ConstVariables
import org.json.JSONArray
import org.json.JSONObject

class StorageManager {

    private var preference: SharedPreferences? = null
    fun getPreferences(): SharedPreferences {
        return preference!!
    }

    fun init(app: Application) {
        val fileName = ConstVariables.APP_STORAGE
        print("*********** fileName" + fileName)
        preference = app.getSharedPreferences(fileName, Context.MODE_PRIVATE)
    }

    fun getJSONArray(name: String): JSONArray? {
        var jsonArray: JSONArray? = null
        val jsonString = getString(name)
        if (jsonString == ConstVariables.STR_EMPTY) return null
        else jsonArray = JSONArray(jsonString)
        return jsonArray
    }

    fun getString(name: String): String? {
        return preference!!.getString(name, "")
    }

    fun getFloat(name: String): Float {
        return preference!!.getFloat(name, 0f)
    }

    fun getLong(name: String): Long {
        return preference!!.getLong(name, 0)
    }

    fun getInt(name: String): Int {
        return preference!!.getInt(name, 0)
    }

    fun getBoolean(name: String): Boolean {
        return preference!!.getBoolean(name, false)
    }

    fun remove(keyRemember: String) {
        editor().remove(keyRemember).commit()
    }

    fun editor(): Editor {
        return preference!!.edit()
    }

    // -----------------------------------------------------------------------

    fun setArduinoDevice(productId: Int) {
        with(preference!!.edit()) {
            putInt(ConstVariables.ARDUINO_DEVICE, productId)
            commit()
        }
    }
    fun setSerialDevice(productId: Int) {
        with(preference!!.edit()) {
            putInt(ConstVariables.TTY_DEVICE, productId)
            commit()
        }
    }

    fun getArduinoDevice(): Int? {
        return preference?.getInt(ConstVariables.ARDUINO_DEVICE, 0)
    }
    fun getSerialDevice(): Int? {
        return preference?.getInt(ConstVariables.TTY_DEVICE, 0)
    }

    fun setPiDevice(productId: Int) {
        with(preference!!.edit()) {
            putInt(ConstVariables.PI_DEVICE, productId)
            commit()
        }
    }

    fun getPiDevice(): Int? {
        return preference?.getInt(ConstVariables.PI_DEVICE, 0)
    }

    fun setSwitch(mode: String){
        with(preference!!.edit()) {
            putString(ConstVariables.SWITCH_MODE, mode)
            commit()
        }
    }

    fun getSwitch(): String? {
        return preference?.getString(ConstVariables.SWITCH_MODE, "On-Night Mode")
    }


    fun getLocker(index: Int): JSONObject {
        val json = JSONObject()
        val locker = preference?.getString("locker_${index}", null)
        if(locker != null) {
            json.put("used", true)
            var lockerArr = locker.split("-")
            json.put("password", lockerArr[0])
            json.put("password_r", lockerArr[1])
            json.put("date_time", lockerArr[2])
        } else {
            json.put("used", false)
            json.put("password", null)
            json.put("password_r", null)
            json.put("date_time", null)
        }
        return json
    }

    fun setLocker(index: Int, password: String) {
        with(preference!!.edit()) {
            remove("locker_${index}")
            putString("locker_${index}", password)
            commit()
        }
    }

    fun removeLocker(index: Int) {
        with(preference!!.edit()) {
            remove("locker_${index}")
            println("********* RemoveLocker_${index}")
            commit()
        }
    }
}
