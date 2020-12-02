/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package com.example.octochat.ui.notification

import android.util.Log
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import com.example.octochat.R
import androidx.core.app.NotificationCompat
import com.example.octochat.MainActivity
import com.example.octochat.ui.notification.receiver.SnoozeReceiver


// Notification ID.
private const val NOTIFICATION_ID = 0
private const val REQUEST_CODE = 0
private const val FLAGS = PendingIntent.FLAG_ONE_SHOT

// TODO STEP 1.1 - Extension function to send messages (GIVEN)
/**
 * Builds and delivers the notification.
 *
 * @param applicationContext Activity context.
 */
fun NotificationManager.sendNotification(
    messageBody: String,
    applicationContext: Context) {
    // Create the content intent for the notification, which launches
    // this activity
    // TODO STEP 1.11 - Create Intent
    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    // TODO END STEP 1.11

    /* DOC STEP 1.12 - Create PendingIntent
    You created the intent, but the notification is displayed outside your app.
    To make an intent work outside your app, you need to create a new
    PendingIntent.

    PendingIntent grants rights to another application or the system to perform
    an operation on behalf of your application. A PendingIntent itself is simply
    a reference to a token maintained by the system describing the original data
    used to retrieve it. This means that, even if its owning application's
    process is killed, the PendingIntent itself will remain usable from other
    processes it has been given to. In this case, the system will use the
    pending intent to open the app on behalf of you, regardless of whether or
    not the timer app is running.
    DOC END STEP 1.12 */
    Log.d("this NotifUtils", "${contentIntent.toString()}")
    // TODO STEP 1.12 - Create PendingIntent
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    // TODO END STEP 1.12

    // TODO STEP 2.0 - Add style
    val msgImage = BitmapFactory.decodeResource(
        applicationContext.resources,
        R.drawable.ic_email
    )

    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(msgImage)
        .bigLargeIcon(null)
    // TODO END STEP 2.0

    // TODO STEP 2.2 - Add snooze action
    val snoozeIntent = Intent(applicationContext, SnoozeReceiver::class.java)
    val snoozePendingIntent: PendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        REQUEST_CODE,
        snoozeIntent,
        FLAGS
    )
    // TODO END STEP 2.2

    /* DOC STEP 1.2 - Get an instance of NotificationCompat.Builder
    Starting with API level 26, all notifications must be assigned to a channel.
    Notification Channels are a way to group notifications. By grouping together
    similar types of notifications, developers and users can control all of the
    notifications in the channel. Once a channel is created, it can be used to
    deliver any number of notifications.
    DOC END STEP 1.2 */

    // TODO STEP 1.2 - Get an instance of NotificationCompat.Builder
    // Build the notification
    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.msg_notification_channel_id)
    )
        // TODO END STEP 1.2

        // TODO STEP 1.3 - Set title, text and icon to builder
        .setSmallIcon(R.drawable.ic_email)
        .setContentTitle(applicationContext.getString(
            R.string.notification_title))
        .setContentText(messageBody)
        // TODO END STEP 1.3

        // TODO STEP 1.13 - Set content intent
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        // TODO END STEP 1.13

        /* DOC STEP 2.1
        - OnePlus A3003, Android 9
        - Genymotion 2.14.0, Google Pixel 2, Android 8.0.0
        If I open the notification by pulling down the status bar, it is
        expanded straightway.
        DOC END STEP 2.1 */

        // TODO STEP 2.1 - Add style to builder
        .setStyle(bigPicStyle)
        .setLargeIcon(msgImage)
        // TODO END STEP 2.1

        // TODO STEP 2.3 - Add snooze action
        .addAction(
            R.drawable.circle,
            applicationContext.getString(R.string.snooze),
            snoozePendingIntent
        )
        // TODO END STEP 2.3

        /* DOC STEP 2.5 - Set priority
        To support devices running Android 7.1 (API level 25) or lower, you
        must also call setPriority() for each notification, using a priority
        constant from the NotificationCompat class.
        DOC END STEP 2.5 */

        // TODO STEP 2.5 - Set priority
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    // TODO END STEP 2.5

    /* DOC STEP 1.4 - Call notify
    NOTIFICATION_ID represents the current notification instance and is needed
    for updating or canceling this notification. Since your app will only have
    one active notification at a given time, you can use the same ID for all
    your notifications.
    DOC END STEP 1.4 */

    // TODO STEP 1.4 - Call notify
    notify(NOTIFICATION_ID, builder.build())
    // TODO END STEP 1.4
}

// TODO END STEP 1.1

// TODO STEP 1.14 - Cancel all notifications
/**
 * Cancels all notifications.
 *
 */
fun NotificationManager.cancelNotifications() {
    cancelAll()
}
// TODO END STEP 1.14