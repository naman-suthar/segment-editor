package app.ijp.segmentation_editor.segment_editor

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.children
import app.ijp.segmentation_editor.R
import app.ijp.segmentation_editor.databinding.FragmentSegmentRangeBarsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import app.ijp.segmentation_editor.extras.model.RangeBarArray
import kotlin.math.abs

const val LEFT_POSITION = 0
const val RIGHT_POSITION = 1

/**
 * This Fragment is parent of sliders and holds lists the sliders
 * for this It requires List of RangeBar array which it get from the Activity/Parent Fragment of app from setArrayList() function (Mandatory)
 * It also requires colorHistory to pass colorHistory to the sliders which it gets from setColorHistoryProvider() function (Optional)
 * It gives to callback function which it gets from sliders and it passes it to parent fragment or activity from setOnValueChange() and setOnSliderChange() both are mandatory
 * */

/**
 * Functions
 * 1. setColorHistoryProvider() -> It sets the "getColorHistory" And this is passed o rangebar Component
 * 2. setArrayList() -> This function sets the "getArrayList" which is used to draw Rangebars
 * 3. setOnValueChange() -> This function is passed to Rangebar Component On slider movement completion and user change the value from dialogBox
 * 4. setOnSliderChange() -> This function is passed to rangebar Component On slider Is moving
 * */

class SegmentRangeBarsFragment : Fragment() {


    private var arrayList = mutableListOf<RangeBarArray>()
    private var alertDialog: AlertDialog? = null
    private var onValueChange: ((MutableList<RangeBarArray>, Int?) -> Unit)? = null
    private var onSLiderChange: ((MutableList<RangeBarArray>) -> Unit)? = null
    private var binding: FragmentSegmentRangeBarsBinding? = null
    private var getColorHistory: (() -> List<Int>?)? = null
    private var getArrayList: (() -> MutableList<RangeBarArray>)? = null

    /**
     * This is used as Temporary State holder for Sliders
     * and it holds Copy of RangeBarArray and perform temporary changes
     * since we can not make changes in original database when users is sliding the rangebar
     * It is internal state holder we don't have to define or declare from outside
     * */
    private var temporaryRangeBarArrayUsedForTextAndDeleteIconUpdateInLive = mutableListOf<RangeBarArray>()

    /**
     * This function is sets "getArrayList" which is used to get latest Arraylist */
    fun setArrayList(arrayListProvider: (() -> MutableList<RangeBarArray>)?) {
        getArrayList = arrayListProvider
    }

    /**
     * This function sets "getColorHistory" which is passed to RangebarItemComponent For ColorDialog*/
    fun setColorHistoryProvider(colorHistoryProvider: (() -> List<Int>?)?) {
        getColorHistory = colorHistoryProvider
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSegmentRangeBarsBinding.inflate(inflater, container, false)

        defaultColor = try {
            val typedValue = TypedValue()
            context?.theme?.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
            val primaryColor = typedValue.data
            primaryColor
        } catch (e: Exception) {
            context?.let {
                ContextCompat.getColor(
                    it,
                    com.google.android.material.R.color.design_default_color_primary
                )
            } ?: 0
        }

        getArrayList?.let {
            it().let { list ->
                arrayList = list
                if (list.isNotEmpty()) drawBarsToUi()
            }
        }

        return binding?.root
    }

    /**
     * This function we gets from parent and now we call it when rangeSlider gives us callback
     * RangeBarComponent.setOnSliderMovementCompletion()
     * */
    fun setOnValueChange(onValueChangeFunction: ((MutableList<RangeBarArray>, Int?) -> Unit)?) {
        onValueChange = onValueChangeFunction
    }

    /**
     * This function we gets from parent and now we call it when rangeSlider gives us callback
     * RangeBarComponent.setOnSliderChangingLive()
     * */
    fun setOnSliderChange(onSliderChangeFunction: ((MutableList<RangeBarArray>) -> Unit)?) {
        onSLiderChange = onSliderChangeFunction
    }

    fun updateSliders() {
        getArrayList?.let {
            it().let { list ->
                arrayList = list
                drawBarsToUi()
            }
        }
    }

    private fun showAlertDialog(index: Int, position: Int) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
        val view =
            layoutInflater.inflate(R.layout.on_range_value_text_dialog_layout, null, false)
        val etRangeValue: EditText = view.findViewById(R.id.et_enter_range_value)
        when (position) {
            LEFT_POSITION -> etRangeValue.setText(arrayList[index].start.toString())
            RIGHT_POSITION -> etRangeValue.setText(arrayList[index].end.toString())
        }

