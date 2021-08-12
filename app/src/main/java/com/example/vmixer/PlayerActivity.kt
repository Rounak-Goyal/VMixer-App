package com.example.vmixer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.vmixer.MainActivity.Companion.TAG
import com.example.vmixer.MainActivity.Companion.videoFiles
import com.example.vmixer.VideoFolderActivity.Companion.folderVideoFiles
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util

class PlayerActivity : AppCompatActivity() {

    companion object {
    private const val TAG = "PlayerActivity"
    }

    private lateinit var playerView: PlayerView
    private  var simpleExoPlayer: SimpleExoPlayer? = null
    private var position: Int = -1                             /*null exception*/
    private var flag: Int = 0
    private var path: String? = null

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setFullScreen
        setFullScreen()
        //hideActionBar
        supportActionBar?.hide()
        //before setContent
        setContentView(R.layout.activity_player)


        playerView = findViewById(R.id.playerView)
        position = intent.getIntExtra("position",-1)
        flag = intent.getIntExtra("flag",0)

        when(flag){
            1 ->  path = videoFiles[position].path
            2 ->  path = folderVideoFiles[position].path
        }

        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.getLong("PLAYBACK_POSITION", 0);
            currentWindow = savedInstanceState.getInt("CURRENT_WINDOW_INDEX", 0);
            playWhenReady = savedInstanceState.getBoolean("AUTOPLAY", true);
        }



        //initializePlayer(path,position)
        /*if(path != null && position != -1){
            val uri: Uri = Uri.parse(path)
            simpleExoPlayer = SimpleExoPlayer.Builder(this).build().also {
                val factory: DataSource.Factory =
                    DefaultDataSourceFactory(this, Util.getUserAgent(this, "VMixer"))
                val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()
                val mediaSource: MediaSource =
                    ProgressiveMediaSource.Factory(factory, extractorsFactory)
                        .createMediaSource(uri)
                playerView.player = simpleExoPlayer
                playerView.keepScreenOn = true
                simpleExoPlayer.prepare(mediaSource)
                simpleExoPlayer.playWhenReady = true
            }
        }*/
    }

    private fun initializePlayer(path: String?, position:Int){
        if(path != null && position != -1){
            val uri: Uri = Uri.parse(path)
            val trackSelector = DefaultTrackSelector(this).apply {
                setParameters(buildUponParameters().setMaxVideoSizeSd())
            }


            simpleExoPlayer = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build().also { exoplayer ->
                val factory: DataSource.Factory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "VMixer"))
                val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()
                val mediaSource: MediaSource = ProgressiveMediaSource.Factory(factory, extractorsFactory).createMediaSource(uri)   // (uri) -> (MediaItem.fromUri(uri))
                playerView.player = exoplayer
                playerView.keepScreenOn = true
                exoplayer.playWhenReady = playWhenReady
                exoplayer.seekTo(currentWindow, playbackPosition)
                //listener added
                exoplayer.addListener(playbackStateListener)
                exoplayer.prepare(mediaSource)
                //exoplayer.setMediaSource(mediaSource)
                //exoplayer.prepare()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer(path,position)
        }

    }
    override fun onResume() {
        super.onResume()
        hideSystemUi()
        resumePlayer()
        if ((Util.SDK_INT < 24 || simpleExoPlayer == null)) {
            initializePlayer(path, position)
        }
        //Log.e("current",currentWindow.toString())
        //Log.e("currentPos",playbackPosition.toString())
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }
    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun pausePlayer() {
        playWhenReady = simpleExoPlayer?.playWhenReady!!
        playbackPosition = simpleExoPlayer?.currentPosition!!
        currentWindow = simpleExoPlayer?.currentWindowIndex!!
    }
    private fun resumePlayer() {
        simpleExoPlayer?.playbackState
        simpleExoPlayer?.playWhenReady = playWhenReady
        simpleExoPlayer?.seekTo(currentWindow, playbackPosition)
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun releasePlayer(){
        if(simpleExoPlayer != null) {
            playbackPosition = simpleExoPlayer?.currentPosition!!
            currentWindow = simpleExoPlayer?.currentWindowIndex!!
            playWhenReady = simpleExoPlayer?.playWhenReady!!
            //listener removed
            simpleExoPlayer?.removeListener(playbackStateListener)
            simpleExoPlayer?.release()
            simpleExoPlayer = null
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        /*
        * A simple configuration change such as screen rotation will destroy this activity
        * so we'll save the player state here in a bundle (that we can later access in onCreate) before everything is lost
        * NOTE: we cannot save player state in onDestroy like we did in onPause and onStop
        * the reason being our activity will be recreated from scratch and we would have lost all members (e.g. variables, objects) of this activity
        */
        if (simpleExoPlayer == null) {
            outState.putLong("PLAYBACK_POSITION", playbackPosition)
            outState.putInt("CURRENT_WINDOW_INDEX", currentWindow)
            outState.putBoolean("AUTOPLAY", playWhenReady)
        }
    }

    //setFullScreen
    private fun setFullScreen(){
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }


    //Declare a private member of type Player.EventListener in the PlayerActivity
    private val playbackStateListener: Player.Listener = playbackStateListener()

}


//Implement the Player.EventListener interface in a factory function outside the PlayerActivity class
private fun playbackStateListener() = object : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        val stateString: String = when (playbackState) {
            ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
            ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
            ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
            ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
            else -> "UNKNOWN_STATE             -"
        }
        Log.d(TAG, "changed state to $stateString")
    }
}
