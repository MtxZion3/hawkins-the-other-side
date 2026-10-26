package com.example.hawkins.platform

import android.app.Application
import com.pico.spatial.ui.foundation.dsl.launch
import com.example.hawkins.mainApp

class SpatialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        launch(::mainApp)
    }
}
