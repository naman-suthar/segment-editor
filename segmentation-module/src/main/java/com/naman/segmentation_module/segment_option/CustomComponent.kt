package com.naman.segmentation_module.segment_option

import android.app.ActionBar
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.naman.color_dialog.ColorDialog
import com.naman.color_dialog.OnColorChangedListener
import com.naman.segmentation_module.R
import com.naman.segmentation_module.databinding.EachRangebarBinding
import com.naman.segmentation_module.model.RangeBarArray
import kotlin.math.abs


var defaultColor: Int = 0

class CustomComponent @JvmOverloads constructor(
    context: Context,
    callback: CustomComponentsCallback,
    supportFragmentManager: FragmentManager,
    parentLay: LinearLayout,
    arrayList: MutableList<RangeBarArray>,
    colorHistory: (() -> List<Int>?)?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) :
    ConstraintLayout(context, attrs, defStyle, defStyleRes) {
    val layoutInflator = LayoutInflater.from(context)
    private val eachRangebarBinding = EachRangebarBinding.inflate(layoutInflator, this, true)

    init {

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it,
                R.styleable.CustomComponent, 0, 0
            )

            val start =
                typedArray.getInt(
                    R.styleable.CustomComponent_startText, 10
                )

            val end = typedArray.getInt(
                R.styleable.CustomComponent_endText, 90
            )


            typedArray.recycle()
            setStartText(start)
            setEndText(end)
            setProgress(start, end)

        }

        defaultColor = try {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
            val primaryColor = typedValue.data
            primaryColor
        } catch (e: Exception) {
            ContextCompat.getColor(
                context,
                com.google.android.material.R.color.design_default_color_primary
            )
        }

        eachRangebarBinding.colorPickerBtn.setOnClickListener {

            val colorDialog =
                ColorDialog.newInstance(
                    currentColor,
                    colorHistory = colorHistory,
                    object : OnColorChangedListener {
                        override fun colorChanged(color: Int) {
                            val note = parentLay.indexOfChild(this@CustomComponent)
                            defaultColor = color
                            arrayList[note].color = defaultColor
                            setColor(defaultColor)

                            callback.onValueChanged(arrayList, color)
                        }
                    })
            colorDialog.show(supportFragmentManager, context.getString(R.string.single_tag))

            // TODO ("Attention")
            /*Code to invoke Multi Color Dialog for Gradient Color Configuration's 'Feeling Lucky!' option
            newInstance requires to specify the number of colors currently present for Gradient Color Configuration (it can't be less than 2,) the UI will not delete colors if it's less than 2
             */
//            MultiColorDialog.newInstance(4).show(supportFragmentManager, "multi")
        }
        eachRangebarBinding.startTxt.setOnClickListener {
            val index = parentLay.indexOfChild(this@CustomComponent)
            if (index != 0) {
                callback.onSetManuallyClicked(index, 0)
            }

        }
        eachRangebarBinding.endTxt.setOnClickListener {
            val index = parentLay.indexOfChild(this@CustomComponent)
            callback.onSetManuallyClicked(index = index, 1)

        }

        //variable to hold end of current seekbar because it keeps changing on Progress Change
        var endOfThis = getEndText()
        var startOfThis = getStartText()
        var currentGrabbedComponent = -1


        eachRangebarBinding.rangeBar.addOnSliderTouchListener(object :
            RangeSlider.OnSliderTouchListener {

            override fun onStartTrackingTouch(slider: RangeSlider) {

                currentGrabbedComponent = parentLay.indexOfChild(this@CustomComponent)
                startOfThis = arrayList[currentGrabbedComponent].start
                endOfThis = arrayList[currentGrabbedComponent].end


            }

            //On Stop Is working Perfectly
            override fun onStopTrackingTouch(slider: RangeSlider) {
                // Responds to when slider's touch event is being stopped

                val values = slider.values
                val left = values[0].toInt()
                val right = values[1].toInt()
                /* var isCorrect = true
                 parentLay.children.forEach { cv->
                     val bar = (cv as CustomComponent).findViewById<RangeSlider>(R.id.rangeBar)
                     if (abs(bar.values[0] - bar.values[1])<5f && abs(bar.values[0] - bar.values[1])!=0f) {
                         isCorrect = false
                     }
                 }*/

                if (abs(left - right) < 5 && left != right) {
                    Log.d("Here", "Yes It is")

                } else {
                    Log.d("Here1", "Yes It is")
                    /**
                     * Start pointer moved in between start and end*/
                    var note = currentGrabbedComponent
                    var sze = arrayList.size

                    //If both ends are equal then delete it
                    if (left == right) {
                        Log.d("Here2", "Yes It is")
                        //delete the current node
                        if (left != startOfThis || note == sze - 1) {
                            Log.d("Here3", "Yes It is")
                            arrayList[note - 1].end = endOfThis
                            arrayList.removeAt(note)
                        } else {
                            Log.d("Here4", "Yes It is")
                            arrayList[note + 1].start = startOfThis
                            arrayList.removeAt(note)
                        }


                    } else {
                        //If left is not equal to what it was on start, then it has moved
                        Log.d("Here5", "Yes It is")
                        /**
                         * Start pointer moved in between start and end*/
                        /**Range Slider Moved Between Start and End by EndPointer
                         * */
                        /**Range Slider Moved Ahead Its end is greater than next Start by EndPointer
                         * */
                        if (left != startOfThis) {
                            Log.d("Here6", "Yes It is")
                            /**
                             * Start pointer moved in between start and end*/
                            /**Range Slider Moved behind Its start is less than Previous End by Start Pointer
                             * */
                            if (startOfThis != 0) {
                                //It it is not the first bar, (it's starting is not 0)
                                Log.d("Here7", "Yes It is")
                                /**Range Slider Moved behind Its start is less than Previous End by Start Pointer
                                 * */
                                /**
                                 * Start pointer moved in between start and end*/
                                var deletionNote = 0
                                for (i in 0 until arrayList.size) {

                                    if (left >= arrayList[i].start && left <= arrayList[i].end) {

                                        deletionNote = i

                                        break
                                    }
                                }
                                if (deletionNote == note) {
                                    Log.d("Here8", "Yes It is")
                                    /**
                                     * Start pointer moved in between start and end*/
                                    arrayList[note - 1].end = left - 1
                                    arrayList[note].start = left
                                    arrayList[note].end = endOfThis
                                } else {
                                    Log.d("Here9", "Yes It is")
                                    /**Range Slider Moved behind Its start is less than Previous End by Start Pointer
                                     * */
                                    if (left == 0) {
                                        Log.d("Here10", "Yes It is")
                                        arrayList[note].start = 0
                                        while (note != 0) {
                                            arrayList.removeAt(0)
                                            note--
                                        }
                                    } else {
                                        Log.d("Here11", "Yes It is")
                                        /**Range Slider Moved behind Its start is less than Previous End by Start Pointer
                                         * */
                                        arrayList[deletionNote].end = left - 1
                                        arrayList[note].start = left

                                        deletionNote++
                                        while (deletionNote < note) {
                                            arrayList.removeAt(deletionNote)
                                            note--
                                        }
//                                        Log.d("Here12", "Yes It is")
                                        //Update Again for minimum and adjust i.end
                                        val tempArray = mutableListOf<RangeBarArray>()
                                        var foundat = -1
                                        arrayList.forEachIndexed { i, ra ->

                                            Log.d("Doing", "${ra.start} ${ra.end}")
                                            tempArray.add(
                                                i,
                                                RangeBarArray(ra.start, ra.end, ra.color)
                                            )
                                            if (abs(ra.start - ra.end) in 1..5) {
                                                foundat = i
                                            }
                                        }
                                        if (foundat != -1) {

                                            tempArray[foundat].end = tempArray[foundat].start + 5
                                            tempArray[foundat + 1].start =
                                                tempArray[foundat].end + 1

                                        }
                                        arrayList.clear()
                                        arrayList.addAll(tempArray)
                                    }
                                }
                            }

                        } else {
                            Log.d("Here12", "Yes It is")
                            /**Range Slider Moved Between Start and End by EndPointer
                             * */
                            /**Range Slider Moved Ahead Its end is greater than next Start by EndPointer
                             * */
                            if (endOfThis == 100 || note == sze - 1) {
                                Log.d("Here13", "Yes It is")
                                //If its starting is zero but there is only onr bar
                                if (right - left < 6 || 100 - right < 6) {
                                    Log.d("Here14", "Yes It is")
                                    setStartText(startOfThis)
                                    setProgress(startOfThis, 100)
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.min_text2),
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                } else {
                                    Log.d("Here15", "Yes It is")

                                    val rangeBar1 = RangeBarArray(
                                        right + 1,
                                        100,
                                        defaultColor
                                    )
                                    arrayList[note].end = right
                                    arrayList.add(note + 1, rangeBar1)

                                }
                            } else {
                                /**Range Slider Moved Between Start and End by EndPointer
                                 * */
                                /**Range Slider Moved Ahead Its end is greater than next Start by EndPointer
                                 * */
                                Log.d("Here16", "Yes It is")
                                //If is the first bar and there are more than one rangeBars
                                var deletionNote = note
                                for (i in 0 until arrayList.size) {
                                    if (right >= arrayList[i].start && right <= arrayList[i].end) {

                                        deletionNote = i

                                        break
                                    }
                                }
                                if (deletionNote == note) {
                                    /**Range Slider Moved Between Start and End by EndPointer
                                     * */
                                    Log.d("Here17", "Yes It is")
                                    arrayList[note + 1].start = right + 1
                                    arrayList[note].start = startOfThis
                                    arrayList[note].end = right
                                } else {
                                    /**Range Slider Moved Ahead Its end is greater than next Start by EndPointer
                                     * */
                                    Log.d("Here18", "Yes It is")
                                    if (right == 100) {
                                        Log.d("Here19", "Yes It is")
                                        arrayList[note].end = 100
                                        while (note != sze - 1) {
                                            arrayList.removeAt(sze - 1)
                                            sze--
                                        }
                                    } else {
                                        /**Range Slider Moved Ahead Its end is greater than next Start by EndPointer
                                         * */
                                        Log.d("Here20", "Yes It is")
                                        arrayList[deletionNote].start = right + 1
                                        arrayList[note].end = right

                                        deletionNote--
                                        while (deletionNote > note) {
                                            arrayList.removeAt(deletionNote)
                                            deletionNote--
                                        }
                                        //Update Again for minimum and adjust i.start
                                        val tempArray = mutableListOf<RangeBarArray>()
                                        var foundat = -1
                                        arrayList.forEachIndexed { i, ra ->

                                            tempArray.add(
                                                i,
                                                RangeBarArray(ra.start, ra.end, ra.color)
                                            )
                                            if (abs(ra.start - ra.end) in 1..5) {
                                                foundat = i
                                            }
                                        }
                                        if (foundat != -1) {

                                            tempArray[foundat].start = tempArray[foundat].end - 5
                                            tempArray[foundat - 1].end =
                                                tempArray[foundat].start - 1

                                        }
                                        arrayList.clear()
                                        arrayList.addAll(tempArray)
                                    }
                                }


                            }
                        }

                    }
                }





                callback.onValueChanged(arrayList, null)
            }
        })
        var prevousLeft = -1
        var previousRight = -1
        eachRangebarBinding.rangeBar.addOnChangeListener { rangeSlider, value, fromUser ->
            // Responds to when slider's value is changed

            val values = rangeSlider.values
            val left = values[0].toInt()
            val right = values[1].toInt()
           if (prevousLeft != -1){
               Log.d(
                   "MyValues",
                   "$left $prevousLeft $previousRight"
               )
               setStartText(left)
               setEndText(right)

               //to make the delete image  Visible
               if (values[0] == values[1]) {
                   eachRangebarBinding.leftImage.visibility = View.VISIBLE
                   eachRangebarBinding.rightImage.visibility = View.VISIBLE
               }
               if (values[0] != values[1]) {
                   eachRangebarBinding.rightImage.visibility = View.GONE
                   eachRangebarBinding.leftImage.visibility = View.GONE
               }


               var note = -1
               for (i in 0 until arrayList.size) {

                   if (
                       startOfThis >= arrayList[i].start && endOfThis <= arrayList[i].end
                   ) {
                       note = i
                       break
                   }

               }

               var sze = arrayList.size

               if (left != startOfThis) {
                   if (note <= 0) {

                       if (note == 0) {
                           rangeSlider.setValues(0f, arrayList[0].end.toFloat())

                       } else {
                           //There is some error with note
                       }

                   } else {
                       var i = 1

                       if (left != 0) {
                           while (left < arrayList[note - i].start) {
                               val temp = parentLay.getChildAt(note - i)
                               val tempBinding = EachRangebarBinding.bind(temp)
                               tempBinding.rangeBar.setValues(
                                   arrayList[note - i].start.toFloat(),
                                   arrayList[note - i].start.toFloat()
                               )
                               tempBinding.rangeBar.visibility = View.INVISIBLE
                               tempBinding.thicknessSliderCustom.visibility = View.INVISIBLE
                               i++
                           }
                           var j = 0
                           while (left > arrayList[j].start) {
                               val temp = parentLay.getChildAt(j)
                               val tempBinding = EachRangebarBinding.bind(temp)
                               tempBinding.rangeBar.visibility = View.VISIBLE
                               if (j == 0) {
                                   tempBinding.rangeBar.visibility = View.INVISIBLE
                                   tempBinding.thicknessSliderCustom.visibility = View.VISIBLE
                               }

                               j++
                               if (j == sze) {
                                   break
                               }

                           }
                           var k = 0
                           while (left > arrayList[k].end) {
                               val temp = parentLay.getChildAt(k)
                               val tempBinding = EachRangebarBinding.bind(temp)
                               tempBinding.rangeBar.setValues(
                                   arrayList[k].start.toFloat(),
                                   arrayList[k].end.toFloat()
                               )
                               if (k == 0) {
                                   tempBinding.thicknessSliderCustom.value = arrayList[k].end.toFloat()
                               }
                               k++
                           }

                           val previousComponent = parentLay.getChildAt(note - i)
                           val previousBinding = EachRangebarBinding.bind(previousComponent)

                           val startOfPrevious = arrayList[note - i].start.toFloat()
                           val endOfPrevious =
                               (left).toFloat()
                           previousBinding.rangeBar.setValues(startOfPrevious, endOfPrevious)
                           previousBinding.thicknessSliderCustom.value = endOfPrevious
                       } else {
                           val temp = parentLay.getChildAt(0)
                           val tempBinding = EachRangebarBinding.bind(temp)
                           tempBinding.rangeBar.setValues(left.toFloat(), left.toFloat())
                           tempBinding.rangeBar.visibility = View.INVISIBLE
                           tempBinding.thicknessSliderCustom.visibility = View.INVISIBLE

                       }

                   }
               }
               if (currentGrabbedComponent != -1) {

                   if (right != endOfThis && note == currentGrabbedComponent) {
                       if (note >= sze - 1) {

                           if (note == sze - 1) {

                               //Live bar View logic
                               val barView = LinearLayout(context)
                               val rangebarView = parentLay.getChildAt(note)
                               val rangebarViewBinding = EachRangebarBinding.bind(rangebarView)
                               var weightOfThis: Float = 100 - rangebarViewBinding.rangeBar.values[1]
                               val barViewLp = LinearLayout.LayoutParams(
                                   0,
                                   ActionBar.LayoutParams.MATCH_PARENT,
                                   weightOfThis
                               )
                               barView.orientation = LinearLayout.HORIZONTAL
                               barView.setBackgroundColor(Color.parseColor("#2D9EDEFA"))
                               barView.layoutParams = barViewLp
//                            liveBarParentLayout.addView(barView)


                           } else {
                               //There is some error with note
                           }

                       } else {
                           var i = 1
                           if (right != 100) {
                               while (right > arrayList[note + i].end) {
                                   val temp = parentLay.getChildAt(note + i)
                                   val tempBinding = EachRangebarBinding.bind(temp)


                                   tempBinding?.rangeBar?.setValues(
                                       arrayList[note + i].end.toFloat(),
                                       arrayList[note + i].end.toFloat()
                                   )
                                   tempBinding?.rangeBar?.visibility = View.INVISIBLE

                                   i++
                               }
                               var j = sze - 1
                               while (right < arrayList[j].end) {
                                   val temp = parentLay.getChildAt(j)
                                   val tempBinding = EachRangebarBinding.bind(temp)
                                   tempBinding.rangeBar.visibility = View.VISIBLE
                                   j--
                                   if (j == 0) {
                                       break
                                   }

                               }
                               var k = sze - 1
                               while (right < arrayList[k].start) {
                                   val temp = parentLay.getChildAt(k)
                                   val tempBinding = EachRangebarBinding.bind(temp)

                                   tempBinding.rangeBar.setValues(
                                       arrayList[k].start.toFloat(),
                                       arrayList[k].end.toFloat()
                                   )

                                   k--
                               }
                               val nextComponent = parentLay.getChildAt(note + i)
                               val nextComponentBinding = EachRangebarBinding.bind(nextComponent)

                               val startOfNext = right.toFloat()
                               val endOfNext =
                                   (arrayList[note + i].end).toFloat()
                               nextComponentBinding?.rangeBar?.setValues(startOfNext, endOfNext)

                           } else {
                               val temp = parentLay.getChildAt(sze - 1)
                               val tempBinding = EachRangebarBinding.bind(temp)
                               tempBinding.rangeBar.setValues(right.toFloat(), right.toFloat())
                               tempBinding.rangeBar.visibility = View.INVISIBLE

                           }

                       }

                   }

               }

               /**
                * For Live Preview Temporary Array */

               val tempArr = mutableListOf<RangeBarArray>()
               arrayList.forEach {
                   tempArr.add(RangeBarArray(it.start, it.end, it.color))
               }
               var sze2 = tempArr.size
               var note2 = -1
               for (i in 0 until tempArr.size) {

                   if (
                       startOfThis >= tempArr[i].start && endOfThis <= tempArr[i].end
                   ) {
                       note2 = i
                       break
                   }

               }
               if (note2 != -1) {
                   if (left == right) {
                       //delete the current node
                       if (left != startOfThis || note2 == sze2 - 1) {
                           tempArr[note2 - 1].end = endOfThis
                           tempArr.removeAt(note2)
                       } else {
                           tempArr[note2 + 1].start = startOfThis
                           tempArr.removeAt(note2)
                       }


                   } else {
                       //If left is not equal to what it was on start, then it has moved
                       if (left != startOfThis) {

                           if (startOfThis != 0) {
                               //It it is not the first bar, (it's starting is not 0)
                               var deletionNote = 0
                               for (i in 0 until tempArr.size) {
                                   if (left >= tempArr[i].start && left <= tempArr[i].end) {

                                       deletionNote = i

                                       break
                                   }
                               }
                               if (deletionNote == note2) {
                                   tempArr[note2 - 1].end = left - 1
                                   tempArr[note2].start = left
                                   tempArr[note2].end = endOfThis
                               } else {
                                   if (left == 0) {
                                       tempArr[note2].start = 0
                                       while (note2 != 0) {
                                           tempArr.removeAt(0)
                                           note2--
                                       }
                                   } else {
                                       tempArr[deletionNote].end = left - 1
                                       tempArr[note2].start = left

                                       deletionNote++
                                       while (deletionNote < note2) {
                                           tempArr.removeAt(deletionNote)
                                           note2--
                                       }
                                   }
                               }
                           }

                       } else {

                           if (endOfThis == 100 || note2 == sze2 - 1) {
                               //If its starting is zero but there is only onr bar
                               if (right - left < 6 || 100 - right < 6) {
                                   //DO Nothing
                               } else {

                                   val rangeBar1 = RangeBarArray(
                                       right + 1,
                                       100,
                                       defaultColor
                                   )
                                   tempArr[note2].end = right
                                   tempArr.add(note2 + 1, rangeBar1)

                               }
                           } else {
                               //If is the first bar and there are more than one rangeBars
                               var deletionNote = note2
                               for (i in 0 until tempArr.size) {
                                   if (right >= tempArr[i].start && right <= tempArr[i].end) {

                                       deletionNote = i

                                       break
                                   }
                               }
                               if (deletionNote == note2) {
                                   tempArr[note2 + 1].start = right + 1
                                   tempArr[note2].start = startOfThis
                                   tempArr[note2].end = right
                               } else {
                                   if (right == 100) {
                                       tempArr[note2].end = 100
                                       while (note2 != sze2 - 1) {
                                           tempArr.removeAt(sze2 - 1)
                                           sze2--
                                       }
                                   } else {
                                       tempArr[deletionNote].start = right + 1
                                       tempArr[note2].end = right

                                       deletionNote--
                                       while (deletionNote > note2) {
                                           tempArr.removeAt(deletionNote)
                                           deletionNote--
                                       }
                                   }
                               }

                           }
                       }
                   }


               }
               callback.onSliderChange(tempArr)
           }

            prevousLeft = left
            previousRight = right
            //to change the endtext

        }

        //From here is the logic for single Thumb bar that is only visible on first bar.

        eachRangebarBinding.thicknessSliderCustom.addOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started

                startOfThis = 0
                endOfThis = slider.value.toInt()
                currentGrabbedComponent = parentLay.indexOfChild(this@CustomComponent)
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being stopped

                val note = parentLay.indexOfChild(this@CustomComponent)
                val end = arrayList[note].end
                val left = 0
                val right = slider.value.toInt()
                /*  var isCorrect = true
                  parentLay.children.forEach { cv->
                      val bar = (cv as CustomComponent).findViewById<RangeSlider>(R.id.rangeBar)
                      if (abs(bar.values[0] - bar.values[1])<5) {
                          isCorrect = false
                      }
                  }
                  if (isCorrect){*/
                if (abs(left - right) <= 5 && left != right) {
                    Log.d("True", "Yes It is")

                } else {
                    var sze = arrayList.size
                    if (left == right) {
                        if (arrayList.size > 1) {
                            arrayList[1].start = 0
                            arrayList.removeAt(0)
                        } else {
                            arrayList[0].start = 0
                            arrayList[0].end = 100
                        }


                    } else {
                        if (end == 100) {

                            if (right - left < 6 || end - right < 6) {
                                setStartText(0)
                                setProgress(0, end)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.min_text3),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } else {

                                val rangeBar1 = RangeBarArray(
                                    right + 1,
                                    end,
                                    defaultColor
                                )
                                arrayList[note].end = right
                                arrayList.add(note + 1, rangeBar1)

                            }
                        } else {

                            var deletionNote = note
                            for (i in 0 until arrayList.size) {
                                if (right >= arrayList[i].start && right <= arrayList[i].end) {

                                    deletionNote = i

                                    break
                                }
                            }
                            if (deletionNote == note) {
                                arrayList[note + 1].start = right + 1
                                arrayList[note].start = 0
                                arrayList[note].end = right
                            } else {
                                if (right == 100) {
                                    arrayList[note].end = 100
                                    while (note != sze - 1) {
                                        arrayList.removeAt(sze - 1)
                                        sze--
                                    }
                                } else {
                                    arrayList[deletionNote].start = right + 1
                                    arrayList[note].end = right

                                    deletionNote--
                                    while (deletionNote > note) {
                                        arrayList.removeAt(deletionNote)
                                        deletionNote--
                                    }
                                    val tempArray = mutableListOf<RangeBarArray>()
                                    var foundat = -1
                                    arrayList.forEachIndexed { i, ra ->

                                        tempArray.add(i, RangeBarArray(ra.start, ra.end, ra.color))
                                        if (abs(ra.start - ra.end) in 1..5) {
                                            foundat = i
                                        }
                                    }
                                    if (foundat != -1) {

                                        tempArray[foundat].start = tempArray[foundat].end - 5
                                        tempArray[foundat - 1].end = tempArray[foundat].start - 1

                                    }
                                    arrayList.clear()
                                    arrayList.addAll(tempArray)
                                }
                            }
                        }
                    }

                }



                callback.onValueChanged(arrayList, null)

            }
        })


        eachRangebarBinding.thicknessSliderCustom.addOnChangeListener { slider, value, fromUser ->
            // Responds to when slider's value is changed
            val right = slider.value.toInt()


            //to change the endtext
//            setStartText(0)
            setEndText(right)

            //to make the delete image  Visible
            if (currentGrabbedComponent == 0) {
                if (right == 0) {
                    eachRangebarBinding.leftImage.visibility = View.VISIBLE
                    eachRangebarBinding.rightImage.visibility = View.VISIBLE
                }
                if (right != 0) {
                    eachRangebarBinding.rightImage.visibility = View.GONE
                    eachRangebarBinding.leftImage.visibility = View.GONE
                }
            }


            val note = 0

            val sze = arrayList.size

            if (currentGrabbedComponent != -1) {
                if (right != endOfThis && note == currentGrabbedComponent) {


                    if (note >= sze - 1) {

                        if (note == sze - 1) {
                            //Live Bar View Logic when new rangebar is to be created
                            Log.d("True", "I am in Note")

                        } else {
                            //There is some error with note
                        }

                    } else {
                        var i = 1

                        if (right != 100) {
                            while (right > arrayList[note + i].end) {
                                val temp = parentLay.getChildAt(note + i)
                                val tempBinding = EachRangebarBinding.bind(temp)
                                tempBinding?.rangeBar?.setValues(
                                    arrayList[note + i].end.toFloat(),
                                    arrayList[note + i].end.toFloat()
                                )
                                tempBinding?.rangeBar?.visibility = View.INVISIBLE

                                i++
                            }
                            var j = sze - 1
                            while (right < arrayList[j].end) {
                                val temp = parentLay.getChildAt(j)
                                val tempBinding = EachRangebarBinding.bind(temp)
                                tempBinding?.rangeBar?.visibility = View.VISIBLE
                                j--
                                if (j == 0) {
                                    break
                                }

                            }
                            var k = sze - 1
                            while (right < arrayList[k].start) {
                                val temp = parentLay.getChildAt(k)
                                val tempBinding = EachRangebarBinding.bind(temp)
                                tempBinding?.rangeBar?.setValues(
                                    arrayList[k].start.toFloat(),
                                    arrayList[k].end.toFloat()
                                )
                                k--
                            }
                            val nextComponent = parentLay.getChildAt(note + i)
                            val nextCompBinding = EachRangebarBinding.bind(nextComponent)

                            val startOfNext = right.toFloat()
                            val endOfNext =
                                (arrayList[note + i].end).toFloat()
                            nextCompBinding?.rangeBar?.setValues(startOfNext, endOfNext)
                        } else {
                            val temp = parentLay.getChildAt(sze - 1)
                            val tempBinding = EachRangebarBinding.bind(temp)
                            tempBinding.rangeBar.setValues(right.toFloat(), right.toFloat())
                            tempBinding.rangeBar.visibility = View.INVISIBLE


                        }

                    }
                }
            }

            val tempArr = mutableListOf<RangeBarArray>()
            arrayList.forEach {
                tempArr.add(RangeBarArray(it.start, it.end, it.color))
            }
            var sze2 = tempArr.size
            var note2 = 0

            val end = tempArr[note2].end
            val left = 0


            if (note2 != -1) {
                if (left == right) {
                    if (tempArr.size > 1) {
                        tempArr[1].start = 0
                        tempArr.removeAt(0)
                    } else {
                        tempArr[0].start = 0
                        tempArr[0].end = 100
                    }

                    if (note == 0) {
                        //If it is first  then delete it
                        if (tempArr.size > 1) {
                            tempArr[1].start = 0
                        } else {
                            tempArr[0].start = 0
                        }

                        tempArr.removeAt(0)
                    } else {
                        //if it is not first then delete current one only
                        //*  *This will never execute

                        //*
                        tempArr[note - 1].end = endOfThis
                        tempArr.removeAt(note)
                    }

                } else {
                    if (end == 100) {

                        if (right - left < 6 || end - right < 6) {
//                            setStartText(0)
//                            setProgress(0, end)

                        } else {
                            val rangeBar1 = RangeBarArray(
                                right + 1,
                                end,
                                defaultColor
                            )
                            tempArr[note2].end = right
                            tempArr.add(note2 + 1, rangeBar1)

                        }
                    } else {
                        var deletionNote = note2
                        for (i in 0 until tempArr.size) {
                            if (right >= tempArr[i].start && right <= tempArr[i].end) {

                                deletionNote = i

                                break
                            }
                        }
                        if (deletionNote == note2) {
                            tempArr[note2 + 1].start = right + 1
                            tempArr[note2].start = 0
                            tempArr[note2].end = right
                        } else {
                            if (right == 100) {
                                tempArr[note2].end = 100
                                while (note2 != sze2 - 1) {
                                    tempArr.removeAt(sze2 - 1)
                                    sze2--
                                }
                            } else {
                                tempArr[deletionNote].start = right + 1
                                tempArr[note2].end = right

                                deletionNote--
                                while (deletionNote > note2) {
                                    tempArr.removeAt(deletionNote)
                                    deletionNote--
                                }
                            }
                        }
                    }
                }
                callback.onSliderChange(tempArr)
            }
        }
    }


    fun getStartText(): Int {
        return Integer.parseInt(eachRangebarBinding.startTxt!!.text.toString())

    }

    fun setStartText(value: Int) {
        eachRangebarBinding.startTxt!!.text = value.toString()
    }

    fun getEndText(): Int {
        return Integer.parseInt(eachRangebarBinding.endTxt!!.text.toString())
    }

    fun setEndText(value: Int) {
        eachRangebarBinding.endTxt!!.text = value.toString()
    }

    fun setProgress(left: Int, right: Int) {
        eachRangebarBinding.rangeBar.valueFrom = 0f
        eachRangebarBinding.rangeBar.valueTo = 100f

        eachRangebarBinding.rangeBar.setValues(left.toFloat(), right.toFloat())
        eachRangebarBinding.thicknessSliderCustom.value = right.toFloat()
    }

    var currentColor = 0

    fun setColor(color: Int) {
        val clr = color
        currentColor = color
        ImageViewCompat.setImageTintList(
            eachRangebarBinding.colorPickerBtn,
            ColorStateList.valueOf(clr)
        )


        val states = arrayOf(
            intArrayOf(android.R.attr.state_enabled)
        )

        val colors = intArrayOf(
            clr
        )

        val myList = ColorStateList(states, colors)
        eachRangebarBinding.rangeBar.trackActiveTintList = myList
        eachRangebarBinding.rangeBar.thumbTintList = myList

        eachRangebarBinding.thicknessSliderCustom.trackActiveTintList = myList
        eachRangebarBinding.thicknessSliderCustom.thumbTintList = myList


    }


}


