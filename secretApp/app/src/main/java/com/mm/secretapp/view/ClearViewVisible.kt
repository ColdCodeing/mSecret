package com.mm.secretapp.view

import android.view.View
import android.widget.ImageView

class ClearViewVisible(val clearImageView: ImageView) : View.OnFocusChangeListener {
    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus) {
            clearImageView.visibility = View.VISIBLE
        } else {
            clearImageView.visibility = View.INVISIBLE
        }
    }
}