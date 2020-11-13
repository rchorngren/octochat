package com.example.octochat.messaging

import java.io.Serializable

class Chat(val chatId: String, val otherUser: User, lastMessage: String? = "No messages yet") : Serializable