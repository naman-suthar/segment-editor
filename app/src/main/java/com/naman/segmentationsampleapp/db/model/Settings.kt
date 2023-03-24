package com.naman.segmentationsampleapp.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var colorStyle: Int
)