        dialog.setView(view)
        dialog.setTitle("Enter Value")
        dialog.setPositiveButton("Apply", null)
        dialog.setNegativeButton("Cancel") { d, _ ->
            d.dismiss()
        }
        alertDialog = dialog.create()
        alertDialog?.show()
        val positiveButton = alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton?.setOnClickListener {
            val value = etRangeValue.text.toString().toInt()
            val changed = validateAndUpdateRangeBarArray(index, position, value)
            if (changed){
                onValueChange?.let {
                    it(arrayList, null)
                }
                alertDialog?.dismiss()
            }
            Log.d("ChangedValue","$changed")
        }
    }
    private fun drawBarsToUi() {
        binding?.rangeBarComponent?.removeAllViews()
        arrayList.forEachIndexed {indx, rangeBar ->
            /**
             * Creating a new RangeBarSlider Component for each element in Array*/
            val rangeBarView =
                RangeBarSliderCustomComponent(
                    requireContext(),
                    childFragmentManager,
                    rangeBar,
                    getColorHistory,
                    indx
                )

            /**
             * Providing reference to the tempRangeBarArray*/
            rangeBarView.getTempArray {
                temporaryRangeBarArrayUsedForTextAndDeleteIconUpdateInLive
            }

            rangeBarView.setOnSliderMovementCompletion { index, position, value ->
                /**
                 * index -> Index of the Slider where value ha been changed or slider movement performed
                 * position -> It is Thumb Position of Slider i.e. LEFT or RIGHT
                 * value -> It is the changed value on that position (ex. If right thumb is moved from 21 to 80 then value here is 80
                 * */

//                updateAndReArrangeRangeBarArray(index, position, value.toInt())
                val changed = validateAndUpdateRangeBarArray(index,position,value.toInt())

                /**
                 * Update temporary Array as copy of updated Range BarArray
                 * If RangeBarChanged then new State else back to previous state
                 * */
                temporaryRangeBarArrayUsedForTextAndDeleteIconUpdateInLive.clear()
                arrayList.forEach { ra ->
                    temporaryRangeBarArrayUsedForTextAndDeleteIconUpdateInLive.add(
                        RangeBarArray(
                            ra.start,
                            ra.end,
                            ra.color
                        )
                    )
                }

                onValueChange?.let {
                    /** This function is basically provided in activity or parent Fragment to store new Array list in db*/
                    it(arrayList, null)
                }
                /**If ArrayList changed then drawBars to UI and call onValueChange callback/Provider else refresh Slider back to original state*/
                if (changed){

                    drawBarsToUi()
                }else{
                    binding?.rangeBarComponent?.children?.forEach {
                        (it as RangeBarSliderCustomComponent).notifyRangeBarComponentUIForUpdateTextIconsAndValues()
                    }
                }

            }
            rangeBarView.setOnSliderChangingLive { index, position, value ->
                /**First update the Temporary Array of Internal state */
                when (position) {
                    LEFT_POSITION -> {
                        updateTemporaryArrayFromLeftPosition(value.toInt(), index)
                    }
                    else -> {
                        updateTemporaryArrayFromRightPosition(value.toInt(), index)
                    }
                }
                /**Then Refresh the Text and values of RangeBarComponents*/
                binding?.rangeBarComponent?.children?.forEach {
                    (it as RangeBarSliderCustomComponent).notifyRangeBarComponentUIForUpdateTextIconsAndValues()
                }
                onSLiderChange?.let {
                    /**This function will be set in activity to just update the array for preview*/
                    it(temporaryRangeBarArrayUsedForTextAndDeleteIconUpdateInLive)
                }
            }
            rangeBarView.setOnValueTextItemClicked { index, position ->
                showAlertDialog(index, position)
            }
            rangeBarView.setOnColorChangedFromRangeBar { index, newColor ->
                arrayList[index].color = newColor
                onValueChange?.let {
                    /**
                     * This function will be set in activity to save the new color and add to history*/
                    it(arrayList, newColor)
                }
            }
            binding?.rangeBarComponent?.addView(rangeBarView)
        }
    }
    private fun validateAndUpdateRangeBarArray(index: Int, position: Int, value: Int): Boolean {
        if (value in 1..5 || value in 95..99) {
            Toast.makeText(requireContext(), "Minimum length 5 required for segment", Toast.LENGTH_SHORT)
                .show()
        } else if (value > 100) {
            Toast.makeText(requireContext(), "Enter Valid Input", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("SettingsChecking","$index $value")
            if (index == arrayList.size - 1 && position == RIGHT_POSITION) {
                /**
                 * Last value 100 updated
                 * Create New if it is between last rangebar start and end
                 * else UpdateForLast will update the existing RangeBar
                 * */
                if (value in arrayList[index].start..arrayList[index].end) {
                    val isCorrect = createNewBar(value, index)
                    return if (isCorrect.first) {
                        /** If successFully Arranged then Update in ViewModel
                         * */

                        true
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Already ${isCorrect.second} in the list Minimum 5 length required for segment",
                            Toast.LENGTH_SHORT
                        ).show()
                        false
                    }
                } else {
                    val isCorrect = updateRangeBarForLastSliderCase(value, position)
                    return if (isCorrect.first) {
                        /** If successFully Arranged then Update in ViewModel
                         * */

                        true
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Already ${isCorrect.second} in the list Minimum 5 length required",
                            Toast.LENGTH_SHORT
                        ).show()
                        false
                    }
                }
            } else {
                val isCorrect = when (position) {
                    /**
                     * First find the Index of Range in between where input value lies and then change according start and ends of corresponding rangebars
                     * @param foundAtPos inside checkLeftAndUpdate and checkRightAndUpdate
                     * In LeftPosition will change some above or below rangebars values and itself become the start of new RangeBar or updated Rangebar
                     * Similarly Right Position will change some above or below rangebars values and become the end of updated Rangebar
                     *
                     **/

                   /* LEFT_POSITION -> checkLeftAndUpdate(value, index)
                    RIGHT_POSITION -> checkRightAndUpdate(value, index)*/
                    LEFT_POSITION -> checkRangeBarArrayForLeftThumbAndUpdate(value, index)
                    RIGHT_POSITION -> checkRangeBarForRightThumbAndUpdate(value, index)
                    else -> {
                        Pair(false, -2)
                    }
                }
                return if (isCorrect.first) {
                    /** If successFully Arranged then Update in ViewModel
                     * */

                    true
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Already ${isCorrect.second} in the list Minimum 5 length required",
                        Toast.LENGTH_SHORT
                    ).show()
                    false
                }
            }

        }
        return false
    }

    /**This handles the case when last range bar is not going to create New but Update Existing
     * basically the value is not between the start and end of the last element
     * then it will update the Array accordingly And it is called by Dialog box*/



    /**
     * This function handle case when the last slider is not creating new Slider and value is affecting other existing sliders*/
    private fun updateRangeBarForLastSliderCase(value: Int, position: Int): Pair<Boolean, Int> {
        val tempArray = mutableListOf<RangeBarArray>()

        var foundAtPos: Int = -1
        arrayList.forEachIndexed { i, ra ->
            tempArray.add(RangeBarArray(ra.start, ra.end, ra.color))
            if (value in ra.start..ra.end) {
                foundAtPos = i
            }
        }

        if (abs(tempArray[foundAtPos].start - value) in 1..5) return Pair(
            false,
            tempArray[foundAtPos].start
        )
        if (abs(value + 1 - 100) < 5) return Pair(false, value + 1)
        tempArray[foundAtPos].end = value
        tempArray[foundAtPos + 1].start = value + 1
        tempArray[foundAtPos + 1].end = 100
        var temp = foundAtPos + 1
        while (temp + 1 < tempArray.size) {
            tempArray[temp + 1].end = 0
            temp++
        }
        val ansTemp = tempArray.filter { it.end != 0 }

        arrayList = ansTemp as MutableList<RangeBarArray>

        return Pair(true, -1)
    }

    /**This handles the special case of last Slider when its value is between its own start and end it means
     * It breaks down into two sliders and creates a new Slider*/

    private fun createNewBar(value: Int, index: Int): Pair<Boolean, Int> {
        val tempArr = mutableListOf<RangeBarArray>()
        arrayList.forEach { ra ->
            tempArr.add(RangeBarArray(ra.start, ra.end, ra.color))
        }
        if (abs(tempArr[index].start - value) in 1..5) return Pair(
            false,
            tempArr[index].start
        )
        if (abs(tempArr[index].start - value)==0){
            if (index-1>=0) {
                tempArr[index-1].end = 100
                tempArr[index].start = tempArr[index].end
            }

        }else{
            tempArr[index].end = value
            tempArr.add(RangeBarArray(value + 1, 100, defaultColor))
        }


        arrayList = tempArr.filter { it.end > it.start} as MutableList<RangeBarArray>
        return Pair(true, -1)
    }

    private fun checkRangeBarForRightThumbAndUpdate(value: Int, index: Int): Pair<Boolean, Int> {
        val tempArray = mutableListOf<RangeBarArray>()
        var foundAtPos: Int = -1

        arrayList.forEachIndexed { i, ra ->
            tempArray.add(RangeBarArray(ra.start, ra.end, ra.color))
            if (value in ra.start..ra.end) {
                Log.d("DLoginLoopRightThumb:","searching -> $value in [${ra.start} .. ${ra.end}]")
                foundAtPos = i
            }
        }
        Log.d("DLog:","value -> $value foundAt -> $foundAtPos")
        if (foundAtPos == index) {

            if (abs(tempArray[index].start - value) in 1..5) {

                return Pair(
                    false,
                    tempArray[index].start
                )
            }

            if (abs(tempArray[index + 1].end - value) in 1..5) {

                return Pair(
                    false,
                    tempArray[index + 1].end
                )
            }
            if (abs(tempArray[index].start - value) == 0) {
                tempArray[index].end = 0
                tempArray[index + 1].start = tempArray[index].start
            } else {
                tempArray[index].end = value
                tempArray[index + 1].start = value + 1
            }

        }
        if (foundAtPos < index) {
            if (abs(tempArray[foundAtPos].start - value) in 1..5) {
                return Pair(
                    false,
                    tempArray[foundAtPos].start
                )
            }

            if (abs(tempArray[index + 1].end - value) in 1..5) {
                return Pair(
                    false,
                    tempArray[index + 1].end
                )
            }

            tempArray[foundAtPos].end = value
            tempArray[index + 1].start = value + 1
            var temp = foundAtPos
            while (temp + 1 <= index) {
                tempArray[temp + 1].end = 0
                temp++
            }
        }
        if (foundAtPos > index) {
            if (abs(tempArray[index].start - value) in 1..5) {
                return Pair(
                    false,
                    tempArray[index].start
                )
            }

            if (abs(tempArray[foundAtPos].end - value) in 1..5) {
                if (foundAtPos+1<tempArray.size){
                    tempArray[foundAtPos+1].start = value+1
                    tempArray[foundAtPos].end = tempArray[foundAtPos].start
//                    Toast.makeText(requireContext(),"Segment at position ${foundAtPos+1} deleted because of small length",Toast.LENGTH_SHORT).show()
                }else return Pair(false, tempArray[foundAtPos].end)

            }

            tempArray[index].end = value
            if (value >= 100) {
                tempArray[foundAtPos].start = 100
            } else {
                tempArray[foundAtPos].start = value + 1
            }


            var temp = index
            while (temp + 1 < foundAtPos) {
                tempArray[temp + 1].end = 0
                temp++
            }
        }
        val ansTemp =
            tempArray.filter { it.end != 0 }.filter { it.end > it.start }

        arrayList = ansTemp as MutableList<RangeBarArray>

        return Pair(true, -1)
    }

    private fun checkRangeBarArrayForLeftThumbAndUpdate(
        value: Int,
        position: Int
    ): Pair<Boolean, Int> {
        val tempArray = mutableListOf<RangeBarArray>()

        var foundAtPos: Int = 0
        arrayList.forEachIndexed { i, ra ->
            tempArray.add(RangeBarArray(ra.start, ra.end, ra.color))
            Log.d("DLoginLoopLeftThumb:","searching -> $value in [${ra.start} .. ${ra.end}]")
            if (value in ra.start..ra.end) {
                foundAtPos = i

            }
        }
        Log.d("DLog:","value -> $value foundAt -> $foundAtPos")
        if (foundAtPos == position) {
            Log.d("CheckingSelf","Yes")
            if (abs(tempArray[position].end - value) in 1..5) {
                return Pair(false, tempArray[position].end)
            }
            if (abs(tempArray[position - 1].start - value) in 1..5) {
                return Pair(false, tempArray[position - 1].start)
            }
            if (abs(tempArray[position].end - value) == 0) {
                tempArray[position].start = tempArray[position].end
                tempArray[position - 1].end = tempArray[position].end
            } else {
                tempArray[position].start = value
                tempArray[position - 1].end = value-1
            }

        }
        if (foundAtPos < position) {
            if (abs(tempArray[foundAtPos].start - (value)) in 1..5) {
                if (foundAtPos-1>=0){
                    tempArray[foundAtPos-1].end = value-1
                    tempArray[foundAtPos].start = tempArray[foundAtPos].end
//                    Toast.makeText(requireContext(),"Segment at position ${foundAtPos+1} deleted because of small length",Toast.LENGTH_SHORT).show()
                }else return Pair(false, tempArray[foundAtPos].start)
            }
            if (abs(tempArray[position].end - value) in 1..5) {
                return Pair(false, tempArray[position].end)
            }
            if (value == 0) {
                tempArray[foundAtPos].end = 0
            } else {
                tempArray[foundAtPos].end = value - 1
            }

            tempArray[position].start = value
            var temp = foundAtPos
            while (temp + 1 < position) {
                tempArray[temp + 1].end = 0
                temp++
            }
        }
        if (foundAtPos > position) {
            if (abs(tempArray[foundAtPos].end - value) <= 5) {
                return Pair(false, tempArray[foundAtPos].end)
            }
            tempArray[foundAtPos].start = value
            if (position - 1 >= 0) {
                if (abs(tempArray[position - 1].start - value - 1) <= 5) {
                    return Pair(false, tempArray[position - 1].start)
                }
                tempArray[position - 1].end = value - 1
            }

            var temp = position
            while (temp < foundAtPos) {
                tempArray[temp].end = 0
                temp++
            }
        }
        val ansTemp = tempArray.filter { it.end != 0 }.filter { it.end > it.start }
        arrayList = ansTemp as MutableList<RangeBarArray>
        return Pair(true, -1)
    }


    /**
     * The Functions are to Update the Temporary Array*/
    private fun updateTemporaryArrayFromRightPosition(value: Int, index: Int) {
        val tempArray = mutableListOf<RangeBarArray>()
        var foundAtPos: Int = -1
        arrayList.forEachIndexed { i, ra ->
            tempArray.add(RangeBarArray(ra.start, ra.end, ra.color))
            if (value in ra.start..ra.end) {
                foundAtPos = i
            }
        }
        if (foundAtPos == index) {

            tempArray[index].end = value
            if (index + 1 < tempArray.size) {
                if (value >= 100) {
                    tempArray[index + 1].start = 100
                } else {
                    tempArray[index + 1].start = value + 1
                }
            }

        }
        if (foundAtPos < index) {
            tempArray[foundAtPos].end = value
            if (value >= 100) {
                tempArray[index + 1].start = 100
            } else {
                tempArray[index + 1].start = value + 1
            }

            var temp = foundAtPos
            while (temp + 1 <= index) {
                tempArray[temp + 1].end = 0
                temp++
            }
        }
        if (foundAtPos > index) {
            tempArray[index].end = value
            if (value >= 100) {
                tempArray[foundAtPos].start = 100
            } else {
                tempArray[foundAtPos].start = value + 1
            }

            var temp = index
            while (temp + 1 < foundAtPos) {
                tempArray[temp + 1].start = tempArray[temp + 1].end
                temp++
            }
        }

        temporaryRangeBarArrayUsedForTextAndDeleteIconUpdateInLive = tempArray
    }

    private fun updateTemporaryArrayFromLeftPosition(value: Int, position: Int) {
        val tempArray = mutableListOf<RangeBarArray>()

        var foundAtPos: Int = -1
        arrayList.forEachIndexed { i, ra ->
            tempArray.add(RangeBarArray(ra.start, ra.end, ra.color))
            if (value in ra.start+1..ra.end+1) {
                foundAtPos = i
            }
        }
        Log.d("FoundAtPos","$foundAtPos")
        if (foundAtPos == -1) foundAtPos =0
        if (foundAtPos == position) {
            tempArray[position].start = value
            if (position - 1 >= 0 && value > 0) {
                tempArray[position - 1].end = value - 1
            }
        }
        if (foundAtPos < position) {
            if (value == 0) {
                tempArray[position].start = 0
                tempArray[foundAtPos].end = 0
            } else {
                tempArray[foundAtPos].end = value - 1
                tempArray[position].start = value
            }


            var temp = foundAtPos
            while (temp + 1 < position) {
                tempArray[temp + 1].end = tempArray[temp + 1].start
                temp++
            }
        }

        temporaryRangeBarArrayUsedForTextAndDeleteIconUpdateInLive = tempArray
    }
}

