package com.midterm.chitchatter.ui.chat

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.midterm.chitchatter.Manifest
import com.midterm.chitchatter.R
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.models.ChannelMediaOptions
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import io.agora.rtc.RtcEngineConfig


class VideoCallActivity : AppCompatActivity() {

    private lateinit var rtcEngine: RtcEngine
    private val appId = "048c998ecbc5414f83c3d9cf23c042e8"
    private val channelName = "testChannel"
    private val token = "YOUR_AGORA_TOKEN"
    private val PERMISSION_REQ_ID = 22

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_video_call)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val senderemail = intent.getStringExtra("senderemail")
        val receiveremail = intent.getStringExtra("receiveremail")
        if (checkPermissions()) {
            initializeAndJoinChannel()
            setupLocalVideo()
            joinChannel()
        } else {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID)
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Listen for remote hosts in the channel to obtain the UID information of the hosts
        override fun onUserJoined(uid: Int, elapsed: Int) {
        }
    }

    private fun initializeAndJoinChannel() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            rtcEngine = RtcEngine.create(config)
            rtcEngine.enableVideo()
            rtcEngine.setVideoEncoderConfiguration(VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            ))
            rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            rtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        } catch (e: Exception) {
            throw RuntimeException("Check the error.")
        }
        rtcEngine.joinChannel(token, channelName, "", 0)
    }

    private fun getRequiredPermissions(): Array<String> {
        // Determine the permissions required when targetSDKVersion is 31 or above
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf<String>(
                android.Manifest.permission.RECORD_AUDIO,  // Record audio permission
                android.Manifest.permission.READ_PHONE_STATE,  // Read phone state permission
                android.Manifest.permission.BLUETOOTH_CONNECT // Bluetooth connection permission
            )
        } else {
            arrayOf<String>(
                android.Manifest.permission.RECORD_AUDIO,
            )
        }
    }

    private fun checkPermissions(): Boolean {
        for (permission in getRequiredPermissions()) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun setupLocalVideo() {
        val localContainer = findViewById<FrameLayout>(R.id.local_video_view_container)
        val localView = RtcEngine.CreateRendererView(baseContext)
        localContainer.addView(localView)
        rtcEngine.setupLocalVideo(VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    private fun joinChannel() {
        rtcEngine.joinChannel(token, channelName, "", 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        rtcEngine.leaveChannel()
        RtcEngine.destroy()
    }
}
