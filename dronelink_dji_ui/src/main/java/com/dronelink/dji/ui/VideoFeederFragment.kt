package com.dronelink.dji.ui

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.dronelink.core.ui.widget.Widget
import dji.common.camera.SettingsDefinitions
import dji.ux.widget.FPVWidget

class VideoFeederFragment: Widget() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val frameLayout = FrameLayout(requireContext())
        frameLayout.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        val fpvWidget = FPVWidget(requireContext())
        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layoutParams.gravity = Gravity.CENTER
        fpvWidget.layoutParams = layoutParams

        frameLayout.addView(fpvWidget)
        return frameLayout
    }

}