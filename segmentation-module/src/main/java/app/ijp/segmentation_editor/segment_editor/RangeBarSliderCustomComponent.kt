package app.ijp.segmentation_editor.segment_editor

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.FragmentManager
import app.ijp.colorpickerdialog.ColorDialog
import app.ijp.colorpickerdialog.OnColorChangedListener
import app.ijp.segmentation_editor.databinding.EachRangebarBinding
import com.google.android.material.slider.RangeSlider
import app.ijp.segmentation_editor.extras.model.RangeBarArray
import kotlin.math.abs


var defaultColor: Int = 0
/**
 * This is Custom Component for RangeBarSlider
 * @param rangeBarItem It is the RangeBarItem for whom this slider is created
 * @param colorHistory It is Passed to Color Dialog to show ColorHistory
 * */

/**
 * It performs the below tasks
 * 1. give callback/Provider when slider movement is completed (On Slider Stops)
 * 2. give a callback/Provider when slider is changing
 * 3. give a callback when color is changed
 * 4. give a callback when Texts are clicked
 * 5. update the copy of itself from temporary array we call temporary array from provider
 * */
class RangeBarSliderCustomComponent @JvmOverloads constructor(
    context: Context,
    supportFragmentManager: FragmentManager,
    rangeBarItem: RangeBarArray,
    colorHistory: (() -> List<Int>?)?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) :
    ConstraintLayout(context, attrs, defStyle, defStyleRes) {
    val layoutInflator = LayoutInflater.from(context)
    private val eachRangebarBinding = EachRangebarBinding.inflate(layoutInflator, this, true)
    private var getTempArrayList: (() -> MutableList<RangeBarArray>)? =
        null       //To get their own copy element for temporary changes
    private var onSliderStopsAndUpdateValue: ((Int, Int, Float) -> Unit)? = null
    private var onSliderChange: ((Int, Int, Float) -> Unit)? = null
    private var onColorChangedFromDialogBox: ((Int, Int) -> Unit)? = null
    private var onTextItemClicked: ((Int, Int) -> Unit)? = null
    private var index: Int? = null

    /**
     * This function is set from parent to perform task on Array value changed (Slider Touch Stopped)
     * */
    fun setOnSliderMovementCompletion(valueChangeProvider: (Int, Int, Float) -> Unit) {
        onSliderStopsAndUpdateValue = valueChangeProvider
    }

    /**
     * This function is set from parent to perform on changing slider (Live)*/
    fun setOnSliderChangingLive(sliderChangeProvider: ((Int, Int, Float) -> Unit)?) {
        onSliderChange = sliderChangeProvider
    }

    /**
     * Get the temporary copy of Array (UI State)*/
    fun getTempArray(tempArrayProvider: () -> MutableList<RangeBarArray>) {
        getTempArrayList = tempArrayProvider
    }

    /**
     * This function is set from parent to perform task on Color selected from Dialog
     * */
    fun setOnColorChangedFromRangeBar(onColorChangedProvider: (Int, Int) -> Unit) {
        onColorChangedFromDialogBox = onColorChangedProvider
    }

    /**This is set by parent To perform task when texts are clicked (Display Dialog)*/
    fun setOnValueTextItemClicked(textItemClicked: ((Int, Int) -> Unit)) {
        onTextItemClicked = textItemClicked
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        index = (this@RangeBarSliderCustomComponent.parent as LinearLayout).indexOfChild(this@RangeBarSliderCustomComponent)
    }

    init {
        /**
         * set values to rangebar */
        eachRangebarBinding.rangeBar.values =
            mutableListOf(rangeBarItem.start.toFloat(), rangeBarItem.end.toFloat())
        /**
         * set text to rangebar*/
        eachRangebarBinding.startTxt.text = rangeBarItem.start.toString()
        eachRangebarBinding.endTxt.text = rangeBarItem.end.toString()
        /**
         * set color to rangebar*/
        setColor(rangeBarItem.color)

        /**
         * Calling provider/callback function on text Items clicked*/
        eachRangebarBinding.startTxt.setOnClickListener {
            if (index != 0) {
                onTextItemClicked?.let {
                    index?.let { it1 -> it(it1, LEFT_POSITION) }
                }
            }
        }
        eachRangebarBinding.endTxt.setOnClickListener {
            onTextItemClicked?.let {
                index?.let { it1 -> it(it1, RIGHT_POSITION) }
            }
        }


        eachRangebarBinding.colorPickerBtn.setOnClickListener {

            val colorDialog =
                ColorDialog.newInstance(
                    currentColor,
                    colorHistory = colorHistory,
                    object : OnColorChangedListener {
                        override fun colorChanged(color: Int) {

                            onColorChangedFromDialogBox?.let {
                                index?.let { it1 -> it(it1, color) }
                            }
                        }
                    })
            colorDialog.show(supportFragmentManager, "Single")

        }
        eachRangebarBinding.rangeBar.addOnSliderTouchListener(object :
            RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {


                if (index == 0 && slider.values[1].toInt() == rangeBarItem.end) {
                    /**
                     * 0 value is being moved and we will return nothing */
                    Log.d("ValuesToIndex","$index ${slider.values[1]} ${rangeBarItem.end}")

                }
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {

                if (rangeBarItem.start == slider.values[0].toInt()) {
                    /**
                     * This means the start value is not changed so It is the End value or Right thumb is moved*/
                    onSliderStopsAndUpdateValue?.let {
                        it(index!!, RIGHT_POSITION, slider.values[1])
                    }
                } else {
                    /**
                     * Else start value is changed and we don't want to call callback/provider function if it is first slider (i.e. 0)*/
                    if (index == 0) slider.values = mutableListOf(0f, slider.values[1])
                    else {
                        onSliderStopsAndUpdateValue?.let {
                            it(index!!, LEFT_POSITION, slider.values[0])
                        }
                    }
                }

            }
        })

        eachRangebarBinding.rangeBar.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {

                val position:Int = if (value == slider.values[0] && value != rangeBarItem.start.toFloat()){
                    LEFT_POSITION
                } else if(value == slider.values[1] && value != rangeBarItem.end.toFloat()){
                    RIGHT_POSITION
                }else -1

                if (index == 0 && position == LEFT_POSITION) {
                    /**
                     * 0 value is being moved and we will return nothing */
                    Log.d("ValuesToIndex","$index ${slider.values[1]} ${rangeBarItem.end}")
                    slider.values = mutableListOf(rangeBarItem.start.toFloat(),slider.values[1])
                    return@addOnChangeListener
                } else {
                    if (position!=-1){
                        onSliderChange?.let {
                            index?.let { it1 -> it(it1, position, value) }
                        }
                    }

                }
            }
        }
    }


    /**
     * This fuction will take the latest value of tempArray element at the same position and will update the rangebar
     * this is usefull as it will reflects if this element is not moved from user then on changing other elements
     * of tempArray it will notify the other rangebar in the list*/
    fun notifyRangeBarComponentUIForUpdateTextIconsAndValues() {
        getTempArrayList?.let {
            it().let { list ->
                index?.let { ind ->
                    eachRangebarBinding.rangeBar.values =
                        mutableListOf(list[ind].start.toFloat(), list[ind].end.toFloat())
                    eachRangebarBinding.startTxt.text = list[ind].start.toString()
                    eachRangebarBinding.endTxt.text = list[ind].end.toString()
                    if (abs(list[ind].start - list[ind].end)<5) {
                        eachRangebarBinding.leftImage.visibility = View.VISIBLE
                        eachRangebarBinding.rightImage.visibility = View.VISIBLE
                    } else {
                        eachRangebarBinding.leftImage.visibility = View.GONE
                        eachRangebarBinding.rightImage.visibility = View.GONE
                    }
                }

            }
        }
    }

    /**
     * This is internal function that sets the colors to trackbar and thumb*/
    var currentColor = 0
    private fun setColor(color: Int) {
        currentColor = color
        ImageViewCompat.setImageTintList(
            eachRangebarBinding.colorPickerBtn,
            ColorStateList.valueOf(color)
        )
        val states = arrayOf(intArrayOf(android.R.attr.state_enabled))
        val colors = intArrayOf(color)
        val myList = ColorStateList(states, colors)
        eachRangebarBinding.rangeBar.trackActiveTintList = myList
        eachRangebarBinding.rangeBar.thumbTintList = myList
    }
}