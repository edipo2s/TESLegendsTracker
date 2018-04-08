/*
 * Beauty Date - http://www.beautydate.com
 * Created by ediposouza on 2/17/2016
 * Copyright (c) 2016 Beauty Date. All rights reserved.
 */

package com.ediposouza.teslesgendstracker.ui.util

import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class CircleTransform : BitmapTransformation() {

    public override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int,
                                  outHeight: Int): Bitmap? {
        return circleCrop(pool, toTransform)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {}

    companion object {

        fun transform(bitmap: Bitmap): Bitmap? = circleCrop(LruBitmapPool(1), bitmap)

        private fun circleCrop(pool: BitmapPool, source: Bitmap?): Bitmap? {
            var size = 0
            val squared = source?.let {
                size = Math.min(it.width, it.height)
                val x = (it.width - size) / 2
                val y = (it.height - size) / 2

                Bitmap.createBitmap(it, x, y, size, size)
            }

            val bitmap: Bitmap? = pool.get(size, size, Bitmap.Config.ARGB_8888)
            val result: Bitmap? = bitmap ?: Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

            result?.let {
                Canvas(it).apply {
                    val paint = Paint().apply {
                        shader = BitmapShader(squared, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                        isAntiAlias = true
                    }
                    val r = size / 2f
                    drawCircle(r, r, r, paint)
                }
            }
            return result
        }

    }

}