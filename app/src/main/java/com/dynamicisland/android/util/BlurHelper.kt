package com.dynamicisland.android.util

import android.graphics.Bitmap
import android.view.WindowManager
import androidx.cardview.widget.CardView
import kotlin.math.abs

object BlurHelper {
    
    private const val DEFAULT_BLUR_RADIUS = 25f
    private const val COLLAPSED_BLUR_RADIUS = 20f
    private const val EXPANDED_BLUR_RADIUS = 25f
    private const val SCALE_FACTOR = 0.25f
    
    fun isBlurSupported(): Boolean {
        return true
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
    
    fun getCollapsedBlurRadius(): Int = COLLAPSED_BLUR_RADIUS.toInt()
    
    fun getExpandedBlurRadius(): Int = EXPANDED_BLUR_RADIUS.toInt()
    
    fun blurBitmapSync(bitmap: Bitmap, radius: Float = DEFAULT_BLUR_RADIUS): Bitmap? {
        return try {
            val scaledWidth = (bitmap.width * SCALE_FACTOR).toInt().coerceAtLeast(1)
            val scaledHeight = (bitmap.height * SCALE_FACTOR).toInt().coerceAtLeast(1)
            
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
            
            val outputBitmap = Bitmap.createBitmap(scaledBitmap.width, scaledBitmap.height, Bitmap.Config.ARGB_8888)
            
            val blurRadius = radius.coerceIn(1f, 25f).toInt()
            
            stackBlur(scaledBitmap, outputBitmap, blurRadius)
            
            val finalBitmap = Bitmap.createScaledBitmap(outputBitmap, bitmap.width, bitmap.height, true)
            
            if (!scaledBitmap.isRecycled && scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            if (!outputBitmap.isRecycled && outputBitmap != finalBitmap) {
                outputBitmap.recycle()
            }
            
            finalBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun stackBlur(input: Bitmap, output: Bitmap, radius: Int) {
        if (radius < 1) {
            input.getPixels(IntArray(input.width * input.height).also { 
                output.setPixels(it, 0, input.width, 0, 0, input.width, input.height) 
            }, 0, input.width, 0, 0, input.width, input.height)
            return
        }
        
        val w = input.width
        val h = input.height
        val pix = IntArray(w * h)
        input.getPixels(pix, 0, w, 0, 0, w, h)
        
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1
        
        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(w.coerceAtLeast(h))
        
        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }
        
        yi = 0
        yw = 0
        
        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int
        
        y = 0
        while (y < h) {
            bsum = 0
            gsum = 0
            rsum = 0
            boutsum = 0
            goutsum = 0
            routsum = 0
            binsum = 0
            ginsum = 0
            rinsum = 0
            i = -radius
            while (i <= radius) {
                p = pix[yi + wm.coerceAtMost(i.coerceAtLeast(0))]
                sir = stack[i + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius
            
            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]
                
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                
                if (y == 0) {
                    vmin[x] = (x + radius + 1).coerceAtMost(wm)
                }
                p = pix[yw + vmin[x]]
                
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff
                
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]
                
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                
                yi++
                x++
            }
            yw += w
            y++
        }
        
        x = 0
        while (x < w) {
            bsum = 0
            gsum = 0
            rsum = 0
            boutsum = 0
            goutsum = 0
            routsum = 0
            binsum = 0
            ginsum = 0
            rinsum = 0
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = 0.coerceAtLeast(yp) + x
                
                sir = stack[i + radius]
                
                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]
                
                rbs = r1 - abs(i)
                
                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs
                
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                
                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                pix[yi] = (-0x1000000 and pix[yi]) or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
                
                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum
                
                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]
                
                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]
                
                if (x == 0) {
                    vmin[y] = (y + r1).coerceAtMost(hm) * w
                }
                p = x + vmin[y]
                
                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]
                
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
                
                rsum += rinsum
                gsum += ginsum
                bsum += binsum
                
                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]
                
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
                
                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]
                
                yi += w
                y++
            }
            x++
        }
        
        output.setPixels(pix, 0, w, 0, 0, w, h)
    }
    
    fun createSolidColorBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        return bitmap
    }
    
    fun safeRecycleBitmap(bitmap: Bitmap?) {
        try {
            bitmap?.let {
                if (!it.isRecycled) {
                    it.recycle()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
