package com.example.octochat.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.octochat.R
import androidx.appcompat.app.AppCompatActivity
import com.example.octochat.ui.notification.model.NotifViewModel
import android.widget.Button
import android.util.Log


class Notification : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)


        val viewModel = ViewModelProvider(this).get(
            NotifViewModel::class.java)

        // TODO STEP 1.7 - Call create channel
        createChannel(
            getString(R.string.msg_notification_channel_id),
            getString(R.string.msg_notification_channel_name)
        )

        // accessing button
        val btn = findViewById<Button>(R.id.btnTestNotification)

        btn.setOnClickListener {

            // TODO STEP 1.7 - Call create channel
            createChannel(
                getString(R.string.msg_notification_channel_id),
                getString(R.string.msg_notification_channel_name)
            )
        }
    }


    private fun createChannel(channelId: String, channelName: String) {
        // checking if android version is greater than oreo(API 26) or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            )

            .apply {
                setShowBadge(false)
            }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Building a notification"

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

            Log.d("this notification class", "${notificationChannel.toString()}")
        }

    }
    companion object {
        fun newInstance() = Notification()
    }
}