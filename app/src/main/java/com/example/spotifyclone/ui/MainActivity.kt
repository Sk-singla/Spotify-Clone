package com.example.spotifyclone.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.adapters.SwipeSongAdapter
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.databinding.ActivityMainBinding
import com.example.spotifyclone.exoplayer.isPlaying
import com.example.spotifyclone.exoplayer.toSong
import com.example.spotifyclone.other.Status
import com.example.spotifyclone.ui.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
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

    private var playbackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToObservers()

        binding.vpSong.adapter = swipeSongAdapter

        binding.vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if(playbackState?.isPlaying == true){
                    mainviewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                } else{
                    curPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })

        binding.ivPlayPause.setOnClickListener{
            curPlayingSong?.let {
                mainviewModel.playOrToggleSong(it,true)
            }
        }
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

        mainviewModel.playbackState.observe(this){
            playbackState = it
            binding.ivPlayPause.setImageResource(
                    if(playbackState?.isPlaying ==true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }
        mainviewModel.isConnected.observe(this){
            it?.getContentIfNotHandled()?.let { result->
                when(result.status){
                    Status.ERROR -> Snackbar.make(
                            binding.rootLayout,
                            result.message ?: "An Unknown Error Occured",
                            Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }

        mainviewModel.networkError.observe(this){
            it?.getContentIfNotHandled()?.let { result->
                when(result.status){
                    Status.ERROR -> Snackbar.make(
                            binding.rootLayout,
                            result.message ?: "An Unknown Error Occured",
                            Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
    }
}