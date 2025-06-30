package mn.application.mobilechargelocker

import android.os.Handler
import android.view.View
import android.view.Window


class SystemUiVisibilityHider(w: Window) : View.OnSystemUiVisibilityChangeListener {
    private var window: Window = w
    private var isFlagHideSystemUI = false
    private var flagHideSystemUI = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

    init {
        this.window.decorView.setOnSystemUiVisibilityChangeListener(this)
    }

    override fun onSystemUiVisibilityChange(visibility: Int) {
        if (this.window.decorView.systemUiVisibility != flagHideSystemUI) {
            Handler().postDelayed({
                isFlagHideSystemUI = false
            }, 1000)
        }
    }

    fun run() {
        if (this.window.decorView.systemUiVisibility != flagHideSystemUI) {
            isFlagHideSystemUI = true
            this.window.decorView.systemUiVisibility = flagHideSystemUI
        }
    }
}