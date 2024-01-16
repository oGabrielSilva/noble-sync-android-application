package com.noble.sync.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.noble.sync.R
import com.noble.sync.enum.Visibility
import com.noble.sync.firebase.CommunityBase
import com.noble.sync.model.Community
import com.noble.sync.ui.home.fragment.ViewCommunityFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class CommunityAdapter(
    private val fragmentManager: FragmentManager?,
    val getString: (id: Int) -> String
) :
    RecyclerView.Adapter<CommunityAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.community_item_adapter, parent, false)
        )
        holder.container.setOnClickListener {
            if (holder.community != null && fragmentManager != null) {
                fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ViewCommunityFragment(holder.community!!))
                    .commit()
            }
        }
        return holder
    }

    override fun getItemCount() = CommunityBase.listOfCommunities.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cm = CommunityBase.listOfCommunities[position]
        if (cm.iconBitmap == null) {
            val scope = CoroutineScope(Dispatchers.Default)
            scope.launch {
                val url = URL(cm.icon)
                val imageInputStream = url.openConnection().inputStream
                val bitmap = BitmapFactory.decodeStream(imageInputStream)
                imageInputStream.close()
                withContext(Dispatchers.Main) {
                    holder.icon.setImageBitmap(bitmap)
                    cm.iconBitmap = bitmap
                }
            }
        } else holder.icon.setImageBitmap(cm.iconBitmap)
        holder.communityName.text = cm.name
        holder.visibility.text =
            if (cm.visibility == Visibility.PUBLIC) getString(R.string.community_public) else getString(
                R.string.community_private
            )
        holder.community = cm
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var community: Community? = null

        val container: View = view.findViewById(R.id.communityItemAdapterContainer)
        val icon: ImageView = view.findViewById(R.id.communityIconItemAdapter)
        val communityName: TextView = view.findViewById(R.id.communityNameItemAdapter)
        val visibility: TextView = view.findViewById(R.id.communityVisibilityItemAdapter)
    }
}