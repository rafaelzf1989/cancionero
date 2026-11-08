package com.cancionero.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val tempoBpm: Int? = null,
    val tempoText: String? = null,
    val songKey: String,
    val sourceLaCuerda: Boolean = false,
    val sourceCancionero: Boolean = false,
    val libretaNumber: Int? = null,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val moment: String? = null,
    val tags: String? = null
)
