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
import app.ijp.segmentation_editor.bar_preview.BarLivePreviewFragment
import app.ijp.segmentation_editor.gradient_option.GradientFragment
import app.ijp.segmentation_editor.bar_preview.GradientPreview
import app.ijp.segmentation_editor.databinding.FragmentColorStyleBinding
import app.ijp.segmentation_editor.model.GridData
import app.ijp.segmentation_editor.model.RangeBarArray
import app.ijp.segmentation_editor.segment_option.SegmentRangeBarsFragment


class ColorStyleFragment : Fragment() {
    private var binding: FragmentColorStyleBinding? = null

    private val options = listOf<String>(
        ColorStyleOption.Segment.colorStyleName,
        ColorStyleOption.MergedSegment.colorStyleName,
        ColorStyleOption.GradientSegment.colorStyleName,
        ColorStyleOption.Gradient.colorStyleName
    )

    private var mySegmentFragment: SegmentRangeBarsFragment? = null
    private var myPreviewFragment: BarLivePreviewFragment? = null

    private var myGradientFragment: GradientFragment? = null
    private var myGradientPreview: GradientPreview? = null

    /**
     * For ParentHost Fragment
     *
     */
    private var getOnColorStyleChange: ((Int) -> Unit)? = null
    private var getColorHistory: (() -> List<Int>?)? = null
    private var getColorStyle: (() -> Int?)? = null

    /**
     * For Segment Fragment
     * */
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


    fun setDataForSegmentsPreview(tempArrayProvider: () -> MutableList<RangeBarArray>) {
        getTempArrayProvider = tempArrayProvider
    }

    fun setDataForGradientPreviewAndEditor(gridDataProvider: () -> List<GridData>?) {
        getGradientColors = gridDataProvider
    }

    fun getAutoMultiColorGradientColors(onGridChange: ((IntArray) -> Unit)) {
        getAutoMultiColorGradientColors = onGridChange
    }

    fun setSingleGradientColorChanged(onGridColorChange: ((ArrayList<String>, Int) -> Unit)) {
        getChangedGradientColorAtPos = onGridColorChange
    }

    fun setGradientColorChangedOnDeletion(onGridColorDeleted: (ArrayList<String>) -> Unit) {
        getNewGradientColorsOnDeletion = onGridColorDeleted
    }

    fun setOnSegmentValueChangeListener(onValueChange: (MutableList<RangeBarArray>, Int?) -> Unit) {
        getOnSegmentValueChange = onValueChange
    }

    fun setOnSliderChange(onSliderChange: (MutableList<RangeBarArray>) -> Unit) {
        getOnSliderChange = onSliderChange
    }

    fun setOnColorStyleChange(onColorStyleChange: (Int) -> Unit) {
        getOnColorStyleChange = onColorStyleChange
    }

    fun setColorHistory(colorHistoryProvider: (() -> List<Int>?)) {
        getColorHistory = colorHistoryProvider
    }

    fun setColorStyle(colorStyleProvider: (() -> Int?)) {
        getColorStyle = colorStyleProvider
    }

    fun setSegmentsData(arrayListProvider: () -> MutableList<RangeBarArray>) {
        getSegmentsData = arrayListProvider
    }

    fun updateGradientFragment() {
        if (myGradientFragment?.isAdded == true) {
            myGradientFragment?.updateGridData()
        }

    }

    fun updateGradientPreview() {
        if (myGradientPreview?.isAdded == true) {
            myGradientPreview?.updateBar()
        }
    }

    fun updateSegmentBars() {

        if (mySegmentFragment?.isAdded == true) {
            mySegmentFragment?.updateNewList()
        }
    }

    fun updateSegmentPreview() {
        if (myPreviewFragment?.isAdded == true) {
            myPreviewFragment?.updateView()
        }
    }

    fun setTextToDropDown(position: Int) {
        binding?.atvColorStyle?.setText(options[position])
    }

