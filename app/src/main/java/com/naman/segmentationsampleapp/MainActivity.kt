package com.naman.segmentationsampleapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import app.ijp.segmentation_editor.ColorStyleFragment
import app.ijp.segmentation_editor.ColorStyleOption
import app.ijp.segmentation_editor.model.RangeBarArray
import com.naman.segmentationsampleapp.databinding.ActivityMainBinding
import com.naman.segmentationsampleapp.db.app_db.AppDb
import com.naman.segmentationsampleapp.db.app_db.MainRepo
import com.naman.segmentationsampleapp.db.model.ColorHistory
import com.naman.segmentationsampleapp.db.model.GridData
import com.naman.segmentationsampleapp.db.model.RangeBarDto
import kotlinx.coroutines.flow.collectLatest


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private var mainActivityViewModel: MainViewModel? = null
    private var arrayRangeBar = mutableListOf<RangeBarArray>()
    private var tempArrayRangeBar = mutableListOf<RangeBarArray>()

    private var listGrid: List<GridData>? = null
    private var listColorHistory: List<Int>? = null
    private var colorStyle: Int? = null

    private var myFragment: ColorStyleFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /** Main View Model*/
        val mainDao = AppDb.getInstance(applicationContext).mainDao
        val repo = MainRepo(mainDao)
        mainActivityViewModel = MainVMFactory(repo).create(MainViewModel::class.java)


        myFragment = ColorStyleFragment(
            options = listOf(
                ColorStyleOption.Segment,
                ColorStyleOption.MergedSegment,
                ColorStyleOption.Gradient
            )
        )
      /*  myFragment?.let {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.segmentation_container, it)
            transaction.commitNow()
        }*/

        /**
         * For Bar Preview*/
        /**This will called from ColorDialog Instance in both Segment and Gradient Editor Fragment*/
        myFragment?.setColorStyle {
            colorStyle
        }
        /**
         * This will be called on Dropdown Item is selected*/
        myFragment?.setOnColorStyleChange {
            Log.d("COlorstyleIs","$it")
            mainActivityViewModel?.updateColorStyle(it)
        }
        /**
         * It will be called by SegmentLivePreviewFragment to get temporary Array fro live preview*/
        myFragment?.setDataForSegmentsPreview {
            tempArrayRangeBar
        }
        /***
         * For ColorHistory
         * */
        myFragment?.setColorHistory {
            listColorHistory
        }
        /**
         * For Segments and Gradient
         * This will provide them value stored in database for segments Editor*/
        myFragment?.setSegmentsData {
            arrayRangeBar
        }

        /**
         * This funtion will be passed to SegmentRangeSliderComponent then It will further pass it to RangeSlider and will be called there inside rangebar.OnSliderStopped()*/
        myFragment?.setOnSegmentValueChangeListener { rangeBarArrays, newColor ->
            mainActivityViewModel?.clearColorTable()
            val rangeBarDtoList = rangeBarArrays.map { rb ->
                RangeBarDto(
                    id = 0,
                    seekBarStart = rb.start,
                    seekBarEnd = rb.end,
                    segmentColor = rb.color
                )
            }
            mainActivityViewModel?.updateArrayState(rangeBarArrays, from = "OnValueChanged")
            mainActivityViewModel?.updateRangeBars(rangeBarDtoList)
            newColor?.let {
                mainActivityViewModel?.insertColorHistory(
                    ColorHistory(
                        0,
                        it
                    ),
                )
            }

        }

        /**
         * This funtion will be passed to SegmentRangeSliderComponent then It will further pass it to RangeSlider and will be called there inside rangebar.addOnSliderChange()*/
        myFragment?.setOnSliderChange {
            mainActivityViewModel?.updateArrayState(it, "SliderChange")
        }


        /**
         * For Gradient*/
        myFragment?.setDataForGradientPreviewAndEditor {
            listGrid?.map {
                app.ijp.segmentation_editor.model.GridData(it.id, it.seqNumber, it.gridColor)
            }
        }

        /**
         * This is called by whenever color is changed or added from Single ColorDialog*/
        myFragment?.setSingleGradientColorChanged { strings, newColor ->
            mainActivityViewModel?.clearGridData()
            for (i in strings.indices) {
                mainActivityViewModel?.insertGridColor(
                    GridData(
                        0,
                        i + 1,
                        Color.parseColor(strings[i])
                    )
                )
            }
            mainActivityViewModel?.insertColorHistory(
                ColorHistory(
                    0,
                    colorValue = newColor
                )
            )
        }
        myFragment?.setGradientColorChangedOnDeletion {
            mainActivityViewModel?.clearGridData()
            val gridDataList = ArrayList<GridData>()
            for (i in it.indices) {
                gridDataList.add(GridData(0, i + 1, Color.parseColor(it[i])))
            }
            mainActivityViewModel?.insertAllGridColor(gridDataList)
        }
        myFragment?.getAutoMultiColorGradientColors {
            mainActivityViewModel?.clearGridData()
            for (i in it.indices) {
                mainActivityViewModel?.insertGridColor(
                    GridData(
                        0,
                        i + 1,
                        it[i]
                    )
                )
            }
        }
        myFragment?.let {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.segmentation_container, it)
            transaction.commitNow()
        }


        /**
         * View model Observers
         * */

        mainActivityViewModel?.settings?.observe(this) {
            Log.d("Observer4", "$it")
            if (it.isNotEmpty()) {
                colorStyle = it[0].colorStyle
                myFragment?.setTextToDropDown(it[0].colorStyle)
                myFragment?.notifyBarPreview()
                myFragment?.notifyColorStyleInputFragment()

            }

        }
        mainActivityViewModel?.allRangeBars?.observe(this) {
            Log.d("Observer1", "$it")
            if (it.isNotEmpty()) {
                val rangeBarArrayList = it.map { rdto ->
                    RangeBarArray(
                        start = rdto.seekBarStart,
                        end = rdto.seekBarEnd,
                        color = rdto.segmentColor
                    )
                }
                arrayRangeBar = rangeBarArrayList as MutableList<RangeBarArray>
                tempArrayRangeBar = arrayRangeBar
                myFragment?.updateSegmentBars()
                myFragment?.updateSegmentPreview()

            }
        }


        mainActivityViewModel?.gridData?.observe(this) {
            Log.d("Observer2", "$it")
            it.forEach {
                Log.d("Grid Data", "Obs $it")
            }
            listGrid = it
            myFragment?.updateGradientFragment()
            myFragment?.updateGradientPreview()


        }


        mainActivityViewModel?.colorHistory?.observe(this) {
            Log.d("Observer3", "$it")
            if (it.isNotEmpty()) {
                val colorIntList = it.map { ch ->
                    ch.colorValue
                }
                listColorHistory = colorIntList

            }
        }



        lifecycleScope.launchWhenCreated {
            mainActivityViewModel?.tempArrayState?.collectLatest {
                if (it != null) {
                    tempArrayRangeBar = it
                    myFragment?.updateSegmentPreview()
                }

            }
        }
    }
}


