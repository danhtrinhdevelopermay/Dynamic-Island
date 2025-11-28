package com.dynamicisland.android.util

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi

object BlurHelper {
    
    private const val DEFAULT_BLUR_RADIUS = 25f
    private const val EXPANDED_BLUR_RADIUS = 30f
    
    fun applyBlurToView(view: View, blurRadius: Float = DEFAULT_BLUR_RADIUS) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            applyBlurToViewApi31(view, blurRadius)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    private fun applyBlurToViewApi31(view: View, blurRadius: Float) {
        try {
            val blurEffect = android.graphics.RenderEffect.createBlurEffect(
                blurRadius,
                blurRadius,
                android.graphics.Shader.TileMode.CLAMP
            )
            view.setRenderEffect(blurEffect)
        } catch (e: Exception) {
            // Fallback: no blur effect on unsupported devices
        }
    }
    
    fun removeBlurFromView(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            removeBlurFromViewApi31(view)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    private fun removeBlurFromViewApi31(view: View) {
        try {
            view.setRenderEffect(null)
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    fun applyWindowBlur(window: Window, blurRadius: Int = DEFAULT_BLUR_RADIUS.toInt()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            applyWindowBlurApi31(window, blurRadius)
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    private fun applyWindowBlurApi31(window: Window, blurRadius: Int) {
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply {
                blurBehindRadius = blurRadius
            }
        } catch (e: Exception) {
            // Fallback: no blur effect
        }
    }
    
    fun configureBlurLayoutParams(
        params: WindowManager.LayoutParams,
        blurRadius: Int = DEFAULT_BLUR_RADIUS.toInt()
    ): WindowManager.LayoutParams {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            configureBlurLayoutParamsApi31(params, blurRadius)
        }
        return params
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    private fun configureBlurLayoutParamsApi31(params: WindowManager.LayoutParams, blurRadius: Int) {
        try {
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            params.blurBehindRadius = blurRadius
        } catch (e: Exception) {
            // Fallback: no blur effect
        }
    }
    
    fun isBlurSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
    
    fun getCollapsedBlurRadius(): Int = DEFAULT_BLUR_RADIUS.toInt()
    
    fun getExpandedBlurRadius(): Int = EXPANDED_BLUR_RADIUS.toInt()
}
