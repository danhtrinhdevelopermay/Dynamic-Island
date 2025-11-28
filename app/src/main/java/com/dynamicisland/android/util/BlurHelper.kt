package com.dynamicisland.android.util

import android.os.Build
import android.view.WindowManager
import androidx.cardview.widget.CardView

object BlurHelper {
    
    private const val BLUR_BEHIND_RADIUS = 15
    
    fun isBlurSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
    
    fun applyBlurToCardView(cardView: CardView, blurRadius: Int) {
        cardView.cardElevation = 12f
    }
    
    fun removeBlurFromCardView(cardView: CardView) {
        cardView.cardElevation = 8f
    }
    
    fun configureBlurLayoutParams(
        params: WindowManager.LayoutParams,
        enableBlur: Boolean = true
    ): WindowManager.LayoutParams {
        if (enableBlur && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val flagBlurBehind = WindowManager.LayoutParams::class.java
                    .getField("FLAG_BLUR_BEHIND")
                    .getInt(null)
                params.flags = params.flags or flagBlurBehind
                
                val setBlurMethod = WindowManager.LayoutParams::class.java
                    .getMethod("setBlurBehindRadius", Int::class.javaPrimitiveType)
                setBlurMethod.invoke(params, BLUR_BEHIND_RADIUS)
            } catch (e: Exception) {
                // Blur not available
            }
        }
        return params
    }
    
    fun getCollapsedBlurRadius(): Int = BLUR_BEHIND_RADIUS
    
    fun getExpandedBlurRadius(): Int = BLUR_BEHIND_RADIUS
}
