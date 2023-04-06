package app.ijp.segmentation_editor.segment_editor

interface CustomComponentsCallback {
    fun onGridColorChange( color : IntArray)
    fun deleteGridColor(cList : ArrayList<String>)
}