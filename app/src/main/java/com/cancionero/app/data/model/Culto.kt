package com.cancionero.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cultos")
data class Culto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dayOfWeek: Int,      // 1=lunes, 2=martes ... 7=domingo
    val time: String,        // "19:00"
    val setlistId: Long? = null
)
