package com.example.spotifyclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.spotifyclone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //Todo: Make this app able to push songs with all details on firebase storage

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}