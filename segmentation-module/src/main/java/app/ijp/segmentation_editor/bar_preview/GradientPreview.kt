package app.ijp.segmentation_editor.bar_preview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.ijp.segmentation_editor.databinding.FragmentGradientColorBarBinding
import app.ijp.segmentation_editor.model.GridData


class GradientPreview : Fragment() {
    private var binding: FragmentGradientColorBarBinding? = null
    private var colorsArray = intArrayOf()
    private var floatColorsArray = floatArrayOf()
    private var getGridData: (()->List<GridData>?)? = null
    fun setGridData(gridDataProvider: (()->List<GridData>?)?){
        getGridData = gridDataProvider
    }
    fun updateBar() {

        val myClorList = mutableListOf<Int>()
        val postionArr = mutableListOf<Float>()
        var initial = 0f
        getGridData?.let {
            it()?.let {lst->
                lst.forEach {
                    myClorList.add(it.gridColor)
                    postionArr.add(initial)
                    initial += (1f/lst.size)
                }
                colorsArray = myClorList.toIntArray()
                floatColorsArray = postionArr.toFloatArray()
                binding?.gradientColorBarPreview?.updateBar(colorsArray,floatColorsArray)
            }
        }




    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGradientColorBarBinding.inflate(inflater,container,false)
        updateBar()
        return binding?.root
    }


}