/*  private fun addComponents(list: MutableList<RangeBarArray>) {
      binding?.rangeBarComponent?.removeAllViews()
      val lp = ActionBar.LayoutParams(
          ActionBar.LayoutParams.MATCH_PARENT,
          ActionBar.LayoutParams.WRAP_CONTENT
      )
      for (i in list) {
          val newCompo = binding?.rangeBarComponent?.let {
              context?.let { it2 ->
                  CustomComponent(
                      it2,
                      requireActivity().supportFragmentManager,
                     i,
                      getColorHistory
                  )
              }

          }

          val newCompoBinding: EachRangebarBinding? =
              newCompo?.let { EachRangebarBinding.bind(it) }
          if (i.start == 0) {
              newCompoBinding?.thicknessSliderCustom?.visibility = View.VISIBLE
              newCompoBinding?.rangeBar?.visibility = View.INVISIBLE
          }
          newCompo?.setColor(i.color)
          newCompo?.let { nc ->
              binding?.rangeBarComponent?.addView(nc, lp)
          }
      }
  }*/


/*
private fun updateAndReArrangeRangeBarArray(index: Int, position: Int, value: Int) {

    if (value in 1..5 || value in 95..99) {
        Toast.makeText(requireContext(), "Minimun 5 length required", Toast.LENGTH_SHORT)
            .show()
    } else if (value > 100) {
        Toast.makeText(requireContext(), "Enter Valid Input", Toast.LENGTH_SHORT).show()
    } else {

        if (index == arrayList.size - 1 && position == RIGHT_POSITION) {
            */
