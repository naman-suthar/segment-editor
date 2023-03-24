package com.naman.segmentationsampleapp.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "color_table")
data class RangeBarDto(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "color_table_id")
    var id : Int,

    @ColumnInfo(name = "starting_value")
    var seekBarStart : Int,

    @ColumnInfo(name = "ending_value")
    var seekBarEnd : Int,

    @ColumnInfo(name = "segment_color")
    var segmentColor : Int

)
/*
General Settings entity
//colorStyle = ENUM(any of the 4)
 */
