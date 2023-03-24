package com.naman.segmentationsampleapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import com.naman.segmentation_module.ColorStyleFragment
import com.naman.segmentation_module.model.RangeBarArray
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
        myFragment = ColorStyleFragment()
        /**
         * For Bar Preview*/

        myFragment?.setColorStyle {
            colorStyle
        }
        myFragment?.setOnColorStyleChange {
            mainActivityViewModel?.updateColorStyle(it)
        }
        myFragment?.setTempArray {
            tempArrayRangeBar
        }
        /**
         * For ColorHistory
         * */
        myFragment?.setColorHistory {
            listColorHistory
        }
        /**For Segments and Gradient*/
        myFragment?.setArrayListProvider {
            arrayRangeBar
        }

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
        myFragment?.setOnSliderChange {
            mainActivityViewModel?.updateArrayState(it,"SliderChange")
        }
        /**
         * For Gradient*/
        myFragment?.setGridData {
            listGrid?.map {
                com.naman.segmentation_module.model.GridData(it.id,it.seqNumber,it.gridColor)
            }
        }
        myFragment?.setOnGridColorChange { strings, newColor ->
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
        myFragment?.setOnGridColorDeleted {
            mainActivityViewModel?.clearGridData()
            val gridDataList = ArrayList<GridData>()
            for (i in it.indices) {
                gridDataList.add(GridData(0, i + 1, Color.parseColor(it[i])))
            }
            mainActivityViewModel?.insertAllGridColor(gridDataList)
        }
        myFragment?.setOnGridChangeProvider {
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
                myFragment?.loadBarViewFragment()
                myFragment?.loadColorStyleInputFragment()

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