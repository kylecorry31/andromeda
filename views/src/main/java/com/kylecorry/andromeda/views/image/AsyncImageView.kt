package com.kylecorry.andromeda.views.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.kylecorry.andromeda.core.coroutines.ControlledRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AsyncImageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs),
    LifecycleEventObserver {

    private var imageLoader: ControlledRunner<Unit> = ControlledRunner()
    private var lastBitmap: Bitmap? = null

    var clearOnPause = false

    fun setImageBitmap(
        lifecycleOwner: LifecycleOwner,
        provider: suspend () -> Bitmap
    ) {
        lifecycleOwner.lifecycle.removeObserver(this)
        lifecycleOwner.lifecycle.addObserver(this)

        val loadImage = {
            lifecycleOwner.lifecycleScope.launch {
                imageLoader.cancelPreviousThenRun {
                    withContext(Dispatchers.Main) {
                        super.setImageDrawable(null)
                    }
                    lastBitmap = withContext(Dispatchers.IO) {
                        lastBitmap?.recycle()
                        provider.invoke()
                    }
                    withContext(Dispatchers.Main) {
                        if (lastBitmap?.isRecycled == false) {
                            super.setImageBitmap(lastBitmap)
                        }
                    }
                }
            }
        }

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            loadImage()
            return
        }

        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                loadImage()
            }
        })
    }

    fun recycleLastBitmap(clearView: Boolean = true) {
        if (clearView) {
            setImageDrawable(null)
        }
        lastBitmap?.recycle()
        lastBitmap = null
    }

    override fun setImageBitmap(bm: Bitmap?) {
        imageLoader.cancel()
        super.setImageBitmap(bm)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        imageLoader.cancel()
        super.setImageDrawable(drawable)
    }

    override fun setImageResource(resId: Int) {
        imageLoader.cancel()
        super.setImageResource(resId)
    }

    override fun setImageURI(uri: Uri?) {
        imageLoader.cancel()
        super.setImageURI(uri)
    }

    override fun setImageIcon(icon: Icon?) {
        imageLoader.cancel()
        super.setImageIcon(icon)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY || (clearOnPause && event == Lifecycle.Event.ON_PAUSE)) {
            imageLoader.cancel()
            recycleLastBitmap(true)
        }
    }

}