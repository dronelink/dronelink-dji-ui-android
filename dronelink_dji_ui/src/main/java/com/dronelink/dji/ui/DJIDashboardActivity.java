//  DJIDashboardActivity.java
//  DronelinkDJIUI
//
//  Created by Jim McAndrew on 11/7/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.dronelink.core.CameraFile;
import com.dronelink.core.DatedValue;
import com.dronelink.core.DroneSession;
import com.dronelink.core.DroneSessionManager;
import com.dronelink.core.Dronelink;
import com.dronelink.core.FuncExecutor;
import com.dronelink.core.MissionExecutor;
import com.dronelink.core.adapters.CameraStateAdapter;
import com.dronelink.core.command.CommandError;
import com.dronelink.core.mission.command.Command;
import com.dronelink.core.mission.core.Message;

import dji.ux.widget.FPVWidget;

public class DJIDashboardActivity extends AppCompatActivity implements Dronelink.Listener, DroneSessionManager.Listener, DroneSession.Listener, MissionExecutor.Listener {
    private DroneSession session;
    private MissionExecutor missionExecutor;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dji_dashboard);

        final FPVWidget fpv = findViewById(R.id.fpv);
        fpv.setSourceCameraNameVisibility(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dronelink.getInstance().addListener(this);
        Dronelink.getInstance().getSessionManager().addListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Dronelink.getInstance().removeListener(this);
        Dronelink.getInstance().getSessionManager().removeListener(this);
        if (session != null) {
            session.removeListener(this);
        }

        if (missionExecutor != null) {
            missionExecutor.removeListener(this);
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

    @Override
    public void onRegistered(final String error) {}

    @Override
    public void onMissionLoaded(final MissionExecutor executor) {
        executor.addListener(this);
        this.missionExecutor = executor;
    }

    @Override
    public void onMissionUnloaded(final MissionExecutor executor) {
        executor.removeListener(this);
        this.missionExecutor = null;
    }

    @Override
    public void onFuncLoaded(final FuncExecutor executor) {}

    @Override
    public void onFuncUnloaded(final FuncExecutor executor) {}

    @Override
    public void onOpened(final DroneSession session) {
        session.addListener(this);
        this.session = session;
    }

    @Override
    public void onClosed(final DroneSession session) {
        session.removeListener(this);
        this.session = null;
    }

    @Override
    public void onInitialized(final DroneSession session) {
        final DJIDashboardActivity self = this;
        final DatedValue<CameraStateAdapter> cameraState = session.getCameraState(0);
        if (cameraState != null) {
            if (!cameraState.value.isSDCardInserted()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(self);
                        alertDialog.setTitle(getResources().getString(R.string.DJIDashboardActivity_camera_noSDCard_title));
                        alertDialog.setMessage(getResources().getString(R.string.DJIDashboardActivity_camera_noSDCard_details));
                        alertDialog.setPositiveButton(getString(R.string.DJIDashboardActivity_dismiss), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface d, int i) {
                                d.dismiss();
                            }
                        });
                        alertDialog.show();
                    }
                });
            }
        }
    }

    @Override
    public void onLocated(final DroneSession session) {}

    @Override
    public void onMotorsChanged(final DroneSession session, final boolean value) {}

    @Override
    public void onCommandExecuted(final DroneSession session, final Command command) {}

    @Override
    public void onCommandFinished(final DroneSession session, final Command command, final CommandError error) {}

    @Override
    public void onCameraFileGenerated(final DroneSession session, final CameraFile file) {}

    @Override
    public void onMissionEstimating(final MissionExecutor executor) {}

    @Override
    public void onMissionEstimated(final MissionExecutor executor, final MissionExecutor.Estimate estimate) {}

    @Override
    public void onMissionEngaging(final MissionExecutor executor) {}

    @Override
    public void onMissionEngaged(final MissionExecutor executor, final MissionExecutor.Engagement engagement) {}

    @Override
    public void onMissionExecuted(final MissionExecutor executor, final MissionExecutor.Engagement engagement) {}

    @Override
    public void onMissionDisengaged(final MissionExecutor executor, final MissionExecutor.Engagement engagement, final Message reason) {}
}