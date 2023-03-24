package com.naman.segmentationsampleapp.db.app_db

import android.content.Context
import android.graphics.Color
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naman.segmentationsampleapp.db.model.ColorHistory
import com.naman.segmentationsampleapp.db.model.GridData
import com.naman.segmentationsampleapp.db.model.RangeBarDto
import com.naman.segmentationsampleapp.db.model.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Database(entities = [RangeBarDto::class, GridData::class, ColorHistory::class, Settings::class], version = 1)
abstract class AppDb: RoomDatabase() {
    abstract val mainDao: MainDao
    companion object {
        @Volatile
        var dummyDatabaseInstance: AppDb? = null
        fun getInstance(context: Context): AppDb {
            synchronized(this) {
                if (dummyDatabaseInstance == null) {
                    dummyDatabaseInstance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDb::class.java,
                        "dummy_db"
                    )
                        .addCallback(
                            object :Callback(){
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    Executors.newSingleThreadScheduledExecutor().execute(object :Runnable{
                                        override fun run() {

                                            GlobalScope.launch(Dispatchers.IO){
                                                dummyDatabaseInstance!!.mainDao.insertAllRangeBars(
                                                    listOf<RangeBarDto>(
                                                        RangeBarDto(
                                                            1,
                                                            0,
                                                            33,
                                                            Color.parseColor("#e74c3c")
                                                        ), RangeBarDto(
                                                            2,
                                                            34,
                                                            64,
                                                            Color.parseColor("#f1c40f")
                                                        ), RangeBarDto(
                                                            3,
                                                            65,
                                                            85,
                                                            Color.parseColor("#3498db")
                                                        ), RangeBarDto(
                                                            4,
                                                            86,
                                                            100,
                                                            Color.parseColor("#2ecc71")
                                                        )
                                                    )
                                                )
                                                dummyDatabaseInstance!!.mainDao.insertAllGrid(
                                                    listOf<GridData>(
                                                        GridData(
                                                            0,
                                                            1,
                                                            Color.parseColor("#1FA2FF")
                                                        ),
                                                        GridData(
                                                            0,
                                                            2,
                                                            Color.parseColor("#12D8FA")
                                                        ),
                                                        GridData(
                                                            0,
                                                            3,
                                                            Color.parseColor("#A6FFCB")
                                                        )
                                                    )
                                                )
                                                dummyDatabaseInstance!!.mainDao.insertSettings(
                                                    Settings(
                                                        0,
                                                        0
                                                    )
                                                )
                                            }

                                        }
                                    })

                                }
                            }
                        )
                        .build()
                }
                return dummyDatabaseInstance as AppDb
            }
        }
    }
}