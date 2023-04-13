package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.listeners.OnClickListener
import com.example.myapplication.models.SongModel

class SongListAdapter  (
    var songs:ArrayList<SongModel>,
    private var listener:OnClickListener

) : RecyclerView.Adapter<SongListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.artist.text = songs[position].artistName
        Glide.with(holder.itemView.context).load(songs[position].songImage).placeholder(R.drawable.mnote).into(holder.imageView)
        holder.title.text = songs[position].title
        holder.imageView.setOnClickListener { listener.onClick(songs[position].title,songs[position].artistName,songs[position].songImage) }


    }

    override fun getItemCount(): Int {
        return songs.count()
    }


    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        var artist: TextView = itemView.findViewById(R.id.artist_tv)
        var imageView : ImageView = itemView.findViewById(R.id.song_image)
        var title : TextView = itemView.findViewById(R.id.title_tv)
    }

    fun setData(history: ArrayList<SongModel>) {
        songs = history
        notifyDataSetChanged()
    }

}