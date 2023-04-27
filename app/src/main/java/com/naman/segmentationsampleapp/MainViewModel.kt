package com.naman.segmentationsampleapp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.ijp.segmentation_editor.extras.model.RangeBarArray
import com.naman.segmentationsampleapp.db.app_db.MainRepo
import com.naman.segmentationsampleapp.db.model.ColorHistory
import com.naman.segmentationsampleapp.db.model.GridData
import com.naman.segmentationsampleapp.db.model.RangeBarDto
import com.naman.segmentationsampleapp.db.model.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val mainRepo: MainRepo) : ViewModel() {
    val allRangeBars = mainRepo.rangeBarsList
    val gridData = mainRepo.gridData
    val colorHistory = mainRepo.colorHistory
    val settings = mainRepo.settings

    private val _tempRangeBarArray: MutableStateFlow<MutableList<RangeBarArray>?> = MutableStateFlow(
       null
    )
    private val _tempRangeBarArrayLD: MutableLiveData<MutableList<RangeBarArray>?> = MutableLiveData(
        null
    )
    val tempArrayState = _tempRangeBarArray.asStateFlow()

    fun updateArrayState(list: MutableList<RangeBarArray>, from: String) {

//        _tempRangeBarArray.value = null
        _tempRangeBarArray.value = list
    }
    fun updateRangeBars(list: List<RangeBarDto>){
        viewModelScope.launch(Dispatchers.IO) {
            mainRepo.insertRangeBars(list)
        }
    }
    fun clearColorTable() = viewModelScope.launch {
        val noOfRows = mainRepo.deleteAllTables()
        if (noOfRows > 0) {
            //Log.d(TAG, " $noOfRows rows updates succesfully")

        } else {
            //Log.d(TAG, " Error Occurred")

        }
    }

    //Grid Data
    fun insertGridColor(gridData: GridData) = viewModelScope.launch {
        val newRowId = mainRepo.insertGridColor(gridData)

    }

    fun insertAllGridColor(gridData: List<GridData>) = viewModelScope.launch {
        val newRowId = mainRepo.insertAllGrid(gridData)
    }
    fun updateGridData(position: Int, color: String) = viewModelScope.launch {
        val noOfRows = mainRepo.updateGridColor(position, color)

    }
    fun clearGridData() = viewModelScope.launch {
        val noOfRows = mainRepo.deleteGridData()
    }

    fun insertColorHistory(colorHistory: ColorHistory){
        viewModelScope.launch(Dispatchers.IO) {
            mainRepo.insertColor(colorHistory)
        }
    }
    fun deleteOldValues(){
        viewModelScope.launch(Dispatchers.IO) {
            mainRepo.deleteOldColors()
        }
    }

    fun insertSettings(settings: Settings){
        viewModelScope.launch(Dispatchers.IO) {
            mainRepo.insert(settings)
        }
    }

    fun updateColorStyle(colorStyle: Int){
        viewModelScope.launch(Dispatchers.IO) {
            mainRepo.updateColorStyle(colorStyle)
        }
    }
}

class MainVMFactory(
    private val mainRepo: MainRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                mainRepo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}