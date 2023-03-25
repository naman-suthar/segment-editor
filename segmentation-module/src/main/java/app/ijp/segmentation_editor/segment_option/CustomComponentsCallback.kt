package app.ijp.segmentation_editor.segment_option

import app.ijp.segmentation_editor.model.RangeBarArray

interface CustomComponentsCallback {
    fun onValueChanged(arrayList: MutableList<RangeBarArray>, color: Int?)
    fun onSliderChange(temp: MutableList<RangeBarArray>)
    fun onGridColorChange( color : IntArray)
    fun deleteGridColor(cList : ArrayList<String>)

    fun onSetManuallyClicked(index:Int,position: Int)
}