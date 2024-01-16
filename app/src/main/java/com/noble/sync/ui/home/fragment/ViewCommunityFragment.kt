package com.noble.sync.ui.home.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.noble.sync.databinding.FragmentViewCommunityBinding
import com.noble.sync.model.Community

class ViewCommunityFragment(private val cm: Community) : Fragment() {

    private lateinit var binding: FragmentViewCommunityBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentViewCommunityBinding.inflate(inflater, container, false).let {
            binding = it
            updateUI()
            it.root
        }
    }

    private fun updateUI() {
        binding.name.text = cm.name
    }
}