package com.noble.sync.ui.home.activity

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.EditText
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.noble.sync.R
import com.noble.sync.anim.Shake
import com.noble.sync.databinding.ActivityCreateCommunityBinding
import com.noble.sync.enum.Visibility
import com.noble.sync.firebase.Auth
import com.noble.sync.firebase.CommunityBase
import com.noble.sync.model.Community
import com.noble.sync.tools.ImageTool
import com.noble.sync.ui.auth.AuthActivity
import com.noble.sync.ui.dialog.DialogProgress
import com.noble.sync.ui.home.fragment.CommunitiesFragment
import com.noble.sync.validation.CommunityValidation
import com.noble.sync.vm.CreateCommunityViewModel
import java.io.File

class CreateCommunityActivity : AppCompatActivity() {
    private lateinit var dialog: DialogProgress

    private lateinit var binding: ActivityCreateCommunityBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth

    private lateinit var vm: CreateCommunityViewModel

    private val validation = CommunityValidation()

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
                    vm.updateCommunityIcon(
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
        binding = ActivityCreateCommunityBinding.inflate(layoutInflater)
        vm = ViewModelProvider(this)[CreateCommunityViewModel::class.java]
        dialog = DialogProgress(this)

        setContentView(binding.root)
        updateAuth()
        setListeners()
        setObservers()
    }

    private fun setListeners() {
        binding.goBackTop.setOnClickListener { finish() }
        binding.createCommunity.setOnClickListener { trySave() }
        binding.iconBGSelect.setOnClickListener { pickImage() }
        binding.name.addTextChangedListener { vm.updateCommunityName(it.toString()) }
        binding.description.addTextChangedListener { vm.updateCommunityDescription(it.toString()) }
        binding.communityPublic.setOnClickListener {
            if (vm.communityVisibility.value != Visibility.PUBLIC) vm.updateCommunityVisibility(
                Visibility.PUBLIC
            )
        }
        binding.communityPrivate.setOnClickListener {
            if (vm.communityVisibility.value != Visibility.PRIVATE) vm.updateCommunityVisibility(
                Visibility.PRIVATE
            )
        }
    }

    private fun setObservers() {
        vm.communityIcon.observe(this) { if (it != null) binding.icon.setImageURI(it.path.toUri()) }
        vm.communityName.observe(this) { inputIsOK(binding.name, validation.nameIsValid(it)) }
        vm.communityDescription.observe(this) {
            binding.descriptionCount.text =
                (CommunityValidation.descriptionMaxLength - it.length).toString()
            val isOK = validation.descriptionIsValid(it)
            binding.descriptionCount.setTextColor(
                if (isOK) getColor(R.color.warning) else getColor(
                    R.color.danger
                )
            )
            inputIsOK(
                binding.description,
                isOK
            )
        }
        vm.communityVisibility.observe(this) {
            binding.communityPublic.setTextColor(getColor(R.color.text))
            binding.communityPrivate.setTextColor(getColor(R.color.text))
            if (it == Visibility.PUBLIC) binding.communityPublic.setTextColor(getColor(R.color.success))
            else binding.communityPrivate.setTextColor(getColor(R.color.success))
        }
    }

    private fun updateAuth() {
        db = Firebase.firestore
        storage = Firebase.storage
        auth = Firebase.auth
        if (auth.currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finishAffinity()
            return
        }
        if (Auth.userExtra == null) Auth.updateUserExtra(db, auth.currentUser!!.uid, binding.root) {
            updateCommunityNameByNickname()
        } else updateCommunityNameByNickname()
    }

    private fun updateCommunityNameByNickname() {
        if (Auth.userExtra != null) {
            val communityName = getString(R.string.community_name_hint).replace(
                "#", Auth.userExtra!!.nickname
            )
            binding.name.text = Editable.Factory.getInstance().newEditable(communityName)
            vm.updateCommunityName(communityName)
        }
    }

    private fun pickImage() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun inputIsOK(v: EditText, isOK: Boolean) {
        v.setTextColor(if (isOK) getColor(R.color.title) else getColor(R.color.danger))
    }

    private fun trySave() {
        if (vm.communityIcon.value == null) {
            return Snackbar.make(
                binding.root,
                R.string.community_icon_is_required,
                Snackbar.LENGTH_LONG
            ).show()
        }
        if (!validation.nameIsValid(vm.communityName.value ?: "")) {
            Shake.anim(binding.name, 500, 10f, 4f)
            binding.name.requestFocus()
            Snackbar.make(
                binding.root,
                R.string.community_name_is_required,
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        if (!validation.descriptionIsValid(vm.communityDescription.value ?: "")) {
            Shake.anim(binding.description, 500, 10f, 4f)
            binding.description.requestFocus()
            Snackbar.make(
                binding.root,
                R.string.community_description_invalid,
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        save()
    }

    private fun save() {
        val name = vm.communityName.value as String
        val description = vm.communityDescription.value as String
        val icon = vm.communityIcon.value as File
        val visibility = vm.communityVisibility.value as Visibility

        val cmt = Community(name, "", visibility, description, auth.currentUser!!.uid)
        cmt.staffs = listOf(cmt.createdBy)
        cmt.members = listOf(cmt.createdBy)
        dialog.showWithTitle(getString(R.string.wait))
        try {
            CommunityBase.uploadIcon(storage, icon, cmt.uid).addOnFailureListener {
                dialog.hide()
                Log.e("Test", "uploadUserPhoto:failure", it)
                Snackbar.make(
                    binding.root,
                    R.string.upload_community_icon_error,
                    Snackbar.LENGTH_LONG,
                ).show()
            }
                .addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener { uri ->
                        cmt.icon = uri.toString()
                        uploadCommunity(cmt)
                    }
                }
        } catch (e: Exception) {
            Log.e("Test", "uploadCommunityException", e)
            Snackbar.make(
                binding.root,
                R.string.upload_community_icon_error,
                Snackbar.LENGTH_LONG,
            ).show()
        }
    }

    private fun uploadCommunity(cmt: Community) {
        CommunityBase.uploadCommunity(db, cmt).addOnCompleteListener {
            dialog.hide()
            if (!it.isSuccessful) {
                Snackbar.make(
                    binding.root,
                    R.string.upload_community_error,
                    Snackbar.LENGTH_LONG,
                ).show()
                CommunityBase.deleteIcon(storage, cmt.uid)
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.generic_success,
                    Snackbar.LENGTH_LONG,
                ).show()
                CommunityBase.listOfCommunities.add(cmt)
                if (CommunitiesFragment.adapter != null) CommunitiesFragment.adapter!!.notifyItemInserted(
                    CommunitiesFragment.adapter!!.itemCount
                )
                finish()
            }
        }
    }
}