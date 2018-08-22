/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by-nc/4.0/
 */

package br.com.joseafga.wiim;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.CAMERA;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScanFragment extends Fragment {

    private static final int REQUEST_CAMERA = 1;
    private static final Pattern QR_PATTERN = Pattern.compile("^(process|tag):([0-9]+)$");
    protected DecoratedBarcodeView mScannerView;
    protected String lastText; // prevent duplicates
    // flash light variables
    protected static boolean flashStatus = false;
    protected ImageButton mFlashToggle;
    // beep and vibrate
    protected BeepManager mBeep;
    protected boolean beepEnabled;

    // handle scanner result callback
    private BarcodeCallback handleScanner = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            // Prevent duplicate scans
            if (result.getText() == null || result.getText().equals(lastText))
                return;
            lastText = result.getText();

            // logging
            Log.d("QRCodeScanner", result.getBarcodeFormat().toString());

            // regex matches
            Matcher matcher = QR_PATTERN.matcher(result.getText());
            // beep sound and vibrate
            if (beepEnabled)
                mBeep.playBeepSoundAndVibrate();

            if (matcher.matches()) {
                // go to result activity
                Intent intent = new Intent(getActivity(), ResultActivity.class);
                intent.putExtra("QRData", new String[]{matcher.group(1), matcher.group(2)});
                startActivity(intent);
            } else {
                // dialog with error
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.error)
                        .setMessage(getString(R.string.qrcode_error) + result.getText())
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mScannerView.resume();
                            }
                        })
                        .create()
                        .show();
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        // scanner init
        mScannerView = view.findViewById(R.id.scanner_view);
        mScannerView.setStatusText(""); // remove bottom text
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE); // read QRCode only
        mScannerView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        // start scanner
        mScannerView.decodeContinuous(handleScanner);
        // beep sound
        mBeep = new BeepManager(getActivity());
        getPreferences();

        // fade animation on text after scanner start
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setStartOffset(3000); // hold time to start animation
        fadeOut.setDuration(2000); // milliseconds
        fadeOut.setFillAfter(true);
        view.findViewById(R.id.splash_text).startAnimation(fadeOut);

        // floating action button on click event (flash on/off)
        mFlashToggle = view.findViewById(R.id.flash_toggle);

        if (hasFlash()) {
            mFlashToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (flashStatus) {
                        // turn off
                        mScannerView.setTorchOff();
                        mFlashToggle.setImageResource(R.drawable.ic_flash_off_white_24dp);
                    } else {
                        // turn on
                        mScannerView.setTorchOn();
                        mFlashToggle.setImageResource(R.drawable.ic_flash_on_white_24dp);
                    }

                    // toggle flash status
                    flashStatus = !flashStatus;
                }
            });
        } else {
            mFlashToggle.setVisibility(View.GONE);
        }

        // check SDK version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission()) {
                requestPermission();
            }
        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // pause scanner if not visible
        if (mScannerView != null) {
            if (isVisibleToUser) {
                mScannerView.resume();
            } else {
                mScannerView.pauseAndWait();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                mScannerView.resume();
                getPreferences(); // update preferences
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.pauseAndWait();

        // clear previous text
        lastText = null;
    }

    /**
     * Check if the device's camera has a Flashlight.
     *
     * @return true if there is Flashlight, otherwise false.
     */
    private boolean hasFlash() {
        return getActivity().getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    /**
     * Get and/or update preferentes
     */
    private void getPreferences(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        beepEnabled = prefs.getBoolean(SettingsActivity.KEY_PREF_QRCODE_BEEP, true);

        if (prefs.getBoolean(SettingsActivity.KEY_PREF_QRCODE_VIBRATE, true))
            mBeep.setVibrateEnabled(true); // set vibrate based on settings
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, REQUEST_CAMERA);
    }

    // TODO: really need this?
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (!cameraAccepted) {
                        Toast.makeText(getContext(), R.string.permission_denied, Toast.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                new AlertDialog.Builder(getContext())
                                        .setMessage(R.string.required_permissions)
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            @RequiresApi(api = Build.VERSION_CODES.M)
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // if click on cancel exit app
                                                getActivity().finish();
                                                System.exit(0);
                                            }
                                        })
                                        .create()
                                        .show();

                                return;
                            }
                        }
                    }
                }
                break;
        }
    }
}
