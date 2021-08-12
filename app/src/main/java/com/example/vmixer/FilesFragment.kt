package com.example.vmixer


import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.vmixer.MainActivity.Companion.sortType
import com.example.vmixer.MainActivity.Companion.videoFiles
import java.util.concurrent.TimeUnit


class FilesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var videoLayoutManager: RecyclerView.LayoutManager
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    companion object{
        var refreshedVideoFiles: ArrayList<VideoFiles> = ArrayList()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_files, container, false)

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)

        recyclerView = view.findViewById(R.id.filesRV)
        if(videoFiles != null && videoFiles.size > 0){
            videoAdapter = VideoAdapter(activity as Context/*context*/,videoFiles)
            videoLayoutManager = LinearLayoutManager(activity as Context/*context*/, RecyclerView.VERTICAL,false)
            recyclerView.adapter = videoAdapter
            recyclerView.layoutManager = videoLayoutManager
        }

        //refresh
        swipeRefreshLayout.setOnRefreshListener {
            Log.e("refresh", "onRefresh called from SwipeRefreshLayout")
            swipeRefreshLayout.isRefreshing = true
            // This method performs the actual data-refresh operation.
            // The method calls setRefreshing(false) when it's finished.
            refreshedVideoFiles = getAllVideos(activity as Context)
            videoAdapter = VideoAdapter(activity as Context/*context*/, refreshedVideoFiles)
            recyclerView.adapter = videoAdapter
            swipeRefreshLayout.isRefreshing = false
        }
        return view
    }

    /*override fun onRefresh() {
        Log.e("refresh", "onRefresh called from SwipeRefreshLayout")
        swipeRefreshLayout.isRefreshing = true
        loadRecyclerViewData()
        swipeRefreshLayout.isRefreshing = false
    }???????*/


    private fun getAllVideos(context: Context): ArrayList<VideoFiles>{
        val tempVideoFiles: ArrayList<VideoFiles> = ArrayList()
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DISPLAY_NAME
        )

        // Display videos in order based on their display type.
        var sortOrder: String? = null
        when(sortType){
            "size" -> {sortOrder = "${MediaStore.Video.Media.SIZE} DESC"}
            "name" -> {sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"}
            "date" -> {sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"}
            else -> {sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"}
        }

        val cursor: Cursor? = context.contentResolver.query(uri,projection,null,null,sortOrder)
        if(cursor != null){
            while(cursor.moveToNext()) {
                val id = cursor.getLong(0).toString()
                val path = cursor.getString(1)
                val title = cursor.getString(2)
                val sizeInt = cursor.getInt(3)
                val dataAdded = cursor.getString(4)
                val durationInt = cursor.getInt(5)
                val filename = cursor.getString(6)

                val size = sizeConversion(sizeInt)
                //Log.e("size",size)
                val duration = durationConversion(durationInt)
                //Log.e("duration",duration)

                val videoFiles: VideoFiles =
                    VideoFiles(id, path, title, filename, size, dataAdded, duration)

                //just to check file or not
                //uLog.e("Path", path)
                //  /storage/sd_card/VideoDir/abc/file.mp4   --index of last slash before fileName--
                val slashFirstIndex: Int = path.lastIndexOf("/")
                //  /storage/sd_card/VideoDir/abc  <--only this,  --last is excluded--
                val subString: String = path.substring(0, slashFirstIndex)


                if (!MainActivity.folderList.contains(subString)) {
                    MainActivity.folderList.add(subString)
                }
                if (durationInt >= 1000){
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