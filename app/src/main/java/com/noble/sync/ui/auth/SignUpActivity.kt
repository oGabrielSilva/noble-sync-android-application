package com.noble.sync.ui.auth

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.noble.sync.R
import com.noble.sync.anim.Shake
import com.noble.sync.databinding.ActivitySignUpBinding
import com.noble.sync.enum.Gender
import com.noble.sync.firebase.Auth
import com.noble.sync.model.User
import com.noble.sync.tools.ImageTool
import com.noble.sync.ui.dialog.DialogProgress
import com.noble.sync.ui.home.activity.HomeActivity
import com.noble.sync.validation.AuthValidation
import com.noble.sync.vm.AuthViewModel

class SignUpActivity : AppCompatActivity() {
    private lateinit var dialog: DialogProgress

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore

    private var email = ""
    private var password = ""

    private val validation = AuthValidation()
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var vm: AuthViewModel

    private var userChanges: UserProfileChangeRequest? = null

    private val launcherGallery =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            if (it != null) {
                val parcelFileDescriptor =
                    applicationContext.contentResolver.openFileDescriptor(it, "r")
                if (parcelFileDescriptor != null) {
                    val fileDescriptor = parcelFileDescriptor.fileDescriptor
                    val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                    parcelFileDescriptor.close()
                    val btm = ImageTool.adjustImageBitmap(image)
                    vm.updateUserImage(
                        if (btm != null) ImageTool.bitmapToFile(
                            btm,
                            applicationContext
                        ) else null
                    )
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        vm = ViewModelProvider(this)[AuthViewModel::class.java]
        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore
        dialog = DialogProgress(this)

        val bundle = intent.extras
        if (bundle != null) {
            email = bundle.getString("email", "")
            password = bundle.getString("password", "")
        }

        setContentView(binding.root)
        setListeners()
        setObservers()
    }

    private fun setObservers() {
        vm.userImage.observe(this) {
            if (it != null) {
                ImageViewCompat.setImageTintList(binding.profile, null)
                binding.profile.setImageURI(it.path.toUri())
            } else {
                binding.profile.setImageResource(R.drawable.profile_placeholder)
                ImageViewCompat.setImageTintList(
                    binding.profile,
                    ColorStateList.valueOf(getColor(R.color.textPlaceholder))
                )
            }
        }
        vm.name.observe(this) { inputIsOK(binding.name, validation.nameIsValid(it)) }
        vm.nickname.observe(this) { inputIsOK(binding.nickname, validation.nameIsValid(it)) }
        vm.year.observe(this) { inputIsOK(binding.year, validation.yearIsValid(it)) }
        vm.gender.observe(this) { updateGenderOnScreen() }
    }

    private fun setListeners() {
        binding.profile.setOnClickListener { pickImage() }
        binding.removeProfile.setOnClickListener { vm.updateUserImage(null) }
        binding.name.addTextChangedListener { vm.updateName(it.toString()) }
        binding.nickname.addTextChangedListener { vm.updateNickname(it.toString()) }
        binding.year.addTextChangedListener {
            val year = try {
                it.toString().toInt()
            } catch (e: Exception) {
                Log.e("Test", e.stackTraceToString())
                -1
            }
            vm.updateYear(year)
        }
        binding.male.setOnClickListener { vm.updateGender(Gender.MALE) }
        binding.female.setOnClickListener { vm.updateGender(Gender.FEMALE) }
        binding.other.setOnClickListener { vm.updateGender(Gender.OTHER) }
        binding.save.setOnClickListener { trySignUp() }
        binding.goBack.setOnClickListener { finish() }
    }

    private fun pickImage() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun inputIsOK(v: EditText, isOK: Boolean) {
        v.setTextColor(if (isOK) getColor(R.color.title) else getColor(R.color.danger))
    }

    private fun updateGenderOnScreen() {
        binding.male.setTextColor(getColor(R.color.textPlaceholder))
        binding.female.setTextColor(getColor(R.color.textPlaceholder))
        binding.other.setTextColor(getColor(R.color.textPlaceholder))

        when (vm.gender.value) {
            Gender.MALE -> binding.male.setTextColor(getColor(R.color.success))
            Gender.FEMALE -> binding.female.setTextColor(getColor(R.color.success))
            else -> binding.other.setTextColor(getColor(R.color.success))
        }
    }

    private fun authIsValid(name: String, nickname: String, year: Int): Boolean {
        val emailIsValid = validation.emailIsValid(email)
        if (!emailIsValid) {
            Snackbar.make(binding.root, R.string.email_invalid, Snackbar.LENGTH_LONG).show()
            return false
        }


        val passwordIsValid = validation.passwordIsValid(password)
        if (!passwordIsValid) {
            Snackbar.make(binding.root, R.string.password_invalid, Snackbar.LENGTH_LONG).show()
            return false
        }

        val nameIsValid = validation.nameIsValid(name)
        if (!nameIsValid) {
            Shake.anim(binding.name, 500, 10f, 4f)
            return false
        }

        val nicknameIsValid = validation.nameIsValid(nickname)
        if (!nicknameIsValid) {
            Shake.anim(binding.nickname, 500, 10f, 4f)
            return false
        }

        val yearIsValid = validation.yearIsValid(year)
        if (!yearIsValid) {
            Shake.anim(binding.year, 500, 10f, 4f)
            return false
        }
        return true
    }

    private fun trySignUp() {
        dialog.showWithTitle(getString(R.string.wait))
        val name = vm.name.value ?: ""
        val nickname = vm.nickname.value ?: ""
        val year = vm.year.value ?: -1

        if (authIsValid(name, nickname, year)) signUp(name, nickname, year)
        else dialog.hide()
    }

    private fun signUp(name: String, nickname: String, year: Int) {
        Auth.createUserWithEmailAndPassword(auth, email, password)
            .addOnSuccessListener { completeSignUp(name, nickname, year) }
            .addOnFailureListener {
                Log.e("Test", "createUserWithEmail:failure", it)
                Auth.showInvalidAuthMessage(this, it, binding.root)
                dialog.hide()
            }
    }

    private fun completeSignUp(name: String, nickname: String, year: Int) {
        val photo = vm.userImage.value

        if (photo != null) {
            Auth.uploadUserPhoto(
                storage,
                photo,
                auth.currentUser!!.uid
            ).addOnFailureListener {
                dialog.hide()
                Log.e("Test", "uploadUserPhoto:failure", it)
                Snackbar.make(
                    binding.root,
                    R.string.upload_user_photo_error,
                    Snackbar.LENGTH_LONG,
                ).show()
            }
                .addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener {
                        userChanges = userProfileChangeRequest {
                            displayName = name
                            photoUri = it
                        }
                        endSignUp(nickname, year)
                    }
                }
        } else {
            userChanges = userProfileChangeRequest {
                displayName = name
            }
            endSignUp(nickname, year)
        }
    }

