package com.noble.sync.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.noble.sync.R
import com.noble.sync.anim.Shake
import com.noble.sync.databinding.ActivityAuthBinding
import com.noble.sync.firebase.Auth
import com.noble.sync.ui.dialog.DialogProgress
import com.noble.sync.ui.home.activity.HomeActivity
import com.noble.sync.validation.AuthValidation
import com.noble.sync.vm.AuthViewModel

class AuthActivity : AppCompatActivity() {

    private lateinit var dialog: DialogProgress

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val authValidation = AuthValidation()
    private lateinit var vm: AuthViewModel
    private lateinit var binding: ActivityAuthBinding

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var gso: GoogleSignInOptions
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            if (task.isSuccessful) {
                val account = task.getResult(ApiException::class.java)!!
                continueGoogleSignIn(account)
            } else {
                Log.e("Test", "LoginGoogleFailure", task.exception)
                Snackbar.make(binding.root, R.string.login_failure, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        vm = ViewModelProvider(this)[AuthViewModel::class.java]
        auth = Firebase.auth
        db = Firebase.firestore
        dialog = DialogProgress(this)

        createGoogleSignInRequest()
        setContentView(binding.root)
        setListeners()
        setObservers()
    }

    private fun setListeners() {
        binding.email.addTextChangedListener { vm.updateEmail(it.toString()) }
        binding.password.addTextChangedListener { vm.updatePassword(it.toString()) }
        binding.signUp.setOnClickListener { trySignUp() }
        binding.signInButton.setOnClickListener { trySignIn() }
        binding.signInGoogle.setOnClickListener { googleSignIn() }
        binding.forgotPassword.setOnClickListener { forgotPassword() }
    }

    private fun setObservers() {
        val titleColor = getColor(R.color.title)
        val dangerColor = getColor(R.color.danger)

        vm.email.observe(this) {
            binding.email.setTextColor(
                if (authValidation.emailIsValid(it)) titleColor else dangerColor
            )
        }

        vm.password.observe(this) {
            binding.password.setTextColor(
                if (authValidation.passwordIsValid(it)) titleColor else dangerColor
            )
        }
    }

    private fun authIsValid(email: String, password: String): Boolean {
        val emailIsValid = authValidation.emailIsValid(email)
        if (!emailIsValid) {
            Shake.anim(binding.email, 500, 10f, 4f)
            return false
        }

        val passwordIsValid = authValidation.passwordIsValid(password)
        if (!passwordIsValid) {
            Shake.anim(binding.password, 500, 10f, 4f)
            return false
        }
        return true
    }

    private fun trySignUp() {
        val email = vm.email.value ?: ""
        val password = vm.password.value ?: ""

        if (authIsValid(email, password)) {
            val b = Bundle()
            b.putString("email", email)
            b.putString("password", password)
            val i = Intent(this, SignUpActivity::class.java)
            i.putExtras(b)
            startActivity(i)
        }
    }

    private fun trySignIn() {
        dialog.showWithTitle(getString(R.string.wait))
        val email = vm.email.value ?: ""
        val password = vm.password.value ?: ""

        if (!authIsValid(email, password)) {
            dialog.hide()
            return
        }
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            Auth.updateUserExtra(db, auth.currentUser!!.uid, binding.root) {
                startActivity(Intent(this, HomeActivity::class.java))
                dialog.hide()
                finishAffinity()
            }
        }.addOnFailureListener {
            Log.e("Test", "loginFailure", it)
            Auth.showInvalidAuthMessage(applicationContext, it, binding.root)
            dialog.hide()
        }
    }

    private fun createGoogleSignInRequest() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private fun googleSignIn() {
        FirebaseAuth.getInstance().signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun continueGoogleSignIn(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                } else {
                    Log.e("Test", "LoginGoogleFailure", task.exception)
                    Snackbar.make(binding.root, R.string.login_failure, Snackbar.LENGTH_LONG)
                        .show()
                }
            }
    }

    private fun forgotPassword() {
        startActivity(Intent(this, ForgotPasswordActivity::class.java))
    }
}