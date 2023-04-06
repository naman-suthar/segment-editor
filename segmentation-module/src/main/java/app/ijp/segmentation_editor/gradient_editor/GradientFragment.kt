package app.ijp.segmentation_editor.gradient_editor

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.Toast
import androidx.core.content.ContextCompat
import app.ijp.colorpickerdialog.ColorDialog
import app.ijp.colorpickerdialog.OnColorChangedListener
import app.ijp.segmentation_editor.R
import app.ijp.segmentation_editor.databinding.FragmentGradientBinding
import app.ijp.segmentation_editor.extras.model.GridData
import app.ijp.segmentation_editor.segment_editor.CustomComponentsCallback
import app.ijp.segmentation_editor.extras.multi_color_dialog.MultiColorDialog



var defaultColor = 0

class GradientFragment : Fragment(), CustomComponentsCallback {
    private var binding: FragmentGradientBinding? = null
    private var gridColorList = ArrayList<String>()
    private var onGridColorChange: ((IntArray) -> Unit)? =null
    private var onColorChange: ((ArrayList<String>,Int) -> Unit)? = null
    private var onDeleteGridColor:((ArrayList<String>) -> Unit)? = null
    private var getColorHistory: (()->List<Int>?)? = null
    private var getGridData: (()->List<GridData>?)? = null

    /**
     * It gives us colorHistory for Single color dialog */
    fun setColorHistoryProvider(colorHistoryProvider: (() -> List<Int>?)?){
        getColorHistory = colorHistoryProvider
    }

    /**
     * It gives us the GridData from the Parent*/
    fun setGridData(gridDataProvider: (()->List<GridData>?)?){
        getGridData = gridDataProvider
    }
    fun updateGridData( ){
        getGridData?.let {
            it()?.let { lst->
                gridColorList.clear()
                val temp = ArrayList<GridData>()
                for (i in lst) {
                    temp.add(i)
                }
                val temp2 = temp.sortedWith(compareBy { it.seqNumber })
                for (i in temp2) {
                    gridColorList.add("#" + Integer.toHexString(i.gridColor).substring(2))
                }
                val gAdapter = context?.let { it1 ->
                    GridAdapter(
                        it1,
                        gridColorList,
                        this

                    )
                }
                (binding?.gridGradient as? GridView)?.adapter = gAdapter
            }
        }

    }
    fun setOnGridColorChangeFromMultiColorDialog(colorChange: ((IntArray)->Unit)?){
        onGridColorChange = colorChange
    }
    fun setOnColorChangeFromSingleColorDialog(colorChange: ((ArrayList<String>, Int)-> Unit)?){
        onColorChange = colorChange
        this.view?.invalidate()
    }
    fun setOnGridColorDeleted(colorDelete: ((ArrayList<String>)-> Unit)?){
        onDeleteGridColor = colorDelete
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGradientBinding.inflate(inflater,container,false)
        //        Add New color button in gradient sections
        binding?.addNewColor?.setOnClickListener {
            context?.let { it1 -> defaultColor = ContextCompat.getColor(it1,
                R.color.backgroundColor
            ) }
            val colorDialog =
                ColorDialog.newInstance(defaultColor,getColorHistory, object :
                    OnColorChangedListener {
                    override fun colorChanged(color: Int) {
                        Log.v("Color changed", "$color")
                        if (color == 0){
                            Toast.makeText(
                                context,
                                "Please Enter Valid HexCode",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else
                        {
                            val clr = "#" + Integer.toHexString(color).substring(2)
                            gridColorList.add(clr)
                            onColorChange?.let { it1 -> it1(gridColorList,color) }

                        }


                    }
                })
            colorDialog.show(requireActivity().supportFragmentManager, this.getString(R.string.single_tag))
        }
        //Feeling lucky button
        binding?.feelingLuckyLay?.setOnClickListener {
            getGridData?.let {
                it()?.let {lst->
                    gridColorList.clear()
                    val temp = ArrayList<GridData>()
                    for (i in lst) {
                        temp.add(i)
                    }
                    val temp2 = temp.sortedWith(compareBy { it.seqNumber })
                    for (i in temp2) {
                        gridColorList.add("#" + Integer.toHexString(i.gridColor).substring(2))
                    }
                }
            }

          MultiColorDialog.newInstance(
              if (gridColorList.size < 10) gridColorList.size else 10,
              this
          )
                .show(requireActivity().supportFragmentManager, this.getString(R.string.multi_tag))
        }
        (binding?.gridGradient as? GridView)?.setOnItemClickListener { adapterView, view, position, id ->


            val colorDialog =
                ColorDialog.newInstance(
                    Color.parseColor(gridColorList[position]),getColorHistory,
                    object : OnColorChangedListener {
                        override fun colorChanged(color: Int) {
                            val clr = "#" + Integer.toHexString(color).substring(2)
                            gridColorList[position] = clr
                            onColorChange?.let { it(gridColorList,color) }

                        }
                    })
            colorDialog.show(requireActivity().supportFragmentManager, this.getString(R.string.single_tag))


        }

        return binding?.root
    }

    override fun onGridColorChange(color: IntArray) {
        /**MultiColor Dialog*/
        onGridColorChange?.let{
            it(color)
        }
    }

    override fun deleteGridColor(cList: ArrayList<String>) {
        onDeleteGridColor?.let { it(cList) }
    }

}