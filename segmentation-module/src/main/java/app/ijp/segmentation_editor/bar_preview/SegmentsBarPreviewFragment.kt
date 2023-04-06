package app.ijp.segmentation_editor.bar_preview

import android.app.ActionBar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import app.ijp.segmentation_editor.databinding.FragmentVerticalSegmentBarPreviewBinding
import app.ijp.segmentation_editor.model.RangeBarArray

/**
 * This is Preview fragment for Segments
 * It has vertical linear layout which will draw the segments of their weight*/
class SegmentsBarPreviewFragment : Fragment() {
   private var binding: FragmentVerticalSegmentBarPreviewBinding? = null
    private var arrayList: MutableList<RangeBarArray> = mutableListOf()
    fun updateList(list: MutableList<RangeBarArray>) {
        arrayList = list

        this.view?.invalidate()
    }

    private fun showPreview() {
        binding?.barViewLayout?.removeAllViews()        //removes all views
        for (i in 0 until arrayList.size) {     //creates linearlayout of weights of RangeSlider
            val barviewParent: LinearLayout = LinearLayout(context)
            val barViewParentLp = LinearLayout.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                20
            )
            barViewParentLp.setMargins(0,6,0,6)
            barviewParent.weightSum = 100F
            barviewParent.orientation = LinearLayout.HORIZONTAL
            barviewParent.layoutParams = barViewParentLp
            val barView = LinearLayout(context)

            val barViewLp = LinearLayout.LayoutParams(
                0,
                ActionBar.LayoutParams.MATCH_PARENT,
                arrayList[i].end.toFloat()
            )
            barView.orientation = LinearLayout.HORIZONTAL
            barView.setBackgroundColor(arrayList[i].color)
            barView.layoutParams = barViewLp
            barviewParent.addView(barView)
            binding?.barViewLayout?.addView(barviewParent)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentVerticalSegmentBarPreviewBinding.inflate(inflater,container,false)
        showPreview()
        return binding?.root
    }


}