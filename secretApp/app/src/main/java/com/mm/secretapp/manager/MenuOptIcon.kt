package com.mm.secretapp.manager

import android.util.Log
import android.view.Menu

fun setMenuIconShow(menu: Menu?) {
    if (menu != null) {
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                val m = menu.javaClass.getDeclaredMethod(
                        "setOptionalIconsVisible", java.lang.Boolean.TYPE)
                m.isAccessible = true
                m.invoke(menu, true)
            } catch (e: Exception) {
                Log.e("MenuOptIcon", "onMenuOpened...unable to set icons for overflow menu", e)
            }

        }
    }
}