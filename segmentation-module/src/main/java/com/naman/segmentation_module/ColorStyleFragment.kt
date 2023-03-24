package com.naman.segmentation_module

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import com.naman.segmentation_module.bar_preview.BarLivePreviewFragment
import com.naman.segmentation_module.gradient_option.GradientFragment
import com.naman.segmentation_module.segment_option.SegmentRangeBarsFragment
import com.naman.segmentation_module.bar_preview.GradientPreview
import com.naman.segmentation_module.databinding.FragmentColorStyleBinding
import com.naman.segmentation_module.model.GridData
import com.naman.segmentation_module.model.RangeBarArray


class ColorStyleFragment : Fragment() {
    private var binding: FragmentColorStyleBinding? = null
    private var arrayRangeBar = mutableListOf<RangeBarArray>()
    private var tempArrayRangeBar = mutableListOf<RangeBarArray>()

    private var listGrid: List<GridData>? = null
    private var listColorHistory: List<Int>? = null
    private var colorStyle: Int? = null
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

    private var getOnGridChange: ((IntArray) -> Unit)? = null
    private var getOnGridColorChange: ((ArrayList<String>, Int) -> Unit)? = null
    private var getOnGridColorDeleted: ((ArrayList<String>) -> Unit)? = null
    private var getOnSegmentValueChange: ((MutableList<RangeBarArray>, Int?) -> Unit)? = null
    private var getOnSliderChange: ((MutableList<RangeBarArray>) -> Unit)? = null
    private var getOnColorStyleChange: ((Int) -> Unit)? = null
    private var getColorHistoryProvider: (() -> List<Int>?)? = null
    private var getColorStyleProvider: (() -> Int?)? = null
    private var getArrayListProvider: (() -> MutableList<RangeBarArray>)? = null
    private var getGridDataProvider: (() -> List<GridData>?)? = null
    private var getTempArrayProvider: (() -> MutableList<RangeBarArray>)? = null

    fun setTempArray(tempArrayProvider: () -> MutableList<RangeBarArray>) {
        getTempArrayProvider = tempArrayProvider
    }

    fun setGridData(gridDataProvider: () -> List<GridData>?) {
        getGridDataProvider = gridDataProvider
    }

    fun setOnGridChangeProvider(onGridChange: ((IntArray) -> Unit)) {
        getOnGridChange = onGridChange
    }

    fun setOnGridColorChange(onGridColorChange: ((ArrayList<String>, Int) -> Unit)) {
        getOnGridColorChange = onGridColorChange
    }

    fun setOnGridColorDeleted(onGridColorDeleted: (ArrayList<String>) -> Unit) {
        getOnGridColorDeleted = onGridColorDeleted
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
        getColorHistoryProvider = colorHistoryProvider
    }

    fun setColorStyle(colorStyleProvider: (() -> Int?)) {
        getColorStyleProvider = colorStyleProvider
    }

    fun setArrayListProvider(arrayListProvider: () -> MutableList<RangeBarArray>) {
        getArrayListProvider = arrayListProvider
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentColorStyleBinding.inflate(inflater, container, false)
        /**For Segments -> SegmentFragment*/
        mySegmentFragment = SegmentRangeBarsFragment()
        mySegmentFragment?.setArrayList(getArrayListProvider)
        mySegmentFragment?.updateNewList()
        mySegmentFragment?.setColorHistoryProvider(getColorHistoryProvider)
//        mySegmentFragment?.setTextValueClickListener { index, position ->
////            showAlertDialog(index,position)
//        }

        /**For Gradient -> Gradient Fragment*/
        myGradientFragment = GradientFragment()
        myGradientFragment?.setColorHistoryProvider(getColorHistoryProvider)
        myGradientFragment?.setGridData(getGridDataProvider)
        //Segments, Merged Segments, Gradient Segments, Gradient

        /**For Live Bar Preview -> Segments, Merged Segments, Gradient Segments*/
        myPreviewFragment = BarLivePreviewFragment()
//        myPreviewFragment.updateColorStyle(BAR_VIEW_VERTICAL)
        myPreviewFragment?.setColorStyle(getColorStyleProvider)
        myPreviewFragment?.setArrayListProvider(getTempArrayProvider)


        /** For Gradient Live Preview*/
        myGradientPreview = GradientPreview()

        myGradientPreview?.setGridData(getGridDataProvider)
        listGrid?.let { myGradientPreview?.updateBar() }

        myGradientFragment?.setOnGridColorChange(getOnGridChange)
        myGradientFragment?.setOnColorChange(getOnGridColorChange)
        myGradientFragment?.setOnGridColorDeleted(getOnGridColorDeleted)

        mySegmentFragment?.setOnValueChange(getOnSegmentValueChange)
        mySegmentFragment?.setOnSliderChange(getOnSliderChange)




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
//                mainActivityViewModel?.updateColorStyle(position)
            }
        }
        return binding?.root
    }

    private fun loadColorStyleInputFragment(fragment: Fragment) {

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.color_fragment_container, fragment)
        transaction.commitNow()
    }

    fun loadColorStyleInputFragment() {
        getColorStyleProvider?.let {
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

    fun loadBarViewFragment() {
        getColorStyleProvider?.let {
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

    private fun loadBarFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.preview_fragment_container, fragment)
        transaction.commitNow()
    }

    fun updateSegmentPreview() {
        if (myPreviewFragment?.isAdded == true) {
            myPreviewFragment?.updateView()
        }
    }

    fun setTextToDropDown(position: Int){
        binding?.atvColorStyle?.setText(options[position])
    }
}

sealed class ColorStyleOption(val colorStyle: Int, val colorStyleName: String) {
    object Segment : ColorStyleOption(0, "Segments")
    object MergedSegment : ColorStyleOption(1, "Merged Segments")
    object GradientSegment : ColorStyleOption(2, "Gradient Segment")
    object Gradient : ColorStyleOption(3, "Gradient")
}