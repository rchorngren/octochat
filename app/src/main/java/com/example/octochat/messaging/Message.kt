package com.example.octochat.messaging

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Message (val sender: String? = null,
                    var text: String = "",
                    @ServerTimestamp val timestamp: Date? = null)