package com.noble.sync.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.noble.sync.enum.Visibility
import java.io.File

class CreateCommunityViewModel : ViewModel() {

    private val communityIconData = MutableLiveData<File>()
    private val communityNameData = MutableLiveData("")
    private val communityDescriptionData = MutableLiveData("")
    private val communityVisibilityData = MutableLiveData(Visibility.PUBLIC)

    val communityIcon: LiveData<File> = communityIconData
    val communityName: LiveData<String> = communityNameData
    val communityDescription: LiveData<String> = communityDescriptionData
    val communityVisibility: LiveData<Visibility> = communityVisibilityData

    fun updateCommunityIcon(ic: File?) {
        communityIconData.value = ic
    }

    fun updateCommunityName(s: String) {
        communityNameData.value = s
    }

    fun updateCommunityDescription(s: String) {
        communityDescriptionData.value = s
    }

    fun updateCommunityVisibility(v: Visibility) {
        communityVisibilityData.value = v
    }
}