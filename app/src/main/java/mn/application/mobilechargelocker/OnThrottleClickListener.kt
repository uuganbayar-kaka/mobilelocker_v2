package mn.application.mobilechargelocker

import android.os.SystemClock
import android.util.Log
import android.view.View

/**
 * This is a custom clicklistener to prevent multiple click events to happens at the same time.
 *
 * Each click on a button is added in the internal event handler queue, there is no way to remove
 * them from there so the only thing we can do is adding a filter to prevent them to run too
 * frequently
 *
 * In RxJava this could be achieved using RxBinding and throttleFirst operator
 *
 * Base implementation took from here: https://stackoverflow.com/a/20672997/2910520
 */
abstract class OnThrottleClickListener @JvmOverloads constructor(
    private val minClickInterval: Long = 600 //default value for minclick time
) : View.OnClickListener {

    /**
     * The time the last click was applied
     */
    private var firstClickTime: Long = 0
    private var clickCount: Int = 0

    /**
     * @param v The view that was clicked.
     */
    abstract fun onSingleClick(v: View?)

    override fun onClick(v: View?) {
        clickCount++
        val currentClickTime: Long = SystemClock.elapsedRealtime()
        if(clickCount == 1) {
            firstClickTime = currentClickTime
        } else if (clickCount >= 5) {
            if(firstClickTime + minClickInterval > currentClickTime) {
                onSingleClick(v)
            }
            clickCount = 0
            firstClickTime = 0
        }
    }
}

//Extension (optional but usefull):
fun View.setThrottleClickListener(action: (v: View?) -> Unit, millis: Long = 700) {
    setOnClickListener(object : OnThrottleClickListener(millis) {
        override fun onSingleClick(v: View?) {
            action(v)
        }
    })
}
