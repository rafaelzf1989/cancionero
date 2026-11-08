package com.cancionero.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moments")
data class Moment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val position: Int
)
