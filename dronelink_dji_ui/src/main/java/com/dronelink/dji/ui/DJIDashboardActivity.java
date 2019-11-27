//  DJIDashboardActivity.java
//  DronelinkDJIUI
//
//  Created by Jim McAndrew on 11/7/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.dronelink.core.Dronelink;
import com.dronelink.core.MissionExecutor;

import dji.ux.widget.FPVWidget;

public class DJIDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dji_dashboard);

        final FPVWidget fpv = findViewById(R.id.fpv);
        fpv.setSourceCameraNameVisibility(false);
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