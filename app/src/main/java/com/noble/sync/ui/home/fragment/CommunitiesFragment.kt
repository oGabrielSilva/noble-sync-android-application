package com.noble.sync.ui.home.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.noble.sync.R
import com.noble.sync.adapter.CommunityAdapter
import com.noble.sync.databinding.FragmentCommunitiesBinding
import com.noble.sync.firebase.CommunityBase
import com.noble.sync.model.Community
import com.noble.sync.ui.home.activity.CreateCommunityActivity

class CommunitiesFragment : Fragment() {

    companion object {
        var adapter: CommunityAdapter? = null
    }

    private lateinit var binding: FragmentCommunitiesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentCommunitiesBinding.inflate(inflater, container, false).let {
            binding = it
            setListeners()
            updateRecyclerView()
            updateCommunitiesList()
            it.root
        }
    }

    private fun updateCommunitiesList() {
        if (CommunityBase.listOfCommunities.isEmpty()) CommunityBase.updateListOfCommunities(
            Firebase.firestore,
            Firebase.auth.currentUser!!.uid
        )
            .addOnFailureListener {
                Snackbar.make(
                    binding.root,
                    R.string.update_communities_list_error,
                    Snackbar.LENGTH_LONG
                ).show()
                Log.e("Test", "updateCommunitiesList", it)
            }.addOnSuccessListener {
                val list = if (it.isEmpty) null else it.toObjects(Community::class.java)
                if (list != null) {
                    CommunityBase.listOfCommunities.addAll(list)
                    CommunityBase.listOfCommunities.addAll(list)
                    CommunityBase.listOfCommunities.addAll(list)
                    CommunityBase.listOfCommunities.addAll(list)
                    adapter!!.notifyItemRangeInserted(adapter!!.itemCount, list.size)
                }
            }
    }

    private fun updateRecyclerView() {
        binding.communityRecycler.layoutManager = GridLayoutManager(activity, 2)
        binding.communityRecycler.setHasFixedSize(true)
        adapter = CommunityAdapter(activity?.supportFragmentManager) { id -> getString(id) }
        binding.communityRecycler.adapter = adapter
    }

    private fun setListeners() {
        binding.createCommunity.setOnClickListener {
            startActivity(
                Intent(
                    activity,
                    CreateCommunityActivity::class.java
                )
            )
        }
    }
}