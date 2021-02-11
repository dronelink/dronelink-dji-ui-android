package com.dronelink.dji.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dronelink.core.ui.widget.Widget
import com.dronelink.dji.ui.R.layout
import dji.ux.widget.FPVWidget

class DJICameraFeedWidget : Widget() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(layout.widget_camera_feed, container, false)
        val fpvWidget = view.findViewById<FPVWidget>(R.id.feed)
        fpvWidget.videoSource = FPVWidget.VideoSource.PRIMARY
        fpvWidget.setSourceCameraNameVisibility(false)
        return view
    }
}