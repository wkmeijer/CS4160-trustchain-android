package nl.tudelft.cs4160.trustchain_android.passport.ocr.camera;/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.passport.ocr.util.ErrorDialog;

import static nl.tudelft.cs4160.trustchain_android.passport.ocr.camera.CameraFragment.GET_DOC_INFO;

public class CameraActivity extends Activity {
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_WRITE_PERMISSIONS = 3;
    private static final String TAG = "CameraActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraFragment.newInstance())
                    .commit();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
    }

    /**
     * Pass data from ManualInputActivity to the MainActivity.
     * @param requestCode requestCode
     * @param resultCode resultCode
     * @param data The data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_DOC_INFO && resultCode == RESULT_OK) {
            setResult(Activity.RESULT_OK, data);
            //Clear this activity
            finish();
        }
    }

    public void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
//            showInfoDialog(R.string.ocr_camera_permission_explanation);
            Log.e(TAG, "infodialog");

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    public void requestStoragePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            showInfoDialog(R.string.storage_permission_explanation);
            Log.e(TAG, "infodialog");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSIONS);
        }
    }

//    public void showInfoDialog(int stringId) {
//        ErrorDialog.newInstance(getString(stringId)).sh
//    }
}
