package org.qosp.notes.ui.utils.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ScaleGestureDetector
import android.view.View
import coil.load

// Todo: Implement panning functionality and replace PhotoView with this widget
class ExtendedImageView : androidx.appcompat.widget.AppCompatImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var scaleFactor = 1.0f
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    @SuppressLint("ClickableViewAccessibility")
    fun loadWithCoil(path: String, parentView: View) {
        scaleGestureDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.OnScaleGestureListener {
                override fun onScale(detector: ScaleGestureDetector?): Boolean {
                    scaleFactor *= scaleGestureDetector.scaleFactor
                    scaleFactor = 0.1f.coerceAtLeast(scaleFactor.coerceAtMost(10.0f))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                    return true
                }

                override fun onScaleBegin(detector: ScaleGestureDetector?) = true
                override fun onScaleEnd(detector: ScaleGestureDetector?) {}
            }
        )

        parentView.setOnTouchListener { v, event ->
            scaleGestureDetector.onTouchEvent(event)
        }

        load(path)
    }
}
