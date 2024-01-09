package com.noble.sync.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.core.widget.ImageViewCompat
import com.noble.sync.R
import java.io.File

class DialogUpdateAvatar(ctx: Context, val onSave: () -> Unit) : Dialog(ctx) {

    private lateinit var avatar: File
    private lateinit var avatarView: ImageView
    private lateinit var successButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_update_avatar)
        window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        avatarView = findViewById(R.id.avatarDialog)
        successButton = findViewById(R.id.saveDialogButton)
        cancelButton = findViewById(R.id.cancelDialogButton)

        successButton.setOnClickListener { onSave() }
        cancelButton.setOnClickListener { hide() }
    }

    fun updateAvatar(file: File) {
        avatar = file
    }

    fun showWithAvatar(file: File) {
        show()
        avatar = file
        ImageViewCompat.setImageTintList(avatarView, null)
        avatarView.setImageURI(avatar.path.toUri())
        setCancelable(false)
    }

    fun getAvatar(): File? {
        return if (this::avatar.isInitialized) avatar
        else null
    }
}