    fun loadBarViewFragment() {
        getColorStyle?.let {
            it()?.let { colorStyle ->
                when (colorStyle) {
                    ColorStyleOption.Gradient.colorStyle -> {
                        if (myGradientPreview?.isAdded == false) {
                            myGradientPreview?.let { gr ->
                                loadBarFragment(gr)
                                gr.updateBar()
                            }

                        } else myGradientPreview?.updateBar()
                    }
                    else -> {
                        if (myPreviewFragment?.isAdded == false) {
                            myPreviewFragment?.let { it1 ->
                                loadBarFragment(it1)
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

    fun loadColorStyleInputFragment() {
        Log.d("Initilization","I am In Loading Segments")
        getColorStyle?.let {
            it()?.let { colorStyle ->
                when (colorStyle) {
                    ColorStyleOption.Gradient.colorStyle -> {
                        if (myGradientFragment?.isAdded == false) {
                            myGradientFragment?.let { gr ->
                                loadColorStyleInputFragment(gr)
                                gr.updateGridData()
                            }

                        } else {
                            myGradientFragment?.updateGridData()
                        }
                    }
                    else -> {
                        if (mySegmentFragment?.isAdded == false) {
                            mySegmentFragment?.let { sf ->
                                loadColorStyleInputFragment(sf)
                                sf.updateNewList()
                            }
                        } else {
                            mySegmentFragment?.updateNewList()
                        }
                    }
                }

            }
        }
    }

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

    fun attachColorStyleFragment(){
        if (mySegmentFragment == null){
            mySegmentFragment = SegmentRangeBarsFragment()
            mySegmentFragment?.setProviders()
        }
        if (myGradientFragment == null){
            myGradientFragment = GradientFragment()
            myGradientFragment?.setProviders()
        }
        loadColorStyleInputFragment()
    }

    fun attachBarPreview(){
        if (myGradientPreview == null){
            myGradientPreview = GradientPreview()
            myGradientPreview?.setProviders()
        }
        if (myPreviewFragment == null){
            myPreviewFragment = BarLivePreviewFragment()
            myPreviewFragment?.setProviders()
        }
        loadBarViewFragment()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentColorStyleBinding.inflate(inflater, container, false)
        Log.d("Initilization","I am Initialized")
        /**For Segments -> SegmentFragment*/
        mySegmentFragment = SegmentRangeBarsFragment()
        mySegmentFragment?.setProviders()

        /**For Gradient -> Gradient Fragment*/
        myGradientFragment = GradientFragment()
        myGradientFragment?.setProviders()

        /**For Live Bar Preview -> Segments, Merged Segments, Gradient Segments*/
        myPreviewFragment = BarLivePreviewFragment()
        myPreviewFragment?.setProviders()

        /** For Gradient Live Preview*/
        myGradientPreview = GradientPreview()
        myGradientPreview?.setProviders()


        (binding?.atvColorStyle as? AutoCompleteTextView)?.let {
            it.setText(options[0])
            it.setAdapter(object :
                ArrayAdapter<Any?>(requireContext(), android.R.layout.simple_list_item_1, options) {
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
                    it(position)
                }
            }
        }
        loadBarViewFragment()
        loadColorStyleInputFragment()
        return binding?.root
    }

    private fun loadColorStyleInputFragment(fragment: Fragment) {

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.color_fragment_container, fragment)
        transaction.commitNow()
    }


    private fun loadBarFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.preview_fragment_container, fragment)
        transaction.commitNow()
    }

    private fun detachFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.remove(fragment)
        transaction.commitNow()
    }

    private fun SegmentRangeBarsFragment.setProviders() {
        this.setArrayList(getSegmentsData)
        this.updateNewList()
        this.setColorHistoryProvider(getColorHistory)
        this.setOnValueChange(getOnSegmentValueChange)
        this.setOnSliderChange(getOnSliderChange)
    }

    private fun GradientFragment.setProviders() {
        this.setColorHistoryProvider(getColorHistory)
        this.setGridData(getGradientColors)
        this.setOnGridColorChange(getAutoMultiColorGradientColors)
        this.setOnColorChange(getChangedGradientColorAtPos)
        this.setOnGridColorDeleted(getNewGradientColorsOnDeletion)
    }

    private fun BarLivePreviewFragment.setProviders() {
        this.setColorStyle(getColorStyle)
        this.setArrayListProvider(getTempArrayProvider)
    }

    private fun GradientPreview.setProviders(){
        this.setGridData(getGradientColors)
    }
}

sealed class ColorStyleOption(val colorStyle: Int, val colorStyleName: String) {
    object Segment : ColorStyleOption(0, "Segments")
    object MergedSegment : ColorStyleOption(1, "Merged Segments")
    object GradientSegment : ColorStyleOption(2, "Gradient Segment")
    object Gradient : ColorStyleOption(3, "Gradient")
}