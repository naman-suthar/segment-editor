package com.naman.segmentationsampleapp.db.app_db

import com.naman.segmentationsampleapp.db.model.ColorHistory
import com.naman.segmentationsampleapp.db.model.GridData
import com.naman.segmentationsampleapp.db.model.RangeBarDto
import com.naman.segmentationsampleapp.db.model.Settings

class MainRepo (private val mainDao: MainDao) {
    val rangeBarsList = mainDao.getAllRangeBars()
    val gridData = mainDao.getAllGridData()
    val colorHistory = mainDao.getRecentColors()
    val settings = mainDao.getSettings()
    suspend fun insertRangeBars(list: List<RangeBarDto>){
        return mainDao.insertAllRangeBars(list)
    }
    suspend fun deleteAllTables() : Int{
        return mainDao.deleteAllRangeBars()
    }
    //Grid Data
    suspend fun insertGridColor(gridData: GridData) : Long{
        return mainDao.insertGridColor(gridData)
    }
    suspend fun insertAllGrid(gridData: List<GridData>)  {
        return mainDao.insertAllGrid(gridData)
    }
    suspend fun updateGridColor(position:Int , color :String ) : Int{
        return mainDao.updateGridColor(position,color)
    }
    suspend fun deleteGridData() : Int{
        return mainDao.deleteGridData()
    }

    suspend fun insertColor(colorHistory: ColorHistory){
        return mainDao.insertColor(colorHistory)
    }

    suspend fun deleteOldColors(){
        return mainDao.deleteOldColors()
    }

    suspend fun insert(settings: Settings) {
        mainDao.insertSettings(settings)
    }
    suspend fun updateColorStyle(colorStyle: Int) {
        mainDao.updateColorStyle(colorStyle)
    }
}