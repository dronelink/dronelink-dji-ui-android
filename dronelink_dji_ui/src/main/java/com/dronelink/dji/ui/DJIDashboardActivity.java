//  DJIDashboardActivity.java
//  DronelinkDJIUI
//
//  Created by Jim McAndrew on 11/7/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.ui;

import android.location.Location;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;

import com.dronelink.core.Convert;
import com.dronelink.core.DroneSession;
import com.dronelink.core.DroneSessionManager;
import com.dronelink.core.Dronelink;
import com.dronelink.core.MissionExecutor;
import com.dronelink.core.adapters.DroneStateAdapter;
import com.dronelink.core.mission.core.enums.UnitSystem;

import java.util.Timer;
import java.util.TimerTask;

import dji.ux.widget.FPVWidget;

public class DJIDashboardActivity extends AppCompatActivity implements DroneSessionManager.Listener {
    private DroneSession session;
    private final long updateMillis = 500;
    private Timer updateTimer;
    private final String telemetryLargeFormat = "%s %.0f %s";
    private final String telemetrySmallFormat = "%s %.1f %s";
    private String distancePrefix;
    private String distanceSuffix;
    private String altitudePrefix;
    private String altitudeSuffix;
    private String horizontalSpeedPrefix;
    private String horizontalSpeedSuffix;
    private String verticalSpeedPrefix;
    private String verticalSpeedSuffix;

    private DroneStateAdapter getDroneState() {
        if (session == null || session.getState() == null) {
            return null;
        }

        return session.getState().value;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        distancePrefix = getString(R.string.Dashboard_distance_prefix);
        distanceSuffix = getString(Dronelink.UNIT_SYSTEM == UnitSystem.IMPERIAL ? R.string.Dashboard_distance_suffix_imperial : R.string.Dashboard_distance_suffix_metric);
        altitudePrefix = getString(R.string.Dashboard_altitude_prefix);
        altitudeSuffix = getString(Dronelink.UNIT_SYSTEM == UnitSystem.IMPERIAL ? R.string.Dashboard_distance_suffix_imperial : R.string.Dashboard_distance_suffix_metric);
        horizontalSpeedPrefix = getString(R.string.Dashboard_horizontalSpeed_prefix);
        horizontalSpeedSuffix = getString(Dronelink.UNIT_SYSTEM == UnitSystem.IMPERIAL ? R.string.Dashboard_horizontalSpeed_suffix_imperial : R.string.Dashboard_horizontalSpeed_suffix_metric);
        verticalSpeedPrefix = getString(R.string.Dashboard_verticalSpeed_prefix);
        verticalSpeedSuffix = getString(Dronelink.UNIT_SYSTEM == UnitSystem.IMPERIAL ? R.string.Dashboard_verticalSpeed_suffix_imperial : R.string.Dashboard_verticalSpeed_suffix_metric);

        setContentView(R.layout.activity_dji_dashboard);

        final FPVWidget fpv = findViewById(R.id.fpv);
        fpv.setSourceCameraNameVisibility(false);

        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTimer();
            }
        }, 0, updateMillis);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dronelink.getInstance().getSessionManager().addListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Dronelink.getInstance().getSessionManager().removeListener(this);
    }

    private void updateTimer() {
        runOnUiThread(update);
    }

    private Runnable update = new Runnable() {
        public void run() {
            double distance = 0.0;
            double altitude = 0.0;
            double horizontalSpeed = 0.0;
            double verticalSpeed = 0.0;

            final DroneStateAdapter state = getDroneState();
            if (state != null) {
                final Location droneLocation = state.getLocation();
                if (droneLocation != null) {
                    final Location homeLocation = state.getHomeLocation();
                    if (homeLocation != null) {
                        distance = droneLocation.distanceTo(homeLocation);
                        distance = Double.isNaN(distance) ? 0 : distance;
                    }
                }

                altitude = state.getAltitude();
                horizontalSpeed = state.getHorizontalSpeed();
                verticalSpeed = state.getVerticalSpeed();
            }

            switch (Dronelink.UNIT_SYSTEM) {
                case IMPERIAL:
                    distance = Convert.MetersToFeet(distance);
                    altitude = Convert.MetersToFeet(altitude);
                    horizontalSpeed = Convert.MetersPerSecondToMilesPerHour(horizontalSpeed);
                    verticalSpeed = Convert.MetersToFeet(verticalSpeed);
                    break;

                case METRIC:
                    horizontalSpeed = Convert.MetersPerSecondToKilometersPerHour(horizontalSpeed);
                    break;
            }

            final TextView distanceTextView = findViewById(R.id.distanceTextView);
            distanceTextView.setText(String.format(distance > 10 ? telemetryLargeFormat : telemetrySmallFormat, distancePrefix, distance, distanceSuffix));

            final TextView altitudeTextView = findViewById(R.id.altitudeTextView);
            altitudeTextView.setText(String.format(altitude > 10 ? telemetryLargeFormat : telemetrySmallFormat, altitudePrefix, altitude, altitudeSuffix));

            final TextView horizontalSpeedTextView = findViewById(R.id.horizontalSpeedTextView);
            horizontalSpeedTextView.setText(String.format(horizontalSpeed > 10 ? telemetryLargeFormat : telemetrySmallFormat, horizontalSpeedPrefix, horizontalSpeed, horizontalSpeedSuffix));

            final TextView verticalSpeedTextView = findViewById(R.id.verticalSpeedTextView);
            verticalSpeedTextView.setText(String.format(verticalSpeed > 10 ? telemetryLargeFormat : telemetrySmallFormat, verticalSpeedPrefix, verticalSpeed, verticalSpeedSuffix));
        }
    };



    @Override
    public void onOpened(final DroneSession session) {
        this.session = session;
    }

    @Override
    public void onClosed(final DroneSession session) {
        this.session = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        final MissionExecutor missionExecutor = Dronelink.getInstance().getMissionExecutor();
        if (missionExecutor != null && missionExecutor.isEngaged()) {
            return;
        }

        Dronelink.getInstance().unloadMission();
        super.onBackPressed();
    }

    public void onDismiss(final View view) {
        onBackPressed();
    }
}