    private fun endSignUp(nickname: String, year: Int) {
        val gender = vm.gender.value

        if(userChanges != null) {
            val user = auth.currentUser
            user!!.updateProfile(userChanges!!)
                .addOnSuccessListener {
                    val usr = User(auth.currentUser!!.uid, nickname, gender!!, year, "")
                    Auth.uploadUserExtras(db, usr).addOnCompleteListener {
                        if (it.isSuccessful) {
                            dialog.hide()
                            Auth.userExtra = usr
                            startActivity(Intent(this, HomeActivity::class.java))
                            finishAffinity()
                        } else {
                            dialog.hide()
                            Log.e("Test", "uploadUserExtras:failure", it.exception)
                            Auth.deleteUserPhoto(storage, auth.currentUser!!.uid)
                            auth.currentUser!!.delete()
                            Snackbar.make(
                                binding.root,
                                R.string.upload_user_extra_error,
                                Snackbar.LENGTH_LONG,
                            ).show()
                        }
                    }
                }.addOnFailureListener {
                    dialog.hide()
                    Log.e("Test", "userChanges:failure", it)
                    Auth.deleteUserPhoto(storage, auth.currentUser!!.uid)
                    auth.currentUser!!.delete()
                    Snackbar.make(
                        binding.root,
                        R.string.upload_user_extra_error,
                        Snackbar.LENGTH_LONG,
                    ).show()
                }
        }
    }
}