/**
             * Last value 100 updated
             * Create New if it is between last rangebar start and end
             * else UpdateForLast will update the existing RangeBar
             * *//*

            if (value in arrayList[index].start - 5..arrayList[index].end - 5) {
                val isCorrect = createNewBar(value, index)
                if (!isCorrect.first) {
                    Toast.makeText(
                        requireContext(),
                        "Already ${isCorrect.second} in the list Minimum 5 length required",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            } else {
                val isCorrect = updateRangeBarForLastSliderCase(value, position)
                if (!isCorrect.first) {
                    Toast.makeText(
                        requireContext(),
                        "Already ${isCorrect.second} in the list Minimum 5 length required",
                        Toast.LENGTH_SHORT
                    ).show()
                    */
/*    onValueChange?.let {
                            it(rangeBarArray, null)
                        }
                        alertDialog?.dismiss()*//*

                }
            }
        } else {
            val isCorrect = when (position) {
                */
/**
                 * First find the Index of Range in between where input value lies and then change according start and ends of corresponding rangebars
                 * @param foundAtPos inside checkLeftAndUpdate and checkRightAndUpdate
                 * In LeftPosition will change some above or below rangebars values and itself become the start of new RangeBar or updated Rangebar
                 * Similarly Right Position will change some above or below rangebars values and become the end of updated Rangebar
                 *
                 **//*


                LEFT_POSITION -> checkRangeBarArrayForLeftThumbAndUpdate(value, index)
                RIGHT_POSITION -> checkRangeBarForRightThumbAndUpdate(value, index)

                else -> {
                    Pair(false, -2)
                }
            }
            if (!isCorrect.first) {
                Toast.makeText(
                    requireContext(),
                    "Already ${isCorrect.second} in the list Minimum 5 length required",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }

    }
}*/


