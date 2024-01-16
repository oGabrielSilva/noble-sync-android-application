package com.noble.sync.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.noble.sync.model.Community

class CommunitiesViewModel : ViewModel() {
    private val communitiesData = MutableLiveData<List<Community>>(listOf())

    val communities: LiveData<List<Community>> = communitiesData

    fun updateListOfCommunities(list: List<Community>) {
        communitiesData.value = list
    }
}