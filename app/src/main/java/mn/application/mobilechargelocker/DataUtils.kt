package mn.application.mobilechargelocker

import android.content.Context
import android.view.View
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DataUtils {
    class LoadingViewManager(
        private val loadingView: View?,
        private val coroutineScope: CoroutineScope
    ) {
        private var timeoutJob: Job? = null

        fun showWithTimeout(timeoutMillis: Long = 3000L) {
            loadingView?.visibility = View.VISIBLE
            timeoutJob?.cancel()
            timeoutJob = coroutineScope.launch {
                delay(timeoutMillis)
                loadingView?.visibility = View.GONE
            }
        }

        fun hide() {
            timeoutJob?.cancel()
            loadingView?.visibility = View.GONE
        }
    }



}