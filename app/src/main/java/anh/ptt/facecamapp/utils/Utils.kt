package anh.ptt.facecamapp.utils

import android.app.Activity
import android.util.DisplayMetrics

object Utils {

    @JvmOverloads
    fun configureDeviceScreen(activity: Activity): Pair<Int, Int> {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

}