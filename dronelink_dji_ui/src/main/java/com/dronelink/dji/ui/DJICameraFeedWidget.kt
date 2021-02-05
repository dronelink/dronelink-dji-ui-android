package com.dronelink.dji.ui

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.dronelink.core.ui.widget.Widget
import dji.ux.widget.FPVWidget

class DJICameraFeedWidget: Widget() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val frameLayout = FrameLayout(requireContext())
        frameLayout.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        //FIXME does not reliably show the video feed when loaded like this?
        val fpvWidget = FPVWidget(requireContext())
        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layoutParams.gravity = Gravity.CENTER
        //FIXME fpvWidget.layoutParams = layoutParams
        //FIXME fpvWidget.videoSource = FPVWidget.VideoSource.PRIMARY
        //FIXME fpvWidget.setSourceCameraNameVisibility(false)

        //FIXME frameLayout.addView(fpvWidget)

        return frameLayout
    }
}