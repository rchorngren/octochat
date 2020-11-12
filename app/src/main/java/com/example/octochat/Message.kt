package com.example.octochat

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Message (val sender: String? = null, var text: String = "", @ServerTimestamp val timestamp: Date? = null)