//  DJIDashboardActivity.java
//  DronelinkDJIUI
//
//  Created by Jim McAndrew on 11/7/19.
//  Copyright © 2019 Dronelink. All rights reserved.
//
package com.dronelink.dji.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.dronelink.core.CameraFile;
import com.dronelink.core.DatedValue;
import com.dronelink.core.DroneSession;
import com.dronelink.core.DroneSessionManager;
import com.dronelink.core.Dronelink;
import com.dronelink.core.FuncExecutor;
import com.dronelink.core.MissionExecutor;
import com.dronelink.core.ModeExecutor;
import com.dronelink.core.adapters.CameraStateAdapter;
import com.dronelink.core.command.CommandError;
import com.dronelink.core.kernel.command.Command;
import com.dronelink.core.kernel.core.CameraFocusCalibration;
import com.dronelink.core.kernel.core.Message;
import com.dronelink.core.kernel.core.UserInterfaceSettings;
import com.dronelink.core.ui.DroneOffsetsFragment;
import com.dronelink.core.ui.MapboxMapFragment;
import com.dronelink.core.ui.MicrosoftMapFragment;
import com.squareup.picasso.Picasso;

import dji.ux.widget.FPVWidget;

public class DJIDashboardActivity extends AppCompatActivity implements Dronelink.Listener, DroneSessionManager.Listener, DroneSession.Listener, MissionExecutor.Listener {
    private static final String TAG = DJIDashboardActivity.class.getCanonicalName();

