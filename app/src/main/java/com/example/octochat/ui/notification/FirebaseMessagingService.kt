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


import android.app.NotificationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.octochat.ui.notification.sendNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from
     *                      Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        /* DOC STEP 3.5
        If your app is in the background, the FCM message will trigger an
        automatic notification and the onMessageReceived() function will receive
        the remoteMessage object only when the user clicks the notification.

        Maximum payload for both message types is 4KB (except when sending
        messages from the Firebase console, which enforces a 1024 character
        limit).
        DOC END STEP 3.5 */

        // TODO STEP 3.5 - Check messages for data
        // Check if message contains a data payload.
        remoteMessage.data.let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
        }
        // TODO END STEP 3.5

        /* DOC STEP 3.6
        App behavior when receiving messages that include both notification and
        data payloads depends on whether the app is in the background or the
        foreground—essentially, whether or not it is active at the time of
        receipt.

        1) When in the background, if the message has a notification payload,
        the notification is automatically shown in the notification tray. If the
        message also has a data payload, the data payload will be handled by the
        app when the user taps on the notification.

        2) When in the foreground, if the message notification has payloads, the
        notification will not appear automatically. The app needs to decide how
        to handle the notification in the onMessageReceived() function. If the
        message also has data payload, both payloads will be handled by the app.

        DOC END STEP 3.6 */

        // TODO STEP 3.6 - Check messages for notification and call
        // sendNotification
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.body!!)
        }
        // TODO END STEP 3.6

    }
    // [END receive_message]

    // TODO STEP 3.2 - Log registration token
    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when
     * the InstanceID token is initially generated so this is where you would
     * retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(token)
    }
    // [END on_new_token]

//    /**
//     * Persist token to third-party (your app) servers.
//     *
//     * @param token The new token.
//     */
//    private fun sendRegistrationToServer(token: String?) {
//        // Implement this method to send token to your app server.
//    }
    // TODO END STEP 3.2

    /**
     * Create and show a simple notification containing the received FCM
     * message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageBody: String) {
        val notificationManager = ContextCompat.getSystemService(
            applicationContext, NotificationManager::class.java) as
                NotificationManager
        notificationManager.sendNotification(messageBody, applicationContext)
    }

    companion object {
        private const val TAG = "FirebaseMsgService"
    }
}
