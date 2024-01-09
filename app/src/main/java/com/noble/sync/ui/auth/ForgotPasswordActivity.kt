package com.noble.sync.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.noble.sync.R
import com.noble.sync.anim.Shake
import com.noble.sync.databinding.ActivityForgotPasswordBinding
import com.noble.sync.databinding.ActivityHomeBinding
import com.noble.sync.ui.dialog.DialogProgress
import com.noble.sync.validation.AuthValidation
import com.noble.sync.vm.AuthViewModel

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var dialog: DialogProgress

    private val validation = AuthValidation()
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var vm: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        vm = ViewModelProvider(this)[AuthViewModel::class.java]
        dialog = DialogProgress(this)

        setContentView(binding.root)
        setListeners()
        setObservers()
    }

    private fun setListeners() {
        binding.email.addTextChangedListener { vm.updateEmail(it.toString()) }
        binding.goBack.setOnClickListener { finish() }
        binding.send.setOnClickListener { send() }
    }

    private fun setObservers() {
        val txtColor = getColor(R.color.title)
        val dangerColor = getColor(R.color.danger)

        vm.email.observe(this) {
            binding.email.setTextColor(if (validation.emailIsValid(it)) txtColor else dangerColor)
        }
    }

    private fun send() {
        dialog.showWithTitle(getString(R.string.wait))
        val email = vm.email.value ?: ""
        if (!validation.emailIsValid(email)) {
            dialog.hide()
            Shake.anim(binding.email, 500, 10f, 4f)
            return
        }
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                dialog.hide()
                if (task.isSuccessful)
                    Snackbar.make(binding.root, R.string.email_send, Snackbar.LENGTH_LONG)
                        .show()
            }
    }
}