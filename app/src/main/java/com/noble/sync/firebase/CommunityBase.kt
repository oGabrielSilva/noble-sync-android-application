package com.noble.sync.firebase

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.noble.sync.model.Community
import java.io.File

class CommunityBase {
    companion object {
        val listOfCommunities = mutableListOf<Community>()

        fun deleteIcon(storage: FirebaseStorage, uid: String) {
            val ref = storage.reference.child("communities/$uid")
            ref.delete()
        }

        fun uploadIcon(storage: FirebaseStorage, icon: File, uid: String): UploadTask {
            val ref = storage.reference.child("communities/$uid")
            return ref.putFile(Uri.fromFile(icon))
        }

        fun uploadCommunity(db: FirebaseFirestore, community: Community): Task<Void> {
            return db.collection("communities").document(community.uid).set(community.getHashMap())
        }

        fun updateListOfCommunities(db: FirebaseFirestore, userUid: String): Task<QuerySnapshot> {
            return db.collection("communities").whereArrayContains("members", userUid).get()
        }
    }
}