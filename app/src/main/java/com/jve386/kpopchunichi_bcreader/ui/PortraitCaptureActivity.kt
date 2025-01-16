package com.jve386.kpopchunichi_bcreader.ui

import com.journeyapps.barcodescanner.CaptureActivity
import android.content.pm.ActivityInfo
import android.os.Bundle

class PortraitCaptureActivity : CaptureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}
