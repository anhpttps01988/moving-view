package anh.ptt.facecamapp

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import anh.ptt.facecamapp.utils.Utils

class ViewFaceCam @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    RelativeLayout(context, attrs, defStyle), View.OnTouchListener {

    init {
        setOnTouchListener(this)
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        val screenWidth = Utils.configureDeviceScreen(context as AppCompatActivity).first
        val screenHeight = Utils.configureDeviceScreen(context as AppCompatActivity).second

        var diffX = 0
        var diffY = 0

        event?.run { diffX = rawX.toInt() }
        event?.run { diffY = rawY.toInt() }

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {

            }
            MotionEvent.ACTION_MASK,
            MotionEvent.ACTION_MOVE -> {
                val layoutParams = view!!.layoutParams as FrameLayout.LayoutParams
                val widthContainer = layoutParams.width.toFloat()
                val heightContainer = layoutParams.height.toFloat()

                val endEgde = diffX + widthContainer
                val bottomEgde = diffY + heightContainer

                if (diffX >= 0 && endEgde <= screenWidth && diffY >= 0 && bottomEgde <= screenHeight) {
                    layoutParams.leftMargin = diffX
                    layoutParams.topMargin = diffY
                }

                layoutParams.rightMargin = 0
                layoutParams.bottomMargin = 0
                view.layoutParams = layoutParams
            }
        }
        invalidate()
        return true
    }
}