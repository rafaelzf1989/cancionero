package com.cancionero.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cultos")
data class Culto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dateMillis: Long,
    val setlistId: Long? = null,
    val leader: String? = null
)