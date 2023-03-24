package com.naman.segmentation_module.bar_preview

import android.app.ActionBar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.naman.segmentation_module.databinding.FragmentHorizontalBarPreviewBinding
import com.naman.segmentation_module.model.RangeBarArray


class HorizontalBarPreviewFragment : Fragment() {
    private var binding: FragmentHorizontalBarPreviewBinding? = null
    private var arrayList: MutableList<RangeBarArray> = mutableListOf()
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

        showPreview()
        return binding?.root
    }

    private fun showPreview(){

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
                /*var diff = (rangebarView.rangeBar.values[1] - rangebarView.rangeBar.values[0])
                            if (diff == 0f) {
                                diff
                            } else {
                                diff + 1
                            }*/
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
