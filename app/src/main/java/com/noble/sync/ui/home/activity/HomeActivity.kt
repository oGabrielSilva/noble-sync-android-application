package com.noble.sync.ui.home.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.widget.ImageViewCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.noble.sync.R
import com.noble.sync.databinding.ActivityHomeBinding
import com.noble.sync.enum.Gender
import com.noble.sync.firebase.Auth
import com.noble.sync.ui.auth.AuthActivity
import com.noble.sync.ui.home.fragment.CommunitiesFragment
import com.noble.sync.ui.home.fragment.ExploreFragment
import com.noble.sync.ui.home.fragment.MessagesFragment
import com.noble.sync.ui.home.fragment.SettingsFragment
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var vh: ViewHolder
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        auth = Firebase.auth
        db = Firebase.firestore

        setContentView(binding.root)
        configureDrawerMenu()
        setListeners()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, MessagesFragment()).commit()
            binding.navigationView.setCheckedItem(R.id.drawerItemMessages)
        }
    }

    override fun onStart() {
        super.onStart()
        updateUserUI()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment = when (item.itemId) {
            R.id.drawerItemMessages -> MessagesFragment()
            R.id.drawerItemExplore -> ExploreFragment()
            R.id.drawerItemCommunities -> CommunitiesFragment()
            R.id.drawerItemSettings -> SettingsFragment { signOut() }
            R.id.drawerItemEditProfile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                null
            }

            else -> MessagesFragment()
        }
        if (fragment != null) supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun configureDrawerMenu() {
        try {
            val nv = binding.navigationView.getHeaderView(0)
            vh = ViewHolder(
                nv.findViewById(R.id.avatarDrawer),
                nv.findViewById(R.id.userName),
                nv.findViewById(R.id.userNickname),
                nv.findViewById(R.id.userEmail),
                nv.findViewById(R.id.userGender)
            )
            val actionBarDrawerToggle =
                ActionBarDrawerToggle(
                    this,
                    binding.drawerLayout,
                    R.string.nav_open,
                    R.string.nav_close
                )
            binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
            actionBarDrawerToggle.syncState()
            binding.navigationView.setNavigationItemSelectedListener(this)

            updateUserUI()
        } catch (e: Exception) {
            Log.e("Test", "exception", e)
            Snackbar.make(binding.root, e.message ?: "", Snackbar.LENGTH_INDEFINITE).show()
        }
    }

    private fun setListeners() {
        binding.drawerTopButton.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }
        binding.fabSupportDrawer.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }

        vh.avatar.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateUserUI() {
        try {
            if (auth.currentUser != null) {
                val user = auth.currentUser!!
                vh.email.text = user.email
                vh.name.text = user.displayName
                if (Auth.userExtra == null) {
                    Auth.updateUserExtra(db, user.uid, binding.root) { success ->
                        if (success != null && success && Auth.userExtra != null) {
                            vh.nickname.text = Auth.userExtra?.nickname
                            when (Auth.userExtra?.gender) {
                                Gender.MALE -> R.drawable.ic_male
                                Gender.FEMALE -> R.drawable.ic_female
                                else -> null
                            }.let {
                                if (it != null) {
                                    vh.gender.visibility = View.VISIBLE
                                    vh.gender.setImageResource(it)
                                } else vh.gender.visibility = View.GONE
                            }
                            if (user.photoUrl != null && Auth.userPhoto == null) {
                                GlobalScope.launch {
                                    try {
                                        val url = URL(user.photoUrl?.toString())
                                        val inputStream = url.openConnection().inputStream
                                        val file = File(cacheDir, "avatar")
                                        file.writeBytes(inputStream.readBytes())
                                        inputStream.close()
                                        withContext(Dispatchers.Main) {
                                            Auth.userPhoto = file
                                            ImageViewCompat.setImageTintList(vh.avatar, null)
                                            vh.avatar.setImageURI(file.path.toUri())
                                        }
                                    } catch (e: Exception) {
                                        Log.e("Test", "loadPhotoURIException", e)
                                    }
                                }
                            } else {
                                ImageViewCompat.setImageTintList(vh.avatar, null)
                                vh.avatar.setImageURI(Auth.userPhoto!!.path.toUri())
                            }
                        } else {
                            startActivity(Intent(this, CompleteProfileActivity::class.java))
                            finish()
                        }
                    }
                } else {
                    vh.nickname.text = Auth.userExtra?.nickname
                    when (Auth.userExtra?.gender) {
                        Gender.MALE -> R.drawable.ic_male
                        Gender.FEMALE -> R.drawable.ic_female
                        else -> null
                    }.let {
                        if (it != null) {
                            vh.gender.visibility = View.VISIBLE
                            vh.gender.setImageResource(it)
                        } else vh.gender.visibility = View.GONE
                    }
                    if (user.photoUrl != null && Auth.userPhoto == null) {
                        GlobalScope.launch {
                            try {
                                val url = URL(user.photoUrl?.toString())
                                val inputStream = url.openConnection().inputStream
                                val file = File(cacheDir, "avatar")
                                file.writeBytes(inputStream.readBytes())
                                inputStream.close()
                                withContext(Dispatchers.Main) {
                                    Auth.userPhoto = file
                                    ImageViewCompat.setImageTintList(vh.avatar, null)
                                    vh.avatar.setImageURI(file.path.toUri())
                                }
                            } catch (e: Exception) {
                                Log.e("Test", "loadPhotoURIException", e)
                            }
                        }
                    } else {
                        ImageViewCompat.setImageTintList(vh.avatar, null)
                        vh.avatar.setImageURI(Auth.userPhoto!!.path.toUri())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Test", "exception", e)
            Snackbar.make(binding.root, e.message ?: "", Snackbar.LENGTH_INDEFINITE).show()
        }
    }

    private fun signOut() {
        auth.signOut()
        finishAffinity()
        startActivity(Intent(this, AuthActivity::class.java))
    }

    companion object {
        private class ViewHolder(
            val avatar: ImageView,
            val name: TextView,
            val nickname: TextView,
            val email: TextView,
            val gender: ImageView
        )
    }
}