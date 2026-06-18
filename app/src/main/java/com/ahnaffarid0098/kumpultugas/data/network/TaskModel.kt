package com.ahnaffarid0098.kumpultugas.data.network

data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String = "",
    val priority: String,
    val imageUrl: String,
    val userEmail: String
)