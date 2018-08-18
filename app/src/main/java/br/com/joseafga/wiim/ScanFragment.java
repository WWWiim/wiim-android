package br.com.joseafga.wiim;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
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

import com.google.zxing.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScanFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    private static final Pattern QR_PATTERN = Pattern.compile("^(process|tag):([0-9]+)$");
    protected ImageButton flashToggle;
    protected ZXingScannerView mScannerView;

    public ScanFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        // scanner init
        mScannerView = new ZXingScannerView(getActivity());
        mScannerView.setAutoFocus(true);
        // layout widget
        ConstraintLayout lo = view.findViewById(R.id.scanner_view);
        lo.addView(mScannerView, 0);

        // fade animation on text after scanner start
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setStartOffset(3000); // hold time to start animation
        fadeOut.setDuration(2000); // milliseconds
        fadeOut.setFillAfter(true);
        view.findViewById(R.id.splash_text).startAnimation(fadeOut);

        // floating action button on click event (flash on/off)
        flashToggle = view.findViewById(R.id.flash_toggle);
        flashToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScannerView.getFlash()) {
                    // turn off
                    mScannerView.setFlash(false);
                    flashToggle.setImageResource(R.drawable.ic_flash_off_white_24dp);
                } else {
                    // turn on
                    mScannerView.setFlash(true);
                    flashToggle.setImageResource(R.drawable.ic_flash_on_white_24dp);
                }
            }
        });

        // check SDK version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission()) {
                requestPermission();
            }
        }

        return view;
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                mScannerView.setResultHandler(this);
                mScannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mScannerView.stopCamera();
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

    @Override
    public void handleResult(Result result) {
        // logging
        Log.d("QRCodeScanner", result.getBarcodeFormat().toString());
        // regex matches
        Matcher matcher = QR_PATTERN.matcher(result.getText());

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
                            mScannerView.resumeCameraPreview(ScanFragment.this);
                        }
                    })
                    .create()
                    .show();
        }
    }
}
