package com.example.vmixer

import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.TimeUnit

class VideoFolderActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var videoFolderAdapter: VideoFolderAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager
    private var selectedFolderName: String? = null

    companion object {
        var folderVideoFiles: ArrayList<VideoFiles> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_folder)
        //set actionBar
        setSupportActionBar(findViewById(R.id.folder_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        recyclerView = findViewById(R.id.FolderVideoRV)
        selectedFolderName = intent.getStringExtra("folderName")

        if(selectedFolderName != null){
            folderVideoFiles = getAllVideos(this, selectedFolderName)
        }

        if(folderVideoFiles != null && folderVideoFiles.size > 0){
            videoFolderAdapter = VideoFolderAdapter(this,folderVideoFiles)
            recyclerView.adapter = videoFolderAdapter
            layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
            recyclerView.layoutManager = layoutManager
        }
    }

    //get all videos
    private fun getAllVideos(context: Context,folderName: String?): ArrayList<VideoFiles>{
        val tempVideoFiles: ArrayList<VideoFiles> = ArrayList()
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )

        val selection: String = MediaStore.Video.Media.DATA + " like?"
        val selectionArgs = arrayOf("%"+ folderName +"%")

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        val cursor: Cursor? = context.contentResolver.query(uri,projection,selection,selectionArgs,sortOrder)
        if(cursor != null){
            while(cursor.moveToNext()){
                val id = cursor.getLong(0).toString()
                val path = cursor.getString(1)
                val title = cursor.getString(2)
                val sizeInt = cursor.getInt(3)
                val dataAdded = cursor.getString(4)
                val durationInt = cursor.getInt(5)
                val filename = cursor.getString(6)
                val bucketName = cursor.getString(7)

                val size = sizeConversion(sizeInt)
                //Log.e("size",size)
                val duration = durationConversion(durationInt)
                //Log.e("duration",duration)

                val videoFiles: VideoFiles = VideoFiles(id,path,title,filename,size,dataAdded,duration)

                //just to check file or not
                //Log.d("Path",path)
                //Log.e("bucket",bucketName)
                if (folderName!!.endsWith(bucketName) && durationInt >= 1000) {
                    tempVideoFiles.add(videoFiles)
                }
            }
            cursor.close()
        }
        return tempVideoFiles
    }

    //calculation functions
    private fun durationConversion(durationInt: Int): String{
        //val sec = durationInt/1000
        //val min = durationInt/(1000*60)
        val min = TimeUnit.MILLISECONDS.toMinutes(durationInt.toLong())
        val sec = TimeUnit.MILLISECONDS.toSeconds(durationInt.toLong())
        val hour = TimeUnit.MILLISECONDS.toHours(durationInt.toLong()).toInt()
        if(hour == 0) {
            return "$min:${sec % 60}"
        }else{
            return "$hour:${min%60}:${sec%60}"
        }
    }
    private fun sizeConversion(sizeInt: Int): String{
        //val sizeItoF = "%.2f".format(sizeInt.toFloat()/(1000*1000))
        val sizeItoF = "%.2f".format(sizeInt.toFloat()/(1024*1024))
        return "$sizeItoF mb"
    }
}