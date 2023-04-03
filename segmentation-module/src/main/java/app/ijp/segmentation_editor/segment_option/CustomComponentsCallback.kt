package app.ijp.segmentation_editor.segment_option

import app.ijp.segmentation_editor.model.RangeBarArray

interface CustomComponentsCallback {
    fun onGridColorChange( color : IntArray)
    fun deleteGridColor(cList : ArrayList<String>)
}