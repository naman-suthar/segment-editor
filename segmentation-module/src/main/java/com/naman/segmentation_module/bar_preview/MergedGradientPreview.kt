package com.naman.segmentation_module.bar_preview

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.naman.segmentation_module.databinding.FragmentGradientBarPreviewBinding
import com.naman.segmentation_module.model.RangeBarArray


class MergedGradientPreview : Fragment() {
    private var binding: FragmentGradientBarPreviewBinding? = null
    private var vertex = 10f
    private var arrayList: MutableList<RangeBarArray> = mutableListOf()
    private var colorsArray = intArrayOf(Color.GRAY, Color.YELLOW, Color.BLACK)
    private var floatColorsArray = floatArrayOf(0f,0.25f,0.67f)
    fun updateList(list: MutableList<RangeBarArray>) {
        arrayList = list
        if (arrayList.size > 1){
            vertex = arrayList[0].end.toFloat()
            val myClorList = mutableListOf<Int>()
            val postionArr = mutableListOf<Float>()
            arrayList.forEach {
                myClorList.add(it.color)
                postionArr.add(it.start/100f)
            }
            colorsArray = myClorList.toIntArray()
            floatColorsArray = postionArr.toFloatArray()

            this.view?.invalidate()
        }else{
            if (isAdded){
                Toast.makeText(
                    requireContext(),
                    "Min 2 colors requirred for Gradient",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGradientBarPreviewBinding.inflate(inflater, container, false)
        binding?.previewGradient?.updateVertex(vertex)
        /**
         * size of both Array should be same*/

        binding?.previewGradient?.updateColors(colorsArray,floatColorsArray)
        return binding?.root
    }


}