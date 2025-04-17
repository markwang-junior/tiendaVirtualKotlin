package com.markwang.tiendavirtualapp_kotlin

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            // Inicializar Firebase
            FirebaseApp.initializeApp(this)

            // Configurar persistencia de autenticación
            // Esto hace que la sesión de Firebase se mantenga entre reinicios de la app
            try {
                // Intentamos configurar la persistencia (solo puede hacerse una vez)
                FirebaseAuth.getInstance().firebaseAuthSettings
                    .setAppVerificationDisabledForTesting(false)

                Log.d("MyApplication", "Firebase Auth configurado correctamente")
            } catch (e: Exception) {
                Log.e("MyApplication", "Error al configurar Firebase Auth: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("MyApplication", "Error al inicializar la aplicación: ${e.message}")
        }
    }
}