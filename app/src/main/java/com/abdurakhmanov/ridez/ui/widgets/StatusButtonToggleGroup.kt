package com.abdurakhmanov.ridez.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.abdurakhmanov.ridez.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class StatusButtonToggleGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButtonToggleGroup(context, attrs, defStyleAttr) {

    private val checkedFreeColor = ContextCompat.getColor(context, R.color.aqua_green)
    private val checkedFreeTextColor = ContextCompat.getColor(context, R.color.lynx_white)
    private val checkedBusyColor = ContextCompat.getColor(context, R.color.valencia_red)
    private val checkedBusyTextColor = ContextCompat.getColor(context, R.color.lynx_white)

    private val uncheckedColor = ContextCompat.getColor(context, R.color.status_unchecked_bg_color)
    private val uncheckedTextColor =
        ContextCompat.getColor(context, R.color.status_unchecked_text_color)

    override fun onFinishInflate() {
        super.onFinishInflate()
        updateButtonColors()
        addOnButtonCheckedListener { buttonGroup, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == buttonGroup[0].id) {
                    val checkedFreeButton = findViewById<MaterialButton>(checkedId)
                    setButtonColor(checkedFreeButton, checkedFreeColor, checkedFreeTextColor)
                } else {
                    val checkedBusyButton = findViewById<MaterialButton>(checkedId)
                    setButtonColor(checkedBusyButton, checkedBusyColor, checkedBusyTextColor)
                }
            } else {
                val uncheckedButton = findViewById<MaterialButton>(checkedId)
                setButtonColor(uncheckedButton, uncheckedColor, uncheckedTextColor)
            }
        }
    }

    private fun updateButtonColors() {
        for (i in 0 until childCount) {
            val button = getChildAt(i) as MaterialButton
            if (button.isChecked) {
                setButtonColor(button, checkedFreeColor, checkedFreeTextColor)
            } else {
                setButtonColor(button, uncheckedColor, uncheckedTextColor)
            }
        }
    }

    private fun setButtonColor(
        button: MaterialButton, @ColorInt backgroundColor: Int, @ColorInt textColor: Int
    ) {
        button.setBackgroundColor(backgroundColor)
        button.setTextColor(textColor)
    }
}