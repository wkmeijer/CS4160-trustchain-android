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


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.passport.nfc.PassportConActivity;

import static nl.tudelft.cs4160.trustchain_android.passport.ocr.camera.CameraFragment.GET_DOC_INFO;
import static nl.tudelft.cs4160.trustchain_android.passport.ocr.camera.CameraFragment.REQUEST_WRITE_CAMERA_PERMISSIONS;

public class CameraActivity extends Activity {
    private static final String TAG = "CameraActivity";
    private static final String FRAGMENT_TAG = "cameraFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraFragment.newInstance(), FRAGMENT_TAG)
                    .commit();
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
            Intent i = new Intent(this, PassportConActivity.class);
            startActivity(i);
        }
    }

    /**
     * Hack: receives permission callback from fragment if API level < 23.
     * If called, manually delegates the call back to CameraFragment
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
       if (requestCode == REQUEST_WRITE_CAMERA_PERMISSIONS) {
           ((CameraFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG))
                   .onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
