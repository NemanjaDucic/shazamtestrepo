package com.example.myapplication.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import android.view.Menu
import android.view.MenuItem
import com.example.myapplication.databinding.ActivityMainBinding
import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.SongListAdapter
import com.example.myapplication.helpers.Constants.token
import com.example.myapplication.listeners.OnClickListener
import com.example.myapplication.models.SongModel
import com.example.myapplication.ui.detail.ActivityDetail
import com.example.myapplication.ui.fragments.FirstFragment
import com.example.myapplication.ui.history.ActivityHistory
import com.google.gson.Gson
import com.shazam.shazamkit.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class MainActivity : AppCompatActivity(),OnClickListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var catalog: Catalog
    private lateinit var currentSession: StreamingSession
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
    private val fragment = FirstFragment()
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_AUDIO_PERMISSION_CODE = 1
    var songs = ArrayList<SongModel>()
    private lateinit var adapter :SongListAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        configureShazamKitSession(token)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    init()

    }

private fun init(){
adapter = SongListAdapter(arrayListOf(),this)
    recyclerView = binding.recycler
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)
supportFragmentManager.beginTransaction().add(R.id.fragmentContainer,fragment).commit()
    binding.buttonText.setOnClickListener {
//        val intent = Intent(this,ActivityHistory::class.java)
//        startActivity(intent)
    }
    binding.buttonHistory.setOnClickListener {
        val intent = Intent(this,ActivityHistory::class.java)
        startActivity(intent)
    }
}
    private fun configureShazamKitSession(
        developerToken: String?,
    ) {
        try {
            if (developerToken == null) {
                return
            }
            val tokenProvider = DeveloperTokenProvider {
                DeveloperToken(developerToken)
            }
            catalog = ShazamKit.createShazamCatalog(tokenProvider)
            coroutineScope.launch {
                when (val result = ShazamKit.createStreamingSession(
                    catalog, AudioSampleRateInHz.SAMPLE_RATE_44100, 8192
                )) {
                    is ShazamKitResult.Success -> {
                        currentSession = result.data

                    }
                    is ShazamKitResult.Failure -> {
                        result.reason.message?.let { onError(it) }
                    }
                }
                currentSession.let {
                    currentSession.recognitionResults().collect { result: MatchResult ->
                        try {
                            when (result) {
                                is MatchResult.Match -> {
                                    println(result)
                                        result.matchedMediaItems.forEach{
                                            songs.add(SongModel(it.title.toString(),it.artist.toString(),it.artworkURL.toString()))
                                        }.let {
                                            adapter.setData(songs)
                                            saveArrayList(songs!!,"history")
                                        }
                                    stopListening()
                                }
                                is MatchResult.NoMatch -> onError("Not Found")
                                is MatchResult.Error -> onError(result.exception.message)
                            }
                        } catch (e: Exception) {
                            e.message?.let { onError(it) }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.message?.let { onError(it) }
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startListening() {
        try {

            val audioSource = MediaRecorder.AudioSource.DEFAULT
            val audioFormat = AudioFormat.Builder().setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(41_000).build()

            audioRecord =
                AudioRecord.Builder().setAudioSource(audioSource).setAudioFormat(audioFormat)
                    .build()
            val bufferSize = AudioRecord.getMinBufferSize(
                41_000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            audioRecord?.startRecording()
            isRecording = true
            recordingThread = Thread({
                val readBuffer = ByteArray(bufferSize)
                while (isRecording) {
                    val actualRead = audioRecord!!.read(readBuffer, 0, bufferSize)
                    currentSession.matchStream(readBuffer, actualRead, System.currentTimeMillis())
                }
            }, "AudioRecorder Thread")
            recordingThread!!.start()
        } catch (e: Exception) {
            e.message?.let { onError(it) }
            stopListening()
        }
    }


    private fun stopListening() {
        val button = findViewById<Button>(R.id.button)
        if (audioRecord != null) {
            isRecording = false
            audioRecord!!.stop()
            audioRecord!!.release()
            audioRecord = null
            recordingThread = null
            button.isEnabled = true
            button.text = "Search Song"
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

    }

    private fun onError(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        stopListening()
        println(message)
    }

    fun performRecording(){
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_AUDIO_PERMISSION_CODE,
            )
        } else {
            startListening()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onClick(title: String, artist: String, image: String) {
        val intent = Intent(this,ActivityDetail::class.java)
        intent.putExtra("title",title)
        intent.putExtra("artist",artist)
        intent.putExtra("imageString",image)
        startActivity(intent)
    }
    fun saveArrayList(list: ArrayList<SongModel>, key: String?) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor: SharedPreferences.Editor = prefs.edit()
        val gson = Gson()
        val json: String = gson.toJson(list)
        editor.putString(key, json)
        editor.apply()
    }
}


fun MatchResult.Match.toJsonString(): String {
    val itemJsonArray = JSONArray()

    this.matchedMediaItems.forEach { item ->
        item.genres
        val itemJsonObject = JSONObject()
        itemJsonObject.put("title", item.title)
        itemJsonObject.put("subtitle", item.subtitle)
        itemJsonObject.put("shazamId", item.shazamID)
        itemJsonObject.put("appleMusicId", item.appleMusicID)
        item.appleMusicURL?.let {
            itemJsonObject.put("appleMusicUrl", it.toURI().toString())
        }
        item.artworkURL?.let {
            itemJsonObject.put("artworkUrl", it.toURI().toString())
        }
        itemJsonObject.put("artist", item.artist)
        itemJsonObject.put("matchOffset", item.matchOffsetInMs)
        item.videoURL?.let {
            itemJsonObject.put("videoUrl", it.toURI().toString())
        }
        item.webURL?.let {
            itemJsonObject.put("webUrl", it.toURI().toString())
        }

        itemJsonObject.put("genres", JSONArray(item.genres))
        itemJsonObject.put("isrc", item.isrc)
        itemJsonArray.put(itemJsonObject)

    }

    return itemJsonArray.toString()

}