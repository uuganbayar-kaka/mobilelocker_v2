/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mn.application.mobilechargelocker

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
//import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this
        Log.d("APP", "initialized !!")

        // libraries
        storage.init(instance!!)
    }

    companion object {

        private var instance: App? = null
        val storage = StorageManager()

        // -----------------------------------------------------------------------

        val context: Context
            get() = instance!!.baseContext

        val resource: Resources
            get() = instance!!.resources
    }
}