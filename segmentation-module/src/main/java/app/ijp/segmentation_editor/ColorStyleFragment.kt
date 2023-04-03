package app.ijp.segmentation_editor

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import app.ijp.segmentation_editor.bar_preview.SegmentLivePreviewFragment
import app.ijp.segmentation_editor.gradient_option.GradientFragment
import app.ijp.segmentation_editor.bar_preview.GradientPreview
import app.ijp.segmentation_editor.databinding.FragmentColorStyleBinding
import app.ijp.segmentation_editor.model.GridData
import app.ijp.segmentation_editor.model.RangeBarArray
import app.ijp.segmentation_editor.segment_option.SegmentRangeBarsFragment

/**
 * It is Main Fragment that holds all fragments
 * It is parent fragment for Preview Fragment and ColorStyleEditor Fragment
 *
 *                                                                         ColorStyleFragment
 *                                                                               |
 *                                                                               |
 *                      (SegmentPreviewFragment              GradientPreview)         (SegmentRangeBarFragment         GradientFragment)
 *                              |
 *                              |
 *         Segment      MergedSegment    GradientSegment
 *
 *
 * The functions and there job:
 *
 * setDataForSegmentsPreview() -> it sets the "getTempArrayProvider" which it passes to BarPreview
 * setDataForGradientPreviewAndEditor() -> it sets "getGradientColors" which it passes to GradientPreview and Gradient Editor
 *
 * getAutoMultiColorGradientColors() -> it sets "getAutoMultiColorGradientColors" which it passes to Gradient Editor for MultiColor Dialog
 * setSingleGradientColorChanged() -> it sets "getChangedGradientColorAtPos" which it passes to Gradient Editor for single Color Dialog
 * setGradientColorChangedOnDeletion() -> it sets "getNewGradientColorsOnDeletion" which it passes to Gradient editor for color delete
 *
 * setOnSegmentValueChangeListener() -> it sets "getOnSegmentValueChange" which is passed to Segment Gradient
 * setOnSliderChange() -> it sets "getOnSliderChange" which is passed to segment Gradient
 * setSegmentsData() -> it sets "getSegmentsData" which is passed to Segment Gradient
 *
 * setColorHistory() -> it sets the "getColorHistory" which is passed to both segment and gradient
 *
 * setColorStyle() -> it sets the "getColorStyle" which is not passed to any child fragment, It is used by itself to determine the child components
 * setOnColorStyleChange() -> this function is called when item is selected from dropdown
 *
 * */
