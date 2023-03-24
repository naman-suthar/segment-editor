package com.naman.segmentation_module.segment_option

import android.app.ActionBar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.naman.segmentation_module.R
import com.naman.segmentation_module.databinding.EachRangebarBinding
import com.naman.segmentation_module.databinding.FragmentSegmentRangeBarsBinding
import com.naman.segmentation_module.model.RangeBarArray
import kotlin.math.abs

const val LEFT_POSITION = 0
const val RIGHT_POSITION = 1
class SegmentRangeBarsFragment : Fragment(), CustomComponentsCallback {
    private var arrayList = mutableListOf<RangeBarArray>()
    private var alertDialog: AlertDialog? = null
    private var onValueChange: ((MutableList<RangeBarArray>, Int?) -> Unit)? = null
    private var onSLiderChange: ((MutableList<RangeBarArray>) -> Unit)? = null
    private var onValueTextClick: ((Int, Int) -> Unit)? = null
    private var binding: FragmentSegmentRangeBarsBinding? = null
    private var colorHistory: List<Int>? = null
    private var getColorHistory: (() -> List<Int>?)? = null
    private var getArrayList: (() -> MutableList<RangeBarArray>)? = null

    fun setArrayList(arrayListProvider: (() -> MutableList<RangeBarArray>)?) {
        getArrayList = arrayListProvider
    }

    fun setColorHistoryProvider(colorHistoryProvider: (() -> List<Int>?)?) {
        getColorHistory = colorHistoryProvider
        /*colorHistory = colorHistoryProvider
        arrayList.let {
            addComponents(arrayList)

        }*/
    }

    fun setTextValueClickListener(onValueTextClicked: ((Int, Int) -> Unit)) {
        onValueTextClick = onValueTextClicked
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSegmentRangeBarsBinding.inflate(inflater, container, false)
        getArrayList?.let {
            it()?.let { list ->
                //Creating the rangeBars from database
                //array to remove duplicate values
                arrayList = list
                if (list.isNotEmpty()) addComponents(list)
            }
        }

        return binding?.root
    }

    fun setOnValueChange(onValueChangeFunction: ((MutableList<RangeBarArray>, Int?) -> Unit)?) {
        onValueChange = onValueChangeFunction
    }

    fun setOnSliderChange(onSliderChangeFunction: ((MutableList<RangeBarArray>) -> Unit)?) {
        onSLiderChange = onSliderChangeFunction
    }

    fun updateNewList() {
        getArrayList?.let {
            it()?.let { list ->
                arrayList = list
                addComponents(list)

            }
        }
//        arrayList = list

    }

    fun addRangeBarListItem(rangeBarItem: RangeBarArray) {
        arrayList.add(rangeBarItem)
        this.view?.invalidate()
    }

    fun getCurrentList(): MutableList<RangeBarArray> {
        return arrayList
    }

    private fun addComponents(list: MutableList<RangeBarArray>) {
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
                        this,
                        requireActivity().supportFragmentManager,
                        binding?.rangeBarComponent!!,
                        arrayList,
                        getColorHistory
                    )
                }

            }
            newCompo?.setStartText(i.start)
            newCompo?.setEndText(i.end)
            newCompo?.setProgress(i.start, i.end)
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
    }


    override fun onValueChanged(arrayList: MutableList<RangeBarArray>, color: Int?) {

        onValueChange?.let {
            it(arrayList, color)
        }
    }

    override fun onSliderChange(temp: MutableList<RangeBarArray>) {

        onSLiderChange?.let {
            it(temp)
        }

    }

    override fun onGridColorChange(color: IntArray) {

    }

    override fun deleteGridColor(cList: ArrayList<String>) {

    }

    override fun onSetManuallyClicked(index: Int, position: Int) {
        showAlertDialog(index, position)
        /* onValueTextClick?.let {
             it(index,position)
         }*/
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
            if (value < 5 || value in 95..100) {
                Toast.makeText(requireContext(), "Minimun 5 length required", Toast.LENGTH_SHORT)
                    .show()
            } else if (value > 100) {
                Toast.makeText(requireContext(), "Enter Valid Input", Toast.LENGTH_SHORT).show()
            } else {

                if (index == arrayList.size - 1 && position == RIGHT_POSITION) {
                    /**
                     * Last value 100 updated
                     * Create New if it is between last rangebar start and end
                     * else UpdateForLast will update the existing RangeBar
                     * */
                    if (value in arrayList[index].start..arrayList[index].end) {
                        val isCorrect = createNewBar(value, index)
                        if (isCorrect.first) {
                            /** If successFully Arranged then Update in ViewModel
                             * */

                            onValueChange?.let {
                                it(arrayList, null)
                            }
                            alertDialog?.dismiss()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Already ${isCorrect.second} in the list Minimum 5 length required",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    } else {
                        val isCorrect = updateForLast(value, position)
                        if (isCorrect.first) {
                            /** If successFully Arranged then Update in ViewModel
                             * */

                            onValueChange?.let {
                                it(arrayList, null)
                            }
                            alertDialog?.dismiss()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Already ${isCorrect.second} in the list Minimum 5 length required",
                                Toast.LENGTH_SHORT
                            ).show()
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

                        LEFT_POSITION -> checkLeftAndUpdate(value, index)
                        RIGHT_POSITION -> checkRightAndUpdate(value, index)
                        else -> {
                            Pair(false, -2)
                        }
                    }
                    if (isCorrect.first) {
                        /** If successFully Arranged then Update in ViewModel
                         * */

                        onValueChange?.let {
                            it(arrayList, null)
                        }
                        alertDialog?.dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Already ${isCorrect.second} in the list Minimum 5 length required",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }

            }

        }
    }

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
    }

    private fun createNewBar(value: Int, index: Int): Pair<Boolean, Int> {
        val tempArr = mutableListOf<RangeBarArray>()
        arrayList.forEach { ra ->
            tempArr.add(RangeBarArray(ra.start, ra.end, ra.color))
        }
        if (abs(tempArr[index].start - value) <= 5) return Pair(false, tempArr[index].start)
        tempArr[index].end = value
        tempArr.add(RangeBarArray(value + 1, 100, defaultColor))

        arrayList = tempArr
        return Pair(true, -1)
    }

    private fun checkRightAndUpdate(value: Int, index: Int): Pair<Boolean, Int> {

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
            tempArray[position - 1].end = value - 1
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
    }
}