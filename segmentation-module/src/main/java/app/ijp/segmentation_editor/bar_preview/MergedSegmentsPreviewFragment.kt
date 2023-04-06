package app.ijp.segmentation_editor.bar_preview

import android.app.ActionBar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import app.ijp.segmentation_editor.databinding.FragmentHorizontalBarPreviewBinding
import app.ijp.segmentation_editor.model.RangeBarArray

/**
 * This is the MergedSegment Preview*/
class MergedSegmentsPreviewFragment : Fragment() {
    private var binding: FragmentHorizontalBarPreviewBinding? = null
    private var arrayList: MutableList<RangeBarArray> = mutableListOf()

    /**
     * Update the list and redraw the layout*/
    fun updateList(list: MutableList<RangeBarArray>) {
        arrayList = list
        this.view?.invalidate()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHorizontalBarPreviewBinding.inflate(inflater, container, false)

        drawMergedSegmentPreview()
        return binding?.root
    }

    /**
     * This function will create the merged bar preview from the array list
     * It will add all the segment bars as per their weight
     * MergedSegmentPreview is a LinearLayout with horizontal orientation and weightSum of 100
     * and we are adding more linearLayout with weight = (end-start)
     * and backgroundColor of segment.color
     * */
    private fun drawMergedSegmentPreview(){

        binding?.barViewLayout?.removeAllViews()
        //removes all views

        for (i in 0 until arrayList.size) {     //creates linearlayout of weights of RangeSlider
            if (isAdded && context !=null){
                val barView = LinearLayout(requireContext())
                val diff = (arrayList[i].end - arrayList[i].start).toFloat()
                val weightOfThis: Float = if (diff == 0f) {
                    diff
                } else {
                    diff + 1
                }
                val barViewLp = LinearLayout.LayoutParams(
                    0,
                    ActionBar.LayoutParams.MATCH_PARENT,
                    weightOfThis
                )
                barView.orientation = LinearLayout.HORIZONTAL
                barView.setBackgroundColor(arrayList[i].color)
                barView.layoutParams = barViewLp
                binding?.barViewLayout?.addView(barView)
            }

        }
    }


}
