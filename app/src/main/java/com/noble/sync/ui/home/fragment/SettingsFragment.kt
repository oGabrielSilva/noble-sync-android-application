package com.noble.sync.ui.home.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.noble.sync.databinding.FragmentSettingsBinding
import com.noble.sync.firebase.Auth

class SettingsFragment(val signOut: () -> Unit) : Fragment() {
    private var binding: FragmentSettingsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentSettingsBinding.inflate(inflater, container, false).let {
            binding = it
            setListeners()
            it.root
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setListeners() {
        binding?.signOutButton?.setOnClickListener { signOut() }
    }
}