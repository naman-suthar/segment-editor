package com.naman.segmentation_module.segment_option

import com.naman.segmentation_module.model.RangeBarArray

interface CustomComponentsCallback {
    fun onValueChanged(arrayList: MutableList<RangeBarArray>, color: Int?)
    fun onSliderChange(temp: MutableList<RangeBarArray>)
    fun onGridColorChange( color : IntArray)
    fun deleteGridColor(cList : ArrayList<String>)

    fun onSetManuallyClicked(index:Int,position: Int)
}