package com.cancionero.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "culto_musicians",
    primaryKeys = ["cultoId", "musicianId"],
    foreignKeys = [
        ForeignKey(
            entity = Culto::class,
            parentColumns = ["id"],
            childColumns = ["cultoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Musician::class,
            parentColumns = ["id"],
            childColumns = ["musicianId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cultoId"), Index("musicianId")]
)
data class CultoMusician(
    val cultoId: Long,
    val musicianId: Long
)