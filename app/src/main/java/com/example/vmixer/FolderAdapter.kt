package com.example.vmixer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FolderAdapter(val context: Context, private val folderList: ArrayList<String>, val videoFiles: ArrayList<VideoFiles>) : RecyclerView.Adapter<FolderAdapter.FolderHolder>() {

    class FolderHolder(view: View): RecyclerView.ViewHolder(view) {
        val folderImage: ImageView = view.findViewById(R.id.folderImage)
        val folderName: TextView = view.findViewById(R.id.folderName)
        val file_count: TextView = view.findViewById(R.id.file_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderHolder {
        val view = LayoutInflater.from(parent.context/*context*/)
            .inflate(R.layout.folder_item,parent,false)

        return FolderHolder(view)
    }

    override fun onBindViewHolder(holder: FolderHolder, position: Int) {
        val folder: String = folderList[position]
        //-index of last slash before folderName--
        val slashSecondIndex: Int = folder.lastIndexOf("/")
        // -------------/abc     <----abc is folderName excluding slashes(/)---->
        val folderName: String = folder.substring(slashSecondIndex+1)

        holder.folderName.text = folderName
        holder.file_count.text = fileCount(folderName).toString()

        holder.itemView.setOnClickListener {
            val intent: Intent = Intent(context,VideoFolderActivity::class.java)
            intent.putExtra("folderName",folderName)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return folderList.size
    }

    private fun fileCount(folderName: String): Int{
        var countFiles: Int = 0
        for(videoFiles: VideoFiles in videoFiles){
            if(videoFiles.path.substring(0,videoFiles.path.lastIndexOf("/")).endsWith(folderName)){
                countFiles++
            }
        }
        return countFiles
    }
}