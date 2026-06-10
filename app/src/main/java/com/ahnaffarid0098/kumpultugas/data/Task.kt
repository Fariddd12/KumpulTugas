package com.ahnaffarid0098.kumpultugas.data

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val priority: String
)