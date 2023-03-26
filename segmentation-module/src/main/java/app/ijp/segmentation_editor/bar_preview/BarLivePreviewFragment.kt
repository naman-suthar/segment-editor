package app.ijp.segmentation_editor.bar_preview

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.ijp.segmentation_editor.ColorStyleOption
import app.ijp.segmentation_editor.R
import app.ijp.segmentation_editor.databinding.FragmentBarLivePreviewBinding
import app.ijp.segmentation_editor.model.RangeBarArray


const val BAR_VIEW_HORIZONTAL = 0
const val BAR_VIEW_VERTICAL = 1
const val BAR_VIEW_GRADIENT = 2

class BarLivePreviewFragment : Fragment() {
    private var binding: FragmentBarLivePreviewBinding? = null
    private var barPreviewType: Int = BAR_VIEW_HORIZONTAL
    private var arrayList = mutableListOf<RangeBarArray>()
    private var getColorStyleProvider: (() -> Int?)? = null
    private var getArrayRangeProvider: (() -> MutableList<RangeBarArray>)? = null
    fun setColorStyle(colorStyleProvider: (() -> Int?)?) {
        getColorStyleProvider = colorStyleProvider
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBarLivePreviewBinding.inflate(inflater, container, false)

        showPreview()



        return binding?.root
    }

    private fun showPreview() {
        Log.d("Observer4","I am in show Preview")
        getColorStyleProvider?.let {
            it()?.let { colorStyle ->
                barPreviewType = colorStyle
            }
        }
        getArrayRangeProvider?.let {
            it()?.let { list ->
                arrayList = list
            }
        }
        when (barPreviewType) {
            ColorStyleOption.Segment.colorStyle -> {
                val frag = HorizontalBarPreviewFragment()
                frag.updateList(arrayList)
                loadBarViewFragment(frag)
            }
            ColorStyleOption.MergedSegment.colorStyle -> {
                val frag = VerticalSegmentBarPreviewFragment()
                frag.updateList(arrayList)
                loadBarViewFragment(frag)
            }
            ColorStyleOption.GradientSegment.colorStyle -> {
                val frag = MergedGradientPreview()
                frag.updateList(arrayList)
                loadBarViewFragment(frag)
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
        showPreview()
        this.view?.invalidate()
    }

    /**
     * To Update/Change the Bar view Type
     * @param barViewType Constants for Bar view( BAR_VIEW_HORIZONTAL, BAR_VIEW_VERTICAL, BAR_VIEW_GRADIENT )
     * */
    fun updateColorStyle() {
        showPreview()
        /*getColorStyleProvider?.let {
            it() ?.let {clrStyle->
                barPreviewType = clrStyle
            }
        }*/
    }

    fun setArrayListProvider(arrayListProvider: (() -> MutableList<RangeBarArray>)?) {
        getArrayRangeProvider = arrayListProvider
    }
}

