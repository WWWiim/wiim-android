/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by/4.0/
 */
package br.com.joseafga.wiim;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    private static final Pattern QRPATTERN = Pattern.compile("^(process|tag):([0-9]+)$");
    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout view
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // scanner init
        mScannerView = new ZXingScannerView(this);
        ConstraintLayout lo = findViewById(R.id.scanner_view);
        lo.addView(mScannerView);
        mScannerView.setAutoFocus(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission()) {
                requestPermission();
            }
        }
    }

    /**
     * Inflate main
     * add items to action bar
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.ac_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);

                break;
            // ?more options
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Floating action button on click event
     * Turn flash light on/off
     *
     * @param view
     */
    public void flashToggleOnClick(View view) {
        FloatingActionButton flashToggle = findViewById(R.id.flash_toggle);

        if (mScannerView.getFlash()) {
            // turn off
            mScannerView.setFlash(false);
            flashToggle.setImageResource(R.drawable.ic_flash_on_white_24dp);
        } else {
            // turn on
            mScannerView.setFlash(true);
            flashToggle.setImageResource(R.drawable.ic_flash_off_white_24dp);
        }
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if (mScannerView == null) {
                    mScannerView = new ZXingScannerView(this);
                    setContentView(mScannerView);
                }
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

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (!cameraAccepted) {
                        Toast.makeText(getApplicationContext(), R.string.permission_denied, Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setMessage("É necessário conceder as permissões de acesso")
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            @RequiresApi(api = Build.VERSION_CODES.M)
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, null)
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
        Matcher matcher = QRPATTERN.matcher(result.getText());

        if (matcher.matches()) {
            // go to result activity
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("scanned", new String[]{matcher.group(1), matcher.group(2)});
            startActivity(intent);
        } else {
            // dialog with result
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.error)
                    .setMessage(getString(R.string.qrcode_error) + result.getText())
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mScannerView.resumeCameraPreview(MainActivity.this);
                        }
                    })
                    .create()
                    .show();
        }
    }
}
