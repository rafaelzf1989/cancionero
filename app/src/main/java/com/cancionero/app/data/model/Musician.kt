package com.cancionero.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "musicians")
data class Musician(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val roles: String,
    val phone: String? = null,
    val notes: String? = null
) {
    fun rolesList(): List<String> =
        roles.split(",").map { it.trim() }.filter { it.isNotBlank() }
}