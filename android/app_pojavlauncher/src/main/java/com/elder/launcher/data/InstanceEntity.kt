package com.elder.launcher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instances")
data class InstanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val version: String,
    val edition: String = "JAVA",
    val lastPlayed: Long = 0L
)