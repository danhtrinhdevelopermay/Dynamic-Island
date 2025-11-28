package com.dynamicisland.android.util

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager

object BlurHelper {
    
    private const val DEFAULT_BLUR_RADIUS = 25f
    private const val EXPANDED_BLUR_RADIUS = 30f
    
    fun applyBlurToView(view: View, blurRadius: Float = DEFAULT_BLUR_RADIUS) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val blurEffect = RenderEffect.createBlurEffect(
                    blurRadius,
                    blurRadius,
                    Shader.TileMode.CLAMP
                )
                view.setRenderEffect(blurEffect)
            } catch (e: Exception) {
                // Fallback: no blur effect on unsupported devices
            }
        }
    }
    
    fun removeBlurFromView(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                view.setRenderEffect(null)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    fun applyWindowBlur(window: Window, blurRadius: Int = DEFAULT_BLUR_RADIUS.toInt()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes = window.attributes.apply {
                    blurBehindRadius = blurRadius
                }
            } catch (e: Exception) {
                // Fallback: no blur effect
            }
        }
    }
    
    fun configureBlurLayoutParams(
        params: WindowManager.LayoutParams,
        blurRadius: Int = DEFAULT_BLUR_RADIUS.toInt()
    ): WindowManager.LayoutParams {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                params.blurBehindRadius = blurRadius
            } catch (e: Exception) {
                // Fallback: no blur effect
            }
        }
        return params
    }
    
    fun isBlurSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
    
    fun getCollapsedBlurRadius(): Int = DEFAULT_BLUR_RADIUS.toInt()
    
    fun getExpandedBlurRadius(): Int = EXPANDED_BLUR_RADIUS.toInt()
}
