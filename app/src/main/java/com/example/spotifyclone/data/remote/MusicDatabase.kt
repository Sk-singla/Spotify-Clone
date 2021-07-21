package com.example.spotifyclone.data.remote

import android.net.Uri
import android.widget.Toast
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.other.Constants.SONG_COLLECTION
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.Exception

class MusicDatabase {

    private val songCollection = Firebase.firestore.collection(SONG_COLLECTION)
    private val storageRef = Firebase.storage.reference

    suspend fun getAllSongs(): List<Song>{
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        }catch (e: Exception){
            emptyList()
        }
    }

    suspend fun addSong(
        song: Song,
        onSuccess: suspend () -> Unit,
        onFail:suspend (String)->Unit
    ) {
        try {
            songCollection.add(song).await()
            onSuccess()
        } catch (e:Exception) {
            onFail(e.message ?: "Error!")
        }
    }


    suspend fun uploadFileToStorage(
        fileUri: Uri?,
        fileName: String = UUID.randomUUID().toString(),
        handleDownloadUri:(Uri) -> Unit,
        beforeUploadingFile: suspend ()-> Unit = {},
        onError: suspend (String)->Unit = {},
        afterUploadingFile: suspend ()-> Unit = {}
    ){
        try {
            fileUri?.let {
                beforeUploadingFile()
                storageRef.child(fileName).putFile(it).await()
                storageRef.child(fileName).downloadUrl.addOnSuccessListener { imageUri->
                    handleDownloadUri(imageUri)
                }.await()
                afterUploadingFile()
            }

        }catch (e:Exception) {
            onError(e.message ?: "Error Occurred!")
        }
    }

}