package com.example.vmixer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView



class VideoFolderAdapter(val context: Context, private val folderVideoFiles: ArrayList<VideoFiles>): RecyclerView.Adapter<VideoFolderAdapter.ViewHolder>(){

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.img_thumbnail)
        val videoDuration: TextView = view.findViewById(R.id.video_duration)
        val fileName: TextView = view.findViewById(R.id.video_file_name)
        val menuMore: ImageView = view.findViewById(R.id.menu_more)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context/*context*/)
            .inflate(R.layout.video_item,parent,false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = folderVideoFiles[position]
        holder.fileName.text = video.fileName
        holder.videoDuration.text = video.duration.toString()
        //Picasso.get().load(video.path).into(holder.thumbnail)                                                        //using picasso
        GlideApp.with(holder.thumbnail.context/*context*/).load(video.path).into(holder.thumbnail)                        //**context in glide (or -context- only can work)

        holder.itemView.setOnClickListener {
            val intent = Intent(context/*context*/,PlayerActivity::class.java)
            intent.putExtra("position",position)
            intent.putExtra("flag",2)
            context.startActivity(intent)
        }
    }



    override fun getItemCount(): Int {
        return folderVideoFiles.size
    }
}