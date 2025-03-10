package com.markwang.tiendavirtualapp_kotlin.Cliente.Pago

object PayPalConfig {
    // Modo sandbox para desarrollo, cambiar a LIVE para producción
    const val ENVIRONMENT = "SANDBOX"

    // Client ID de PayPal Sandbox
    const val CLIENT_ID = "AUZcceC8n3RI8_q-Ejrlck0XTIzJvOp82tb-BL2Dq63ZHc1LsucabSAJfhcWivVjuaXQ33h85EuhAZcc"

    // Currency code
    const val CURRENCY = "EUR"

    // IDs para intents de pago
    const val PAYMENT_REQUEST_CODE = 123
}