package com.noble.sync.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import com.noble.sync.R

class DialogProgress(ctx: Context) : Dialog(ctx) {

    private lateinit var title: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_progress);
        window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        title = findViewById(R.id.progressTitle)
    }

    fun updateTitle(t: String) {
        title.text = t
    }

    fun showWithTitle(title: String) {
        show()
        this.title.text = title
        setCancelable(false)
    }
}