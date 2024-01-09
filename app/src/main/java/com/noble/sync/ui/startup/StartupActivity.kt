package com.noble.sync.ui.startup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.noble.sync.R
import com.noble.sync.ui.auth.AuthActivity
import com.noble.sync.ui.home.activity.HomeActivity

class StartupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)

        Handler(Looper.getMainLooper()).postDelayed({
            val logged = Firebase.auth.currentUser == null
            startActivity(
                Intent(
                    applicationContext,
                    if (logged) AuthActivity::class.java else HomeActivity::class.java
                )
            )
            finish()
        }, 1500)
    }
}