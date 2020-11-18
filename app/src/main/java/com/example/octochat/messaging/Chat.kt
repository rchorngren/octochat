package com.example.octochat.messaging

import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

class Chat(val chatId: String,
           val otherUser: User,
           var lastMessage: Message? = null,
           @ServerTimestamp val timestamp: Date? = null) : Serializable