class ColorStyleFragment(var showPreviewFragment: Boolean = true, var options: List<ColorStyleOption> = listOf<ColorStyleOption>(
    ColorStyleOption.Segment,
    ColorStyleOption.MergedSegment,
    ColorStyleOption.GradientSegment,
    ColorStyleOption.Gradient
))
    :Fragment(){

    private var binding: FragmentColorStyleBinding? = null
    private var mySegmentFragment: SegmentRangeBarsFragment? = null
    private var myPreviewFragment: SegmentLivePreviewFragment? = null

    private var myGradientFragment: GradientFragment? = null
    private var myGradientPreview: GradientPreview? = null

    /**
     * For ParentHost Fragment
     */
    /**
     * It is provider function we declare in activity and we call it on dropdown Item selected*/
    private var getOnColorStyleChange: ((Int) -> Unit)? = null
    /**
     * It is provider function we declare it in activity and this is passed to child fragment who has the color Dialog*/
    private var getColorHistory: (() -> List<Int>?)? = null
    /**
     * It is provider Function which is set from activity to refer the colorStyle */
    private var getColorStyle: (() -> Int?)? = null

    /**
     * For Segment Fragment
     * */
    /**
     * This function returns the Rangebar arrray which is passed to Segment Fragment*/
    private var getSegmentsData: (() -> MutableList<RangeBarArray>)? = null
    private var getOnSegmentValueChange: ((MutableList<RangeBarArray>, Int?) -> Unit)? = null
    private var getOnSliderChange: ((MutableList<RangeBarArray>) -> Unit)? = null

    /**
     * For Segments Preview
     * */
    private var getTempArrayProvider: (() -> MutableList<RangeBarArray>)? = null


    /**
     * For Gradient Preview (shared with gradient Fragment)
     * */
    private var getGradientColors: (() -> List<GridData>?)? = null

    /**
     * For Gradient Fragment
     * */
    private var getAutoMultiColorGradientColors: ((IntArray) -> Unit)? = null
    private var getChangedGradientColorAtPos: ((ArrayList<String>, Int) -> Unit)? = null
    private var getNewGradientColorsOnDeletion: ((ArrayList<String>) -> Unit)? = null


    /**
     * It is Passed to BarPreviewFragment to set preview*/
    fun setDataForSegmentsPreview(tempArrayProvider: () -> MutableList<RangeBarArray>) {
        getTempArrayProvider = tempArrayProvider
    }

    /**
     * This function return the List of Grid data from activity and which is passed to Gradient Preview and Gradient Fragment*/
    fun setDataForGradientPreviewAndEditor(gridDataProvider: () -> List<GridData>?) {
        getGradientColors = gridDataProvider
    }

    /**
     * It is passed to Gradient Fragment to handle multi Color Dialog */
    fun getAutoMultiColorGradientColors(onGridChange: ((IntArray) -> Unit)) {
        getAutoMultiColorGradientColors = onGridChange
    }

    /**
     * It is Passed to Gradient Fragment to handle when single color changed or newColorAdded to the GridList */
    fun setSingleGradientColorChanged(onGridColorChange: ((ArrayList<String>, Int) -> Unit)) {
        getChangedGradientColorAtPos = onGridColorChange
    }

    /**
     * It is Passed to Gradient Fragment to handle Color Delete ('X' pressed)*/
    fun setGradientColorChangedOnDeletion(onGridColorDeleted: (ArrayList<String>) -> Unit) {
        getNewGradientColorsOnDeletion = onGridColorDeleted
    }

    /**
     * It is Passed to Segment Fragment to handle SliderTouch Complete(i.e. It gives updated RangeBar ArrayList)*/
    fun setOnSegmentValueChangeListener(onValueChange: (MutableList<RangeBarArray>, Int?) -> Unit) {
        getOnSegmentValueChange = onValueChange
    }

    /**
     * It is passed to Segment Fragment to handle the slider change (i.e. It gives updated TempArray)*/
    fun setOnSliderChange(onSliderChange: (MutableList<RangeBarArray>) -> Unit) {
        getOnSliderChange = onSliderChange
    }

    /**
     * Set function on Item Selected from Dropdown*/
    fun setOnColorStyleChange(onColorStyleChange: (Int) -> Unit) {
        getOnColorStyleChange = onColorStyleChange
    }

    /**
     * To set COlor history provider*/
    fun setColorHistory(colorHistoryProvider: (() -> List<Int>?)) {
        getColorHistory = colorHistoryProvider
    }

    /**
     * To set Color Style Provider*/
    fun setColorStyle(colorStyleProvider: (() -> Int?)) {
        getColorStyle = colorStyleProvider
    }

    /**
     * This is passed to Segment Fragment
     * */
    fun setSegmentsData(arrayListProvider: () -> MutableList<RangeBarArray>) {
        getSegmentsData = arrayListProvider
    }

    /**
     * The function is to update Grid fragment if only it is attached/live
     * This is useful when observing grid color array as it will update grid only if they are on screen
     * otherwise if Segment view is live. it will not change anything from collected value*/
    fun updateGradientFragment() {
        if (myGradientFragment?.isAdded == true) {
            myGradientFragment?.updateGridData()
        }

    }

    /**
     * The function is to update Gradient Preview if only it is attached/live
     * This is useful when observing grid color array as it will update gridPreview only if they are on screen
     * otherwise if BarPreview is live. it will not change anything from collected value*/
    fun updateGradientPreview() {
        if (myGradientPreview?.isAdded == true) {
            myGradientPreview?.updateBar()
        }
    }

    /**
     * The function is to update segments if only it is attached/live
     * This is useful when observing range bars array as it will update segments only if they are on screen
     * otherwise if Grid view is live. it will not change anything from collected value*/
    fun updateSegmentBars() {

        if (mySegmentFragment?.isAdded == true) {
            mySegmentFragment?.updateSliders()
        }
    }


    /**
     * The function is to update Bar Preview if only it is attached/live
     * This is useful when observing range bar array as it will update Bar Preview only if it is attached to screen
     * otherwise if Grid Preview is live. it will not change anything from collected value*/
    fun updateSegmentPreview() {
        if (myPreviewFragment?.isAdded == true) {
            myPreviewFragment?.updateView()
        }
    }

    /**
     * This function is to set text on dropdown
     * */
    fun setTextToDropDown(colorStyle: Int) {
        options.find { it.colorStyle == colorStyle }?.colorStyleName?.let {
            binding?.atvColorStyle?.setText(
                it.toString()
            )
        }
    }

    /**
     * It is called to Update Preview*/
    fun notifyBarPreview() {
        getColorStyle?.let {
            it()?.let { colorStyle ->
                when (colorStyle) {
                    ColorStyleOption.Gradient.colorStyle -> {
                        if (myGradientPreview?.isAdded == false) {
                            myGradientPreview?.let { gr ->
                                loadBarFragmentInContainer(gr)
                                gr.updateBar()
                            }

                        } else myGradientPreview?.updateBar()
                    }
                    else -> {
                        if (myPreviewFragment?.isAdded == false) {
                            myPreviewFragment?.let { it1 ->
                                loadBarFragmentInContainer(it1)
                                it1.updateView()
                            }

                        } else {
                            myPreviewFragment?.updateView()
                        }
                    }
                }
            }
        }

    }

    /**
     * It is called to update the Editor Fragment (Segments/Gradient)*/
    fun notifyColorStyleInputFragment() {
        Log.d("Initilization", "I am In Loading Segments")
        getColorStyle?.let {
            it()?.let { colorStyle ->
                when (colorStyle) {
                    ColorStyleOption.Gradient.colorStyle -> {
                        if (myGradientFragment?.isAdded == false) {
                            myGradientFragment?.let { gr ->
                                loadColorStyleInputInContainer(gr)
                                gr.updateGridData()
                            }

                        } else {
                            myGradientFragment?.updateGridData()
                        }
                    }
                    else -> {
                        if (mySegmentFragment?.isAdded == false) {
                            mySegmentFragment?.let { sf ->
                                loadColorStyleInputInContainer(sf)
                                sf.updateSliders()
                            }
                        } else {
                            mySegmentFragment?.updateSliders()
                        }
                    }
                }

            }
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentColorStyleBinding.inflate(inflater, container, false)
        Log.d("Initilization", "I am Initialized")
        /**For Segments -> SegmentFragment*/
        mySegmentFragment = SegmentRangeBarsFragment()
        mySegmentFragment?.setProviders()

        /**For Gradient -> Gradient Fragment*/
        myGradientFragment = GradientFragment()
        myGradientFragment?.setProviders()

        /**For Live Bar Preview -> Segments, Merged Segments, Gradient Segments*/
        myPreviewFragment = SegmentLivePreviewFragment()
        myPreviewFragment?.setProviders()

        /** For Gradient Live Preview*/
        myGradientPreview = GradientPreview()
        myGradientPreview?.setProviders()

        /**
         * DropDown
         * */
        (binding?.atvColorStyle as? AutoCompleteTextView)?.let {
            it.setText(options[0].colorStyleName)
            it.setAdapter(object :
                ArrayAdapter<Any?>(requireContext(), android.R.layout.simple_list_item_1, options.map {option->
                    option.colorStyleName }) {
                override fun getFilter(): Filter {
                    return object : Filter() {
                        override fun performFiltering(constraint: CharSequence): FilterResults? {
                            return null
                        }

                        override fun publishResults(
                            constraint: CharSequence,
                            results: FilterResults?
                        ) {
                        }
                    }
                }
            })
            it.setOnItemClickListener { adapterView, view, position, l ->

                getOnColorStyleChange?.let {

                    Log.d("ColorStyleIsClicked","$position ${options[position].colorStyle}")
                    it(options[position].colorStyle)
                }
            }
        }
        notifyBarPreview()
        notifyColorStyleInputFragment()
        return binding?.root
    }

    /**
     * This function is just to execute Transaction to replace fragment in Editor (Segment/Gradient)*/
    private fun loadColorStyleInputInContainer(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.color_fragment_container, fragment)
        transaction.commitNow()
    }


    /**
     * This function is just to execute Transaction to replace fragment in Preview (BarPreview/GradientPreview)*/
    private fun loadBarFragmentInContainer(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.preview_fragment_container, fragment)
        transaction.commitNow()
    }

    /**
     * This setProvider are passing the provider functions to the Child fragments*/
    private fun SegmentRangeBarsFragment.setProviders() {
        this.setArrayList(getSegmentsData)
        this.setColorHistoryProvider(getColorHistory)
        this.setOnValueChange(getOnSegmentValueChange)
        this.setOnSliderChange(getOnSliderChange)
    }

    private fun GradientFragment.setProviders() {
        this.setColorHistoryProvider(getColorHistory)
        this.setGridData(getGradientColors)
        this.setOnGridColorChangeFromMultiColorDialog(getAutoMultiColorGradientColors)
        this.setOnColorChangeFromSingleColorDialog(getChangedGradientColorAtPos)
        this.setOnGridColorDeleted(getNewGradientColorsOnDeletion)
    }

    private fun SegmentLivePreviewFragment.setProviders() {
        this.setColorStyle(getColorStyle)
        this.setArrayListProvider(getTempArrayProvider)
    }

    private fun GradientPreview.setProviders(){
        this.setGridData(getGradientColors)
    }


    //Useless for Now
    fun isSegmentPreviewAttached(): Boolean? = myPreviewFragment?.isAdded

    fun isGradientPreviewAttached(): Boolean? = myGradientPreview?.isAdded

    fun isSegmentFragmentAttached(): Boolean? = mySegmentFragment?.isAdded

    fun isGradientFragmentAttached(): Boolean? = myGradientFragment?.isAdded

    fun dettachPreview() {

        myPreviewFragment = if (myPreviewFragment?.isAdded == true) {
            myPreviewFragment?.let {
                detachFragment(it)
            }
            null
        } else {
            null
        }
        myGradientPreview = if (myGradientPreview?.isAdded == true) {
            myGradientPreview?.let {
                detachFragment(it)
            }
            null
        } else {
            null
        }

    }

    fun dettachColorStyleInput() {
        myGradientFragment = if (myGradientFragment?.isAdded == true) {
            myGradientFragment?.let {
                detachFragment(it)
            }
            null
        } else {
            null
        }
        mySegmentFragment = if (mySegmentFragment?.isAdded == true) {
            mySegmentFragment?.let {
                detachFragment(it)
            }
            null
        } else {
            null
        }
    }

    fun attachColorStyleFragment() {
        if (mySegmentFragment == null) {
            mySegmentFragment = SegmentRangeBarsFragment()
            mySegmentFragment?.setProviders()
        }
        if (myGradientFragment == null) {
            myGradientFragment = GradientFragment()
            myGradientFragment?.setProviders()
        }
        notifyColorStyleInputFragment()
    }

    fun attachBarPreview() {
        if (myGradientPreview == null) {
            myGradientPreview = GradientPreview()
            myGradientPreview?.setProviders()
        }
        if (myPreviewFragment == null) {
            myPreviewFragment = SegmentLivePreviewFragment()
            myPreviewFragment?.setProviders()
        }
        notifyBarPreview()
    }
    private fun detachFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.remove(fragment)
        transaction.commitNow()
    }
}

sealed class ColorStyleOption(val colorStyle: Int, val colorStyleName: String) {
    object Segment : ColorStyleOption(0, "Segments")
    object MergedSegment : ColorStyleOption(1, "Merged Segments")
    object GradientSegment : ColorStyleOption(2, "Gradient Segment")
    object Gradient : ColorStyleOption(3, "Gradient")
}