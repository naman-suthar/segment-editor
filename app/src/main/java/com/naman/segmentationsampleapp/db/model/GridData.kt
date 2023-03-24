package com.naman.segmentationsampleapp.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grid_data")
data class GridData (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "grid_data_id")
    var id : Int,
    @ColumnInfo(name = "grid_sequence")
    var seqNumber : Int,

    @ColumnInfo(name = "grid_color")
    var gridColor : Int
)
