package com.dynamicisland.android.util

import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.cardview.widget.CardView

object BlurHelper {
    
    private const val BLUR_BEHIND_RADIUS_COLLAPSED = 20
    private const val BLUR_BEHIND_RADIUS_EXPANDED = 25
    
    fun isBlurSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
    
    fun isBlurEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val method = WindowManager::class.java.getMethod("isCrossWindowBlurEnabled")
                return method.invoke(wm) as Boolean
            } catch (e: Exception) {
                return true
            }
        }
        return false
    }
    
    fun applyBlurToCardView(cardView: CardView, blurRadius: Int) {
        cardView.cardElevation = if (blurRadius > BLUR_BEHIND_RADIUS_COLLAPSED) 16f else 12f
    }
    
    fun removeBlurFromCardView(cardView: CardView) {
        cardView.cardElevation = 8f
    }
    
    fun configureBlurLayoutParams(
        params: WindowManager.LayoutParams,
        enableBlur: Boolean = true,
        blurRadius: Int = BLUR_BEHIND_RADIUS_COLLAPSED
    ): WindowManager.LayoutParams {
        if (enableBlur && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val flagBlurBehind = WindowManager.LayoutParams::class.java
                    .getField("FLAG_BLUR_BEHIND")
                    .getInt(null)
                params.flags = params.flags or flagBlurBehind
                
                val setBlurMethod = WindowManager.LayoutParams::class.java
                    .getMethod("setBlurBehindRadius", Int::class.javaPrimitiveType)
                setBlurMethod.invoke(params, blurRadius)
            } catch (e: Exception) {
                // Blur not available
            }
        }
        return params
    }
    
    fun updateBlurRadius(params: WindowManager.LayoutParams, blurRadius: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val setBlurMethod = WindowManager.LayoutParams::class.java
                    .getMethod("setBlurBehindRadius", Int::class.javaPrimitiveType)
                setBlurMethod.invoke(params, blurRadius)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    fun getCollapsedBlurRadius(): Int = BLUR_BEHIND_RADIUS_COLLAPSED
    
    fun getExpandedBlurRadius(): Int = BLUR_BEHIND_RADIUS_EXPANDED
}