/* if (note >= sze - 1) {
               //Do Nothing

               *//*if (note == sze - 1) {
                            //Live Bar View Logic when new rangebar is to be created
//                            liveBarParentLayout.removeAllViews()
                            val barView1 = LinearLayout(context)
                            val barView2 = LinearLayout(context)
                            val rangebarView = parentLay.getChildAt(0)
                            val rangebarBinding = EachRangebarBinding.bind(rangebarView)
                            val barViewLp1 = LinearLayout.LayoutParams(
                                0,
                                ActionBar.LayoutParams.MATCH_PARENT,
                                rangebarBinding.thicknessSliderCustom.value
                            )
                            val barViewLp2 = LinearLayout.LayoutParams(
                                0,
                                ActionBar.LayoutParams.MATCH_PARENT,
                                100 - rangebarBinding.thicknessSliderCustom.value
                            )
                            barView1.orientation = LinearLayout.HORIZONTAL
                            barView2.orientation = LinearLayout.HORIZONTAL

                            barView1.setBackgroundColor(arrayList[0].color)
                            barView2.setBackgroundColor(Color.parseColor("#2D9EDEFA"))

                            barView1.layoutParams = barViewLp1
                            barView2.layoutParams = barViewLp2
//                            liveBarParentLayout.addView(barView1)
//                            liveBarParentLayout.addView(barView2)

                        } else {
                            //There is some error with note
                        }*//*

                    } else {


                        *//*tempArray[currentGrabbedComponent].start = 0
                        tempArray[currentGrabbedComponent].end = slider.value.toInt()
                        callback.onSliderChange(tempArray)*//*

                    }*/


/*  if (fromUser && currentGrabbedComponent != -1) {
      val tempArr = mutableListOf<RangeBarArray>()
      arrayList.forEach {
          tempArr.add(RangeBarArray(it.start, it.end, it.color))
      }

      tempArr[currentGrabbedComponent].start = 0
      tempArr[currentGrabbedComponent].end = slider.value.toInt()
      arrayList.forEach {
          Log.d("ChangedFrom", "${it.start} ${it.end}")

      }
      tempArr.forEach {
          Log.d("ChangedTo", "${it.start} ${it.end}")

      }
      callback.onSliderChange(tempArr)
  }*/