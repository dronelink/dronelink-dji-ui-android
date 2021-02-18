package com.dronelink.dji.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dronelink.core.ui.widget.Widget

class DJICameraSettingsExposureWidget : Widget() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.widget_camera_settings_exposure, container, false)
    }
}