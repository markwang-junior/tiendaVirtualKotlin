package com.markwang.tiendavirtualapp_kotlin

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // No inicializar nada relacionado con PayPal
    }
}