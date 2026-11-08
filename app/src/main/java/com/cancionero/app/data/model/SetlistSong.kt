package com.cancionero.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "setlist_songs",
    primaryKeys = ["setlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = Setlist::class,
            parentColumns = ["id"],
            childColumns = ["setlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("setlistId"), Index("songId")]
)
data class SetlistSong(
    val setlistId: Long,
    val songId: Long,
    val position: Int
)