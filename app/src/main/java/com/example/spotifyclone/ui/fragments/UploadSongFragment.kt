package com.example.spotifyclone.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.data.remote.MusicDatabase
import com.example.spotifyclone.databinding.FragmentUploadSongBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class UploadSongFragment: Fragment(R.layout.fragment_upload_song) {

    private lateinit var binding: FragmentUploadSongBinding

    var songImageUri: Uri? = null
    var songUri:Uri? = null

    var imageUrl: String?  = null
    var songUrl: String? = null

    val musicDatabase = MusicDatabase()


    private val storageRef = Firebase.storage.reference

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        songImageUri = it
        binding.songImage.setImageURI(it)
    }
    private val pickSong = registerForActivityResult(ActivityResultContracts.GetContent()) {
        songUri = it
        binding.selectedSongName.text = it.path
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUploadSongBinding.bind(view)

        binding.songImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.selectSongBtn.setOnClickListener {
            pickSong.launch("audio/*")
        }

        binding.uploadSong.setOnClickListener {
            val songTitle = binding.songTitleEt.text.toString().trim()
            val subTittle = binding.subTitleEt.text.toString().trim()

            if(songTitle.isEmpty()){
                Toast.makeText(requireContext(), "Please add Song Title!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(songImageUri == null){
                Toast.makeText(requireContext(), "Please Select an Image!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(songUri == null){
                Toast.makeText(requireContext(), "Please Select a Song!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else {
                uploadSongAndImageAndAddSongToFireStore(
                    songTitle,
                    subTittle
                )
            }
        }
    }


    private fun uploadSongAndImageAndAddSongToFireStore(
        songTitle: String,
        subTittle: String
    ) = lifecycleScope.launch(Dispatchers.IO){
        musicDatabase.uploadFileToStorage(
            fileUri = songImageUri,
            fileName = "images/${UUID.randomUUID()}",
            handleDownloadUri = {
                imageUrl = it.toString()
            },
            beforeUploadingFile = {
                withContext(Dispatchers.Main){
                    disableButtonsAndShowProgressBar()
                }
            },
            onError = { errorMessage ->
                withContext(Dispatchers.Main) {
                    enableButtonsAndHideProgressBar()
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            },
            afterUploadingFile = {

                musicDatabase.uploadFileToStorage(
                    fileUri = songUri,
                    fileName = "songs/${UUID.randomUUID()}",
                    handleDownloadUri = {
                        songUrl = it.toString()
                    },
                    beforeUploadingFile = {
                        withContext(Dispatchers.Main){
                            disableButtonsAndShowProgressBar()
                        }
                    },
                    onError = { errorMessage ->
                        withContext(Dispatchers.Main) {
                            enableButtonsAndHideProgressBar()
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    },
                    afterUploadingFile = {
                        val song = Song(
                            mediaId = UUID.randomUUID().toString(),
                            title = songTitle,
                            subtitle = subTittle,
                            songUrl = songUrl ?: "",
                            imageUrl = imageUrl ?: ""
                        )
                        musicDatabase.addSong(
                            song,
                            onSuccess = {
                                withContext(Dispatchers.Main){
                                    Toast.makeText(requireContext(), "Song uploaded Successfully!!", Toast.LENGTH_SHORT).show()
                                    enableButtonsAndHideProgressBar()
                                    findNavController().popBackStack()
                                }
                            },
                            onFail = { errorMessage->
                                withContext(Dispatchers.Main){
                                    Toast.makeText(requireContext(),errorMessage , Toast.LENGTH_SHORT).show()
                                    enableButtonsAndHideProgressBar()
                                }
                            }
                        )

                    }
                )
            }
        )
    }



    private fun disableButtonsAndShowProgressBar(){
        binding.songImage.isEnabled = false
        binding.selectSongBtn.isEnabled = false
        binding.uploadSong.isEnabled = false
        binding.songTitleEt.isEnabled = false
        binding.subTitleEt.isEnabled = false
        binding.progressBar.isVisible = true
    }

    private fun enableButtonsAndHideProgressBar(){
        binding.songImage.isEnabled = true
        binding.selectSongBtn.isEnabled = true
        binding.uploadSong.isEnabled = true
        binding.songTitleEt.isEnabled = true
        binding.subTitleEt.isEnabled = true
        binding.progressBar.isVisible = false
    }

}