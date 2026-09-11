package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auto_click_logs")
data class AutoClickLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val stepName: String,
    val matchedKeyword: String,
    val packageName: String,
    val success: Boolean = true,
    val detailMessage: String = ""
)
