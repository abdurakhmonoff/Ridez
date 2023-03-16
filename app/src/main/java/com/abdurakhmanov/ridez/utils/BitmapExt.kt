package com.abdurakhmanov.ridez.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources

fun Context.bitmapFromDrawableRes(@DrawableRes resourceId: Int) =
    AppCompatResources.getDrawable(this, resourceId).convertDrawableToBitmap()

fun Drawable?.convertDrawableToBitmap(): Bitmap? {
    if (this == null) {
        return null
    }
    return if (this is BitmapDrawable) {
        this.bitmap
    } else {
        val constantState = this.constantState ?: return null
        val drawable = constantState.newDrawable().mutate()
        val bitmap: Bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
}