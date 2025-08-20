package com.sun.japaneselisteningtrainer.data.model

data class Folder (
    val id : Int = 0,
    val name: String = "",
    val description: String = "",
    val createdAt: Long = 0,
) {
    val formatCreatedAt: String
        get() = formatDateTime(createdAt)
}