    private DroneSession session;
    private MissionExecutor missionExecutor;
    private boolean videoPreviewerPrimary = true;
    private ImageView reticleImageView;
    private boolean offsetsVisible = false;
    private ImageButton offsetsButton;
    private boolean offsetsButtonEnabled = false;
    private Fragment droneOffsetsFragment0;
    private Fragment droneOffsetsFragment1;
    private Fragment cameraOffsetsFragment;
    private Fragment telemetryFragment;
    private Fragment debugFragment;
    private boolean debug = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.black));
        }

        setContentView(R.layout.activity_dji_dashboard);

        final FPVWidget fpv = findViewById(R.id.fpv);
        fpv.setSourceCameraNameVisibility(false);

        reticleImageView = findViewById(R.id.reticleImageView);

        offsetsButton = findViewById(R.id.offsetsButton);
        droneOffsetsFragment0 = getDroneOffsetsFragment0();
        droneOffsetsFragment1 = getDroneOffsetsFragment1();
        if (droneOffsetsFragment1 == null) {
            ((DroneOffsetsFragment)droneOffsetsFragment0).stylesEnabled = true;
        }
        else {
            ((DroneOffsetsFragment)droneOffsetsFragment0).stylesEnabled = false;
            ((DroneOffsetsFragment)droneOffsetsFragment0).setStyle(DroneOffsetsFragment.Style.POSITION);
            ((DroneOffsetsFragment)droneOffsetsFragment1).stylesEnabled = false;
            ((DroneOffsetsFragment)droneOffsetsFragment1).setStyle(DroneOffsetsFragment.Style.ALT_YAW);
        }
        cameraOffsetsFragment = getCameraOffsetsFragment();
        telemetryFragment = getTelemetryFragment();
        debugFragment = getDebugFragment();

        toggleOffsets(offsetsVisible);

        final ImageButton primaryViewToggleButton = findViewById(com.dronelink.dji.ui.R.id.primaryViewToggleButton);
        primaryViewToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePrimaryView(!videoPreviewerPrimary);
            }
        });

        final DJIDashboardActivity self = this;
        final ImageButton mapMoreButton = findViewById(R.id.mapMoreButton);
        mapMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final Fragment mapFragment = getMapFragment();
                if (mapFragment instanceof MicrosoftMapFragment) {
                    ((MicrosoftMapFragment)mapFragment).onMore(self, findViewById(R.id.mapMoreButton), new MicrosoftMapFragment.MoreMenuItem[] {
                            new MicrosoftMapFragment.MoreMenuItem() {
                                @Override
                                public String getTitle() {
                                    return getResources().getString(R.string.DJIDashboardActivity_map_mapbox);
                                }

                                @Override
                                public void onClick() {
                                    replaceMapFragment(new MapboxMapFragment());
                                }
                            }
                    });
                }
                else if (mapFragment instanceof MapboxMapFragment) {
                    ((MapboxMapFragment)mapFragment).onMore(self, findViewById(R.id.mapMoreButton), new MapboxMapFragment.MoreMenuItem[] {
                            new MapboxMapFragment.MoreMenuItem() {
                                @Override
                                public String getTitle() {
                                    return getResources().getString(R.string.DJIDashboardActivity_map_microsoft);
                                }

                                @Override
                                public void onClick() {
                                    replaceMapFragment(new MicrosoftMapFragment());
                                }
                            }
                    });
                }
            }
        });

        telemetryFragment.getView().setOnClickListener(new View.OnClickListener() {
            long previousDebugMillis = System.currentTimeMillis();
            int consecutiveDebugTaps = 0;

            @Override
            public void onClick(final View view) {
                if ((System.currentTimeMillis() - previousDebugMillis) < 500) {
                    consecutiveDebugTaps++;

                    Log.d(TAG, "Debug taps " + consecutiveDebugTaps);

                    if (consecutiveDebugTaps >= 7) {
                        consecutiveDebugTaps = 0;
                        toggleDebug();
                    }
                }
                else {
                    consecutiveDebugTaps = 1;
                }
                previousDebugMillis = System.currentTimeMillis();
            }
        });
    }

    private void updatePrimaryView(final boolean videoPreviewerPrimary) {
        this.videoPreviewerPrimary = videoPreviewerPrimary;

        final View videoPreviewer = findViewById(R.id.fpvContainer);
        ((ViewGroup)videoPreviewer.getParent()).removeView(videoPreviewer);

        final View mapContainer = findViewById(R.id.mapContainer);
        ((ViewGroup)mapContainer.getParent()).removeView(mapContainer);

        final ViewGroup primary = findViewById(R.id.primaryView);
        final ViewGroup secondary = findViewById(R.id.secondaryView);
        if (videoPreviewerPrimary) {
            primary.addView(videoPreviewer);
            secondary.addView(mapContainer);
        }
        else {
            primary.addView(mapContainer);
            secondary.addView(videoPreviewer);
        }
    }

    private Fragment getDebugFragment() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.debugFragment);
        if (fragment == null) {
            return getSupportFragmentManager().findFragmentByTag("debugFragment");
        }

        return fragment;
    }

    private Fragment getDroneOffsetsFragment0() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.droneOffsetsFragment0);
        if (fragment == null) {
            return getSupportFragmentManager().findFragmentByTag("droneOffsetsFragment0");
        }

        return fragment;
    }

    private Fragment getDroneOffsetsFragment1() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.droneOffsetsFragment1);
        if (fragment == null) {
            return getSupportFragmentManager().findFragmentByTag("droneOffsetsFragment1");
        }

        return fragment;
    }

    private Fragment getCameraOffsetsFragment() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.cameraOffsetsFragment);
        if (fragment == null) {
            return getSupportFragmentManager().findFragmentByTag("cameraOffsetsFragment");
        }

        return fragment;
    }

    private Fragment getTelemetryFragment() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.telemetryFragment);
        if (fragment == null) {
            return getSupportFragmentManager().findFragmentByTag("telemetryFragment");
        }

        return fragment;
    }

    private Fragment getMapFragment() {
        final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (fragment == null) {
            return getSupportFragmentManager().findFragmentByTag("map");
        }

        return fragment;
    }

    private void replaceMapFragment(final Fragment mapFragment) {
        final Fragment currentMapFragment = getMapFragment();
        final ViewGroup mapFragmentContainer = (ViewGroup)currentMapFragment.getView().getParent();
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(getMapFragment());
        fragmentTransaction.add(mapFragmentContainer.getId(), mapFragment, "map");
        fragmentTransaction.commit();
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

        Dronelink.getInstance().unload();
        super.onBackPressed();
    }

    public void onDismiss(final View view) {
        onBackPressed();
    }

    public void onToggleOffsets(final View view) {
        toggleOffsets(!offsetsVisible);
    }

    private void toggleOffsets(final boolean value) {
        offsetsVisible = value;
        if (droneOffsetsFragment0 != null) {
            final View view = droneOffsetsFragment0.getView();
            if (view != null) {
                view.setVisibility(offsetsVisible ? View.VISIBLE : View.INVISIBLE);
            }
        }
        if (droneOffsetsFragment1 != null) {
            final View view = droneOffsetsFragment1.getView();
            if (view != null) {
                view.setVisibility(offsetsVisible ? View.VISIBLE : View.INVISIBLE);
            }
        }

        final View view = cameraOffsetsFragment.getView();
        if (view != null) {
            view.setVisibility(offsetsVisible ? View.VISIBLE : View.INVISIBLE);
        }
        offsetsButton.setImageTintList(ColorStateList.valueOf(offsetsVisible ? Color.parseColor("#f50057") : Color.parseColor("#ffffff")));
        offsetsButton.setVisibility(offsetsButtonEnabled ? View.VISIBLE : View.INVISIBLE);
    }

    private void toggleDebug() {
        debug = !debug;
        final View view = debugFragment.getView();
        if (view != null) {
            view.setVisibility(debug ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void applyUserInterfaceSettings(final UserInterfaceSettings userInterfaceSettings) {
        if (userInterfaceSettings == null) {
            reticleImageView.setVisibility(View.INVISIBLE);
            offsetsButtonEnabled = false;
            toggleOffsets(false);
            return;
        }

        if (userInterfaceSettings.reticleImageUrl != null && !userInterfaceSettings.reticleImageUrl.isEmpty()) {
            Picasso.get().load(userInterfaceSettings.reticleImageUrl).into(reticleImageView);
            reticleImageView.setVisibility(View.VISIBLE);
        }
        else {
            reticleImageView.setVisibility(View.INVISIBLE);
        }

        if (userInterfaceSettings.droneOffsetsVisible != null) {
            offsetsButtonEnabled = userInterfaceSettings.droneOffsetsVisible;
            toggleOffsets(userInterfaceSettings.droneOffsetsVisible);
        }
        else {
            offsetsButtonEnabled = false;
            toggleOffsets(false);
        }
    }

    @Override
    public void onRegistered(final String error) {}

    @Override
    public void onMissionLoaded(final MissionExecutor executor) {
        executor.addListener(this);
        this.missionExecutor = executor;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                applyUserInterfaceSettings(executor.userInterfaceSettings);
            }
        });
    }

    @Override
    public void onMissionUnloaded(final MissionExecutor executor) {
        executor.removeListener(this);
        this.missionExecutor = null;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                applyUserInterfaceSettings(null);
            }
        });
    }

    @Override
    public void onFuncLoaded(final FuncExecutor executor) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                applyUserInterfaceSettings(executor.getUserInterfaceSettings());
            }
        });
    }

    @Override
    public void onFuncUnloaded(final FuncExecutor executor) {
        if (missionExecutor == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    applyUserInterfaceSettings(null);
                }
            });
        }
    }

    @Override
    public void onModeLoaded(final ModeExecutor executor) {}

    @Override
    public void onModeUnloaded(final ModeExecutor executor) {}

    @Override
    public void onCameraFocusCalibrationRequested(final CameraFocusCalibration value) {}

    @Override
    public void onCameraFocusCalibrationUpdated(final CameraFocusCalibration value) {}

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
    public Message[] missionEngageDisallowedReasons(final MissionExecutor executor) {
        return null;
    }

    @Override
    public void onMissionEngaging(final MissionExecutor executor) {}

    @Override
    public void onMissionEngaged(final MissionExecutor executor, final MissionExecutor.Engagement engagement) {}

    @Override
    public void onMissionExecuted(final MissionExecutor executor, final MissionExecutor.Engagement engagement) {}

    @Override
    public void onMissionDisengaged(final MissionExecutor executor, final MissionExecutor.Engagement engagement, final Message reason) {}

    @Override
    public void onMissionUpdatedDisconnected(final MissionExecutor executor, final MissionExecutor.Engagement engagement) {}
}