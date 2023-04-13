package com.example.myapplication.ui.history

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapters.SongListAdapter
import com.example.myapplication.databinding.ActivityHistoryLayoutBinding
import com.example.myapplication.listeners.OnClickListener
import com.example.myapplication.models.SongModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.ArrayList

class ActivityHistory:AppCompatActivity(),OnClickListener {
    private lateinit var binding:ActivityHistoryLayoutBinding
    private lateinit var adapter :SongListAdapter
    private lateinit var  recyclerView :RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }
    private fun init(){
        recyclerView = binding.historyRV
        adapter = SongListAdapter(arrayListOf(),this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        getArrayList("history").let {
           println(it)
            adapter.setData(it)
        }
    }

    override fun onClick(title: String, artist: String, image: String) {
        println(title)
    }
    fun getArrayList(key: String?): java.util.ArrayList<SongModel> {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val json: String? = prefs.getString(key, null)
        val type: Type = object : TypeToken<ArrayList<SongModel>>() {}.getType()
        return gson.fromJson(json, type)
    }
}