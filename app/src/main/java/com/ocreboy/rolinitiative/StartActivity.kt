package com.ocreboy.rolinitiative

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ocreboy.rolinitiative.language.LanguageManager

@SuppressLint("CustomSplashScreen")
class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val languageManager = LanguageManager(this)
        languageManager.loadSavedLanguage(this) // Aplica el idioma antes de cargar la UI

        setContentView(R.layout.activity_splash)

        val logo: ImageView = findViewById(R.id.logo)
        val heartbeatAnimation = AnimationUtils.loadAnimation(this, R.anim.heartbeat)
        logo.startAnimation(heartbeatAnimation)

        val versionTextView: TextView = findViewById(R.id.versionText)
        val versionName = getAppVersionName()
        versionTextView.text = versionName

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 2000)
    }

    private fun getAppVersionName(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
