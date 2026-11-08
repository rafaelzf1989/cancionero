package com.cancionero.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "culto_setlists",
    primaryKeys = ["cultoId", "setlistId"],
    foreignKeys = [
        ForeignKey(
            entity = Culto::class,
            parentColumns = ["id"],
            childColumns = ["cultoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Setlist::class,
            parentColumns = ["id"],
            childColumns = ["setlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cultoId"), Index("setlistId")]
)
data class CultoSetlist(
    val cultoId: Long,
    val setlistId: Long,
    val position: Int
)