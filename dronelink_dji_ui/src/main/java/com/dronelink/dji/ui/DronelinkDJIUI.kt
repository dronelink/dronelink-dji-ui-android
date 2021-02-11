package com.dronelink.dji.ui

import android.content.Context
import com.dronelink.core.DroneSession
import com.dronelink.core.ui.widget.UpdatableWidget
import com.dronelink.core.ui.widget.Widget
import com.dronelink.core.ui.widget.WidgetFactory
import com.dronelink.core.ui.widget.WidgetFactoryProvider
import com.dronelink.dji.DJIDroneSessionManager


class DJIUIDroneSessionManager(val context: Context): DJIDroneSessionManager(context), WidgetFactoryProvider {
    override val widgetFactory: WidgetFactory
        get() = DJIWidgetFactory(context, session)
}

open class DJIWidgetFactory(val context: Context, session: DroneSession?): WidgetFactory(session) {
    override fun createCameraFeedWidget(primary: Boolean): Widget {
        return DJICameraFeedWidget()
    }
}