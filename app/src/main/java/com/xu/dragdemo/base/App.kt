package com.xu.dragdemo.base

import android.app.Application
import android.content.Context


class App : Application() {
    init {
        Appcontext = this
    }

    companion object {
        var Appcontext: Context? = null

        fun getAppContext(): Context? = Appcontext
    }
}