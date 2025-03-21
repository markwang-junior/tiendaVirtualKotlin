plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.googleService)
}

android {
    namespace = "com.markwang.tiendavirtualapp_kotlin"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.markwang.tiendavirtualapp_kotlin"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding= true
    }
    configurations.all {
        exclude(group = "org.jfrog.cardinalcommerce.gradle", module = "cardinalmobilesdk")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.lottie)/*Animaciones*/
    implementation(libs.firebaseAuth)/*Autenticación con Firebase*/
    implementation(libs.firebaseDatabase)/*Base de datos con Firebase*/
    implementation(libs.imagePicker)/*Recortar una imagen*/
    implementation(libs.glide) /*Leer imagenes*/
    implementation(libs.storage)/*Subir archivos multimedia*/
    implementation(libs.authGoogle)/*Iniciar sesión con google*/
    implementation(libs.ccp) /*Seleccionar nuestro codigo telefónico por pais*/
    implementation(libs.photoView)
    implementation(libs.circleImage)
    implementation(libs.maps)
    implementation(libs.places)
    implementation(libs.paypalCheckoutSdk)
    implementation(libs.cardIo)

}