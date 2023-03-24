package com.naman.segmentationsampleapp.db.app_db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.naman.segmentationsampleapp.db.model.ColorHistory
import com.naman.segmentationsampleapp.db.model.GridData
import com.naman.segmentationsampleapp.db.model.RangeBarDto
import com.naman.segmentationsampleapp.db.model.Settings

@Dao
interface MainDao {

    @Insert
    suspend fun insertRangeBar(rangebar: RangeBarDto) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRangeBars(rangeBarList: List<RangeBarDto>)

    @Update
    suspend fun updateRangeBar(rangeBar: RangeBarDto) : Int

    @Query("DELETE FROM color_table")
    suspend fun deleteAllRangeBars() : Int

    @Query("SELECT * FROM color_table")
    fun getAllRangeBars(): LiveData<List<RangeBarDto>>


    //Grid Data
    @Insert
    suspend fun insertGridColor(gridData: GridData) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGrid(gridData: List<GridData>)

    @Query("UPDATE grid_data SET grid_color =:color WHERE grid_data_id=:position")
    suspend fun updateGridColor(position: Int,color: String) : Int

    @Query("DELETE FROM grid_data")
    suspend fun deleteGridData() : Int

    @Query("SELECT * FROM grid_data")
    fun getAllGridData():LiveData<List<GridData>>


    //COlorHistory
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColor(colorHistory: ColorHistory)

    @Query("SELECT * FROM color_history ORDER BY id DESC LIMIT 20")
    fun getRecentColors(): LiveData<List<ColorHistory>>

    @Query("DELETE FROM color_history WHERE id NOT IN (SELECT id FROM color_history ORDER BY id DESC LIMIT 20)")
    suspend fun deleteOldColors()

    //Settings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: Settings)

    @Query("SELECT * FROM settings")
    fun getSettings(): LiveData<List<Settings>>

    @Query("UPDATE settings SET colorStyle = :colorStyle WHERE id = 1")
    suspend fun updateColorStyle(colorStyle: Int)

}