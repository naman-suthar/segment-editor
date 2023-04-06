package app.ijp.segmentation_editor.segment_preview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.ijp.segmentation_editor.ColorStyleOption
import app.ijp.segmentation_editor.R
import app.ijp.segmentation_editor.databinding.FragmentBarLivePreviewBinding
import app.ijp.segmentation_editor.extras.model.RangeBarArray


const val BAR_VIEW_HORIZONTAL = 0
const val BAR_VIEW_VERTICAL = 1
const val BAR_VIEW_GRADIENT = 2

/**
 * It is Preview fragment for the All Segments
 * It will decide which preview to show based on colorStyle
 * It requires two providers from parent class to preview the segments
 * 1. ArrayList of rangebars : we get this from setArrayListProvider()
 * 2. ColorStyle type: we get this from setColorStyle()
 * */
class SegmentLivePreviewParentFragment : Fragment() {
    private var binding: FragmentBarLivePreviewBinding? = null
    private var arrayList = mutableListOf<RangeBarArray>()
    private var getColorStyleProvider: (() -> Int?)? = null
    private var getArrayRangeProvider: (() -> MutableList<RangeBarArray>)? = null

    /**
     * This function is to get the colorStyle from parent*/
    fun setColorStyle(colorStyleProvider: (() -> Int?)?) {
        getColorStyleProvider = colorStyleProvider
    }

    /**
     * This function is to get ArrayList from parent*/
    fun setArrayListProvider(arrayListProvider: (() -> MutableList<RangeBarArray>)?) {
        getArrayRangeProvider = arrayListProvider
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBarLivePreviewBinding.inflate(inflater, container, false)
        drawPreview()

        return binding?.root
    }

    /**
     * This function will take the array list from provider and the colorstyle and will show the Preview accordingly*/
    private fun drawPreview() {
        getArrayRangeProvider?.let {
            it().let { list ->
                arrayList = list
            }
        }
        getColorStyleProvider?.let {
            it()?.let { colorStyle ->
                when (colorStyle) {
                    ColorStyleOption.Segment.colorStyle -> {
                        val frag = SegmentsBarPreviewFragment()
                        frag.updateList(arrayList)
                        loadBarViewFragment(frag)
                    }
                    ColorStyleOption.MergedSegment.colorStyle -> {
                        val frag = MergedSegmentsPreviewFragment()
                        frag.updateList(arrayList)
                        loadBarViewFragment(frag)
                    }
                    ColorStyleOption.GradientSegment.colorStyle -> {
                        val frag = MergedGradientSegmentsPreview()
                        frag.updateGradientViewFromProvidedList(arrayList)
                        loadBarViewFragment(frag)
                    }
                }
            }
        }

    }

    private fun loadBarViewFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.preview_parent_container, fragment)
        transaction.commit()

    }

    /**
     * To update the Array List and preview it to layout
     * */
    fun updateView() {
        drawPreview()
    }

}

