package com.example.spotifyclone.exoplayer

import android.content.ComponentName
import android.content.Context
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.spotifyclone.other.Constants.NETWORK_ERROR
import com.example.spotifyclone.other.Event
import com.example.spotifyclone.other.Resource

class MusicServiceConnection(
    context: Context
) {

    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat>()
    val playbackState: LiveData<PlaybackStateCompat> = _playbackState

    private val _curPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val curplayingSong: LiveData<MediaMetadataCompat?> = _curPlayingSong

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply { connect() }

    lateinit var mediaController: MediaControllerCompat

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    fun subscribe(parentId:String, callback: MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.subscribe(parentId,callback)
    }

    fun unSubscribe(parentId:String, callback: MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.unsubscribe(parentId,callback)
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ): MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context,mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isConnected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            _isConnected.postValue(Event(Resource.error(
                "Connection was suspended",false
            )))
        }

        override fun onConnectionFailed() {
            _isConnected.postValue(Event(Resource.error(
                "Couldn't connect to media browser",false
            )))
        }
    }


    private inner class MediaControllerCallback: MediaControllerCompat.Callback(){

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _curPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when(event){
                NETWORK_ERROR -> _networkError.postValue(
                    Event(
                        Resource.error("Couldn't connect to the server. Check your Internet connection!",
                        null
                        )
                    )
                )
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }



}