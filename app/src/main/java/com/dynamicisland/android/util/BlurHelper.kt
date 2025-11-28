package com.dynamicisland.android.util

import android.view.WindowManager

object BlurHelper {
    
    fun isBlurSupported(): Boolean {
        return false
    }
    
    fun configureBlurLayoutParams(
        params: WindowManager.LayoutParams,
        blurRadius: Int = 0
    ): WindowManager.LayoutParams {
        return params
    }
    
    fun getCollapsedBlurRadius(): Int = 0
    
    fun getExpandedBlurRadius(): Int = 0
}
