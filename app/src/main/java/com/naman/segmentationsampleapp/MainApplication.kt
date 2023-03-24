package com.naman.segmentationsampleapp

import android.app.Application
import com.google.android.material.color.DynamicColors

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}