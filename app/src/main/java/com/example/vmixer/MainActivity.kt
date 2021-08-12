package com.example.vmixer

import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    lateinit var bottomNavView: BottomNavigationView
    lateinit var relativeLayout: RelativeLayout
    private lateinit var toolBar: androidx.appcompat.widget.Toolbar

    private var flag: Boolean = false //flag

    companion object {
        const val REQUEST_PERMISSION = 999
        const val TAG: String = "PermissionActivity"
        var videoFiles: ArrayList<VideoFiles> = ArrayList()
        var folderList: ArrayList<String> = ArrayList()
        var sortType: String? = null
    }
    //create menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.setting_option,menu)
        //flag set visibility of menu
        return flag
    }
    //onclick menu
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.sort_Size -> {
            sortType = "size"
            videoFiles = getAllVideos(this)
            fragmentTransaction(FilesFragment())
            true
        }
        R.id.sort_Name -> {
            sortType = "name"
            videoFiles = getAllVideos(this)
            fragmentTransaction(FilesFragment())
            true
        }
        R.id.sort_Date -> {
            sortType = "date"
            videoFiles = getAllVideos(this)
            fragmentTransaction(FilesFragment())
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavView = findViewById(R.id.bottomNavView)
        relativeLayout = findViewById(R.id.rr)

        //set actionBar
        toolBar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolBar)

        //check permission called
        permission()

        bottomNavView.setOnItemSelectedListener {  item ->                                          //  it.itemId also work { when(it.itemId){}  }
            when (item.itemId) {
                R.id.folderList -> {
                    Toast.makeText(this, "Folders", Toast.LENGTH_SHORT).show()
                    fragmentTransaction(FolderFragment())
                    //flag hide the menu item & invalidateOptionsMenu() call again onCreateOptionMenu--
                    flag = false
                    invalidateOptionsMenu()
                    item.isChecked
                }
                R.id.fileList -> {
                    Toast.makeText(this, "Files", Toast.LENGTH_SHORT).show()
                    fragmentTransaction(FilesFragment())
                    //flag display the menu item & invalidateOptionsMenu() call again onCreateOptionMenu--
                    flag = true
                    invalidateOptionsMenu()
                    item.isChecked
                }
            }
            true
        }
    }

    override fun onStart() {
        super.onStart()
    }



    // show result of request method -> grantResult store granted permissions by user
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                videoFiles = getAllVideos(this)
                fragmentTransaction(FolderFragment())
                //action
            }else{
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                finish()
                /*
                **To manually allow permission
                Toast.makeText(this, "Allow required permission", Toast.LENGTH_SHORT).show()
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle("Permission")
                    .setMessage("Allow permission Manually in Settings > permissions")
                    .setPositiveButton("Never") { dialog,which ->
                        finish()
                    }
                    .setNegativeButton("Settings"){dialog,which ->
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", packageName, null)
                        })
                    }
                builder.create().show()
                */
                }
            }
        }
    //permission function
    private fun permission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Check if permission is not granted
            Log.d(TAG, "Permission for contacts is not granted")
            //Checks permission, if permission has been denied shows rationale for the permission
            if(shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // This condition only becomes true if the user has denied the permission previously
                    showRationaleDialog(
                        getString(R.string.rationale_title),
                        getString(R.string.rationale_desc),
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        REQUEST_PERMISSION)
                }
                else {
                // Perform a permission check
                Log.d(TAG, "Checking permission")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION
                )
            }
        }else{
            // Permission is already granted!
            Toast.makeText(this, "Refreshing Storage", Toast.LENGTH_SHORT).show()
            videoFiles = getAllVideos(this)
            fragmentTransaction(FolderFragment())
            //action
        }
    }

    //showRationaleDialog
    //Shows rationale dialog for displaying why the app needs permission
    // Only shown if the user has denied the permission request previously
    private fun showRationaleDialog(title: String, message: String, permission: String, requestCode: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok") { dialog,which ->
                requestPermissions(arrayOf(permission), requestCode)
            }
        builder.create().show()
    }



    //get all videos
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


                if (!folderList.contains(subString)) {
                    folderList.add(subString)
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


    //fragmentTransactions
    private fun fragmentTransaction(fragment: Fragment){
        val fragmentTransaction: FragmentTransaction = supportFragmentManager
            .beginTransaction()
        fragmentTransaction.replace(R.id.mainFragment,fragment)
        fragmentTransaction.commit()
    }






}


