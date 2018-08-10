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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set layout view
        setContentView(R.layout.activity_main);

        // scanner init
        scannerView = new ZXingScannerView(this);
        ConstraintLayout lo = findViewById(R.id.scanner_view);
        lo.addView(scannerView);
        scannerView.setAutoFocus(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission()) {
                requestPermission();
            }
        }
    }

    /**
     * Inflate toolbar
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);

        //return super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.ac_settings:
                //Toast.makeText(this, R.string.settings, Toast.LENGTH_SHORT).show();
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

        if (scannerView.getFlash()) {
            // turn off
            scannerView.setFlash(false);
            flashToggle.setImageResource(R.drawable.ic_flash_on_white_24dp);
        } else {
            // turn on
            scannerView.setFlash(true);
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
                if (scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
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
        Log.d("QRCodeScanner", result.getText());
        Log.d("QRCodeScanner", result.getBarcodeFormat().toString());

        // dialog with result
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.result)
                .setMessage(result.getText())
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        scannerView.resumeCameraPreview(MainActivity.this);
                    }
                })
                .create()
                .show();
    }
}