/*private fun checkRightAndUpdate(value: Int, index: Int): Pair<Boolean, Int> {

    val tempArray = mutableListOf<RangeBarArray>()
    var foundAtPos: Int = -1

    arrayList.forEachIndexed { i, ra ->
        tempArray.add(RangeBarArray(ra.start, ra.end, ra.color))
        if (value in ra.start..ra.end) {
            foundAtPos = i
        }
    }

    if (foundAtPos == index) {
        if (abs(tempArray[index].start - value) <= 5) return Pair(false, tempArray[index].start)

        if (abs(tempArray[index + 1].end - value) <= 5) return Pair(
            false,
            tempArray[index + 1].end
        )

        tempArray[index].end = value
        tempArray[index + 1].start = value + 1
    }
    if (foundAtPos < index) {
        if (abs(tempArray[foundAtPos].start - value) <= 5) return Pair(
            false,
            tempArray[foundAtPos].start
        )

        if (abs(tempArray[index + 1].end - value + 1) <= 5) return Pair(
            false,
            tempArray[index + 1].end
        )

        tempArray[foundAtPos].end = value
        tempArray[index + 1].start = value + 1
        var temp = foundAtPos
        while (temp + 1 <= index) {
            tempArray[temp + 1].end = 0
            temp++
        }
    }
    if (foundAtPos > index) {
        if (abs(tempArray[index].start - value) <= 5) return Pair(false, tempArray[index].start)

        if (abs(tempArray[foundAtPos].end - value + 1) <= 5) return Pair(
            false,
            tempArray[foundAtPos].end
        )

        tempArray[index].end = value
        tempArray[foundAtPos].start = value + 1

        var temp = index
        while (temp + 1 < foundAtPos) {
            tempArray[temp + 1].end = 0
            temp++
        }
    }
    val ansTemp = tempArray.filter { it.end != 0 }

    arrayList = ansTemp as MutableList<RangeBarArray>

    return Pair(true, -1)
}

private fun checkLeftAndUpdate(value: Int, position: Int): Pair<Boolean, Int> {
    val tempArray = mutableListOf<RangeBarArray>()

    var foundAtPos: Int = -1
    arrayList.forEachIndexed { i, ra ->
        tempArray.add(RangeBarArray(ra.start, ra.end, ra.color))
        if (value in ra.start..ra.end) {
            foundAtPos = i
        }
    }

    if (foundAtPos == position) {
        if (abs(tempArray[position].end - value) <= 5) {
            return Pair(false, tempArray[position].end)
        }
        if (abs(tempArray[position - 1].start - value + 1) <= 5) {
            return Pair(false, tempArray[position - 1].start)
        }
        tempArray[position].start = value
        if (value == 0) {
            tempArray[position - 1].end = 0
        } else tempArray[position - 1].end = value - 1
    }
    if (foundAtPos < position) {
        if (abs(tempArray[foundAtPos].start - (value)) <= 5) {
            return Pair(false, tempArray[foundAtPos].start)
        }
        if (abs(tempArray[position].end - value) <= 5) {
            return Pair(false, tempArray[position].end)
        }
        tempArray[foundAtPos].end = value - 1
        tempArray[position].start = value
        var temp = foundAtPos
        while (temp + 1 < position) {
            tempArray[temp + 1].end = 0
            temp++
        }
    }
    if (foundAtPos > position) {
        if (abs(tempArray[foundAtPos].end - value) <= 5) {
            return Pair(false, tempArray[foundAtPos].end)
        }
        tempArray[foundAtPos].start = value
        if (position - 1 >= 0) {
            if (abs(tempArray[position - 1].start - value - 1) <= 5) {
                return Pair(false, tempArray[position - 1].start)
            }
            tempArray[position - 1].end = value - 1
        }

        var temp = position
        while (temp < foundAtPos) {
            tempArray[temp].end = 0
            temp++
        }
    }
    val ansTemp = tempArray.filter { it.end != 0 }


    arrayList = ansTemp as MutableList<RangeBarArray>
    return Pair(true, -1)
}*/

/*
private fun updateForLast(value: Int, position: Int): Pair<Boolean, Int> {
    val tempArray = mutableListOf<RangeBarArray>()

    var foundAtPos: Int = -1
    arrayList.forEachIndexed { i, ra ->
        tempArray.add(RangeBarArray(ra.start, ra.end, ra.color))
        if (value in ra.start..ra.end) {
            foundAtPos = i
        }
    }

    if (abs(tempArray[foundAtPos].start - value) < 5) return Pair(
        false,
        tempArray[foundAtPos].start
    )
    if (abs(value + 1 - 100) < 5) return Pair(false, value + 1)
    tempArray[foundAtPos].end = value
    tempArray[foundAtPos + 1].start = value + 1
    tempArray[foundAtPos + 1].end = 100
    var temp = foundAtPos + 1
    while (temp + 1 < tempArray.size) {
        tempArray[temp + 1].end = 0
        temp++
    }
    val ansTemp = tempArray.filter { it.end != 0 }

    arrayList = ansTemp as MutableList<RangeBarArray>

    return Pair(true, -1)
}*/
