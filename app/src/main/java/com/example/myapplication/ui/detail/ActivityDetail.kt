package com.example.myapplication.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityDetailLayoutBinding

class ActivityDetail:AppCompatActivity() {
    private lateinit var binding:ActivityDetailLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }
    private fun init(){
        val title = intent.getStringExtra("title")
        val artist = intent.getStringExtra("artist")
        var image = intent.getStringExtra("imageString")
        binding.artistTv.text = "Artist: $artist"
        binding.titleTv.text = "Title: $title"
        Glide.with(this).load(image).placeholder(R.drawable.mnote).into(binding.imgview)
    }
}