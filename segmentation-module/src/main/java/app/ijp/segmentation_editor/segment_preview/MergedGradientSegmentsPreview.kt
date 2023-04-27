package app.ijp.segmentation_editor.segment_preview

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import app.ijp.segmentation_editor.databinding.FragmentGradientBarPreviewBinding
import app.ijp.segmentation_editor.extras.model.RangeBarArray

/**
 * Merged Gradient Preview Fragment
 * It holds Merged Gradient Bar View(Canvas) in it*/
class MergedGradientSegmentsPreview : Fragment() {
    private var binding: FragmentGradientBarPreviewBinding? = null
    private var vertex = 10f
    private var arrayList: MutableList<RangeBarArray> = mutableListOf()
    private var colorsArray = intArrayOf(Color.GRAY, Color.YELLOW, Color.BLACK)
    private var floatColorsStartPositionArray = floatArrayOf(0f,0.25f,0.67f)

    /**
     * This will insert this new list in preview*/
    fun updateGradientViewFromProvidedList(list: MutableList<RangeBarArray>) {
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
            floatColorsStartPositionArray = postionArr.toFloatArray()

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
        /**Providing Default vertex and Gradient COlors onCreate*/
        binding?.previewGradient?.updateVertex(vertex)
        /**
         * size of both colorsArray and floatColorsStartPositionArray Array should be same
         * because all colors must have one start point*/
        binding?.previewGradient?.updateColors(colorsArray,floatColorsStartPositionArray)
        return binding?.root
    }


}