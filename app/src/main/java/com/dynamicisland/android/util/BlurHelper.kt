package com.dynamicisland.android.util

import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import androidx.cardview.widget.CardView

object BlurHelper {
    
    private const val COLLAPSED_BLUR_RADIUS = 30
    private const val EXPANDED_BLUR_RADIUS = 40
    
    fun isBlurSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
    
    fun applyBlurToCardView(cardView: CardView, blurRadius: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                cardView.clipToOutline = true
                cardView.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRoundRect(
                            0, 0,
                            view.width, view.height,
                            cardView.radius
                        )
                    }
                }
                setBlurRadiusViaReflection(cardView, blurRadius)
            } catch (e: Exception) {
                // Fallback silently on error
            }
        }
    }
    
    fun removeBlurFromCardView(cardView: CardView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                setBlurRadiusViaReflection(cardView, 0)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    private fun setBlurRadiusViaReflection(view: View, blurRadius: Int) {
        try {
            val method = View::class.java.getMethod("setBackgroundBlurRadius", Int::class.javaPrimitiveType)
            method.invoke(view, blurRadius)
        } catch (e: Exception) {
            // Method not available on this device
        }
    }
    
    fun configureBlurLayoutParams(
        params: WindowManager.LayoutParams,
        blurRadius: Int = 0
    ): WindowManager.LayoutParams {
        return params
    }
    
    fun getCollapsedBlurRadius(): Int = COLLAPSED_BLUR_RADIUS
    
    fun getExpandedBlurRadius(): Int = EXPANDED_BLUR_RADIUS
}
