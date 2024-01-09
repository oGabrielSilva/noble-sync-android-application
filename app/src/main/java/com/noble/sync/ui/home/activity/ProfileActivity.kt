package com.noble.sync.ui.home.activity

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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.noble.sync.R
import com.noble.sync.anim.Shake
import com.noble.sync.databinding.ActivityProfileBinding
import com.noble.sync.enum.Gender
import com.noble.sync.firebase.Auth
import com.noble.sync.model.User
import com.noble.sync.tools.ImageTool
import com.noble.sync.ui.dialog.DialogProgress
import com.noble.sync.ui.dialog.DialogUpdateAvatar
import com.noble.sync.validation.AuthValidation
import com.noble.sync.vm.AuthViewModel

class ProfileActivity : AppCompatActivity() {
    private val validation = AuthValidation()
    private lateinit var dialog: DialogProgress
    private lateinit var dialogUpdateAvatar: DialogUpdateAvatar

    private lateinit var binding: ActivityProfileBinding
    private lateinit var vm: AuthViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore

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
                    if (btm != null) {
                        val file = ImageTool.bitmapToFile(
                            btm,
                            applicationContext
                        )
                        if (file != null) dialogUpdateAvatar.showWithAvatar(file)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Auth.userExtra == null) {
            finish()
            return
        }
        binding = ActivityProfileBinding.inflate(layoutInflater)
        vm = ViewModelProvider(this)[AuthViewModel::class.java]
        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage
        dialog = DialogProgress(this)
        dialogUpdateAvatar = DialogUpdateAvatar(this) { updateAvatar() }
        setContentView(binding.root)
        setObservers()
        loadVM()
        setListeners()
    }

    private fun setObservers() {
        vm.userImage.observe(this) {
            if (it != null) updateAvatar()
            else {
                binding.profile.setImageResource(R.drawable.profile_placeholder)
                ImageViewCompat.setImageTintList(
                    binding.profile,
                    ColorStateList.valueOf(getColor(R.color.textPlaceholder))
                )
            }
        }
        vm.name.observe(this) { inputIsOK(binding.name, validation.nameIsValid(it)) }
        vm.nickname.observe(this) { inputIsOK(binding.nickname, validation.nameIsValid(it)) }
        vm.bio.observe(this) {
            inputIsOK(binding.bio, validation.bioIsValid(it))
            binding.smallBio.text = (200 - binding.bio.text.length).toString()
        }
        vm.gender.observe(this) { updateGenderOnScreen() }
    }

    private fun loadVM() {
        val user = auth.currentUser ?: return
        val name = user.displayName
        binding.name.setText(name ?: "")
        binding.nickname.setText(Auth.userExtra?.nickname ?: "")
        binding.bio.setText(Auth.userExtra?.bio ?: "")
        vm.updateName(user.displayName ?: "")
        vm.updateNickname(Auth.userExtra?.nickname ?: "")
        vm.updateBio(Auth.userExtra?.bio ?: "")
        if (Auth.userPhoto != null) {
            ImageViewCompat.setImageTintList(binding.profile, null)
            binding.profile.setImageURI(Auth.userPhoto!!.path.toUri())
        }
    }

    private fun setListeners() {
        binding.profile.setOnClickListener { pickImage() }
        binding.name.addTextChangedListener { vm.updateName(it.toString()) }
        binding.nickname.addTextChangedListener { vm.updateNickname(it.toString()) }
        binding.bio.addTextChangedListener { vm.updateBio(it.toString()) }
        binding.male.setOnClickListener { vm.updateGender(Gender.MALE) }
        binding.female.setOnClickListener { vm.updateGender(Gender.FEMALE) }
        binding.other.setOnClickListener { vm.updateGender(Gender.OTHER) }
        binding.save.setOnClickListener { trySignUp() }
        binding.goBack.setOnClickListener { finish() }
        binding.goBackTop.setOnClickListener { finish() }
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

    private fun pickImage() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun updateAvatar() {
        dialogUpdateAvatar.hide()
        dialog.showWithTitle(getString(R.string.wait))
        val avatar = dialogUpdateAvatar.getAvatar()
        if (avatar != null && auth.currentUser != null) {
            Auth.uploadUserPhoto(storage, avatar, auth.currentUser!!.uid)
                .addOnFailureListener {
                    dialog.hide()
                    Snackbar.make(
                        binding.root,
                        R.string.upload_user_photo_error,
                        Snackbar.LENGTH_LONG
                    ).show()
                    Log.e("Test", "uploadUserPhoto:failure", it)
                }
                .addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener {
                        val changes = userProfileChangeRequest {
                            photoUri = it
                        }
                        auth.currentUser?.updateProfile(changes)?.addOnSuccessListener {
                            dialog.hide()
                            Snackbar.make(
                                binding.root,
                                R.string.upload_user_photo_success,
                                Snackbar.LENGTH_LONG
                            ).show()
                            Auth.userPhoto = avatar
                            binding.profile.setImageURI(avatar.path.toUri())
                        }?.addOnFailureListener { ex ->
                            dialog.hide()
                            Snackbar.make(
                                binding.root,
                                R.string.upload_user_photo_error,
                                Snackbar.LENGTH_LONG
                            ).show()
                            Log.e("Test", "uploadUserPhoto:failure", ex)
                        }
                    }.addOnFailureListener { ex ->
                        dialog.hide()
                        Snackbar.make(
                            binding.root,
                            R.string.upload_user_photo_error,
                            Snackbar.LENGTH_LONG
                        ).show()
                        Log.e("Test", "uploadUserPhoto:failure", ex)
                    }

                }
        }

    }

    private fun trySignUp() {
        dialog.showWithTitle(getString(R.string.wait))
        val name = vm.name.value ?: ""
        val nickname = vm.nickname.value ?: ""

        if (authIsValid(name, nickname)) completeSignUp(name, nickname)
        else dialog.hide()
    }

    private fun authIsValid(name: String, nickname: String): Boolean {
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
        return true
    }

    private fun completeSignUp(name: String, nickname: String) {
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
                        endSignUp(nickname)
                    }
                }
        } else {
            userChanges = userProfileChangeRequest {
                displayName = name
            }
            endSignUp(nickname)
        }
    }

    private fun endSignUp(nickname: String) {
        val gender = vm.gender.value

        if (userChanges != null) {
            val user = auth.currentUser
            user!!.updateProfile(userChanges!!)
                .addOnSuccessListener {
                    val usr =
                        User(auth.currentUser!!.uid, nickname, gender!!, Auth.userExtra!!.year, "")
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