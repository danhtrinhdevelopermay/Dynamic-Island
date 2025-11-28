package com.dynamicisland.android.util

import android.view.WindowManager
import androidx.cardview.widget.CardView

object BlurHelper {
    
    fun isBlurSupported(): Boolean {
        return false
    }
    
    fun applyBlurToCardView(cardView: CardView, isExpanded: Boolean) {
        cardView.cardElevation = if (isExpanded) 16f else 12f
    }
    
    fun removeBlurFromCardView(cardView: CardView) {
        cardView.cardElevation = 8f
    }
    
    fun configureBlurLayoutParams(
        params: WindowManager.LayoutParams
    ): WindowManager.LayoutParams {
        return params
    }
    
    fun getCollapsedBlurRadius(): Int = 0
    
    fun getExpandedBlurRadius(): Int = 0
}
