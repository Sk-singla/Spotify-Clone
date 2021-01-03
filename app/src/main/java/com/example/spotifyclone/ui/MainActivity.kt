package com.example.spotifyclone.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.adapters.SwipeSongAdapter
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.databinding.ActivityMainBinding
import com.example.spotifyclone.exoplayer.toSong
import com.example.spotifyclone.other.Status
import com.example.spotifyclone.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //Todo: Make this app able to push songs with all details on firebase storage

    @Inject
    lateinit var glide: RequestManager

    private lateinit var binding: ActivityMainBinding

    private val mainviewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private var curPlayingSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToObservers()
        binding.vpSong.adapter = swipeSongAdapter
    }

    private fun swithcViewPagerToCurSong(song:Song){
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if(newItemIndex != -1){
            binding.vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    private fun subscribeToObservers(){
        mainviewModel.mediaItem.observe(this) { result->
            when(result.status){
                Status.SUCCESS ->{
                    result.data?.let { songs->
                        swipeSongAdapter.songs = songs
                        if(songs.isNotEmpty()){
                            glide.load((curPlayingSong ?: songs[0]).imageUrl).into(ivCurSongImage)
                        }
                        swithcViewPagerToCurSong(curPlayingSong ?: return@observe)
                    }
                }
                Status.ERROR -> Unit

                Status.LOADING -> Unit
            }
        }

        mainviewModel.curPlayingSong.observe(this){
            if(it == null) return@observe
            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.imageUrl).into(ivCurSongImage)
            swithcViewPagerToCurSong(curPlayingSong?:return@observe)
        }
    }
}