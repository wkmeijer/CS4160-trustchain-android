package nl.tudelft.cs4160.trustchain_android.passport.ocr.camera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import nl.tudelft.cs4160.trustchain_android.R;
import nl.tudelft.cs4160.trustchain_android.passport.DocumentData;
import nl.tudelft.cs4160.trustchain_android.passport.nfc.PassportConActivity;
import nl.tudelft.cs4160.trustchain_android.passport.ocr.ManualInputActivity;
import nl.tudelft.cs4160.trustchain_android.passport.ocr.Mrz;
import nl.tudelft.cs4160.trustchain_android.passport.ocr.TesseractOCR;

public class CameraFragment extends Fragment {
    public static final int GET_DOC_INFO = 1; //TODO check for collisions


    // tag for the log and the error dialog
    private static final String TAG = "CameraFragment";

    private static final int DELAY_BETWEEN_OCR_THREADS_MILLIS = 500;
    private List<TesseractOCR> tesseractThreads = new ArrayList<>();
    private boolean resultFound = false;
    private Runnable scanningTakingLongTimeout = new Runnable() {
        @Override
        public void run() {
            manualInput.post(new Runnable() {
                @Override
                public void run() {
                    manualInput.setVisibility(View.VISIBLE);
                }
            });
            final ViewTreeObserver observer = manualInput.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Set the margins after the manualInputButton became visible.
                    overlay.setMargins(0, 0, 0, controlPanel.getHeight());
                    observer.removeOnGlobalLayoutListener(this);
                }
            });
        }
    };

    private ImageView scanSegment;
    private Overlay overlay;
    private Button manualInput;
    private FloatingActionButton toggleTorchButton;
    private TextView infoText;
    private View controlPanel;

    // Conversion from screen rotation to JPEG orientation.
    public static final int REQUEST_WRITE_CAMERA_PERMISSIONS = 3;

    boolean showingPermissionExplanation = false;

    // listener for detecting orientation changes
    private OrientationEventListener orientationListener = null;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            orientationListener.enable();
            mCameraHandler.openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            orientationListener.disable();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
    };

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    public Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * Handler for the connection with the camera
     */
    private CameraHandler mCameraHandler;


    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    /**
     * Handles the setup that can start when the fragment is created.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orientationListener = new OrientationEventListener(this.getActivity()) {
            public void onOrientationChanged(int orientation) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        };
        int threadsToStart = Runtime.getRuntime().availableProcessors() / 2;
        createOCRThreads(threadsToStart);
        mCameraHandler = new CameraHandler(this);
    }

    /**
     * Create the threads where the OCR will run on.
     * @param amount
     */
    private void createOCRThreads(int amount) {
        for (int i = 0; i < amount; i++) {
            tesseractThreads.add(new TesseractOCR("Thread no " + i, this, getActivity().getAssets()));
        }
        Log.e(TAG, "Running threads: " + amount);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
    }

    /**
     * Setup the layout and setup the actions associated with the button.
     */
    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        scanSegment = (ImageView) view.findViewById(R.id.scan_segment);
        manualInput = (Button) view.findViewById(R.id.manual_input_button);
        overlay = (Overlay) view.findViewById(R.id.overlay);
        manualInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ManualInputActivity.class);
                getActivity().startActivityForResult(intent, GET_DOC_INFO);
            }
        });

        toggleTorchButton = (FloatingActionButton) view.findViewById(R.id.toggle_torch_button);
        toggleTorchButton.setOnClickListener(new View.OnClickListener() {
            @Override
             public void onClick(View v) {
                mCameraHandler.toggleTorch();
            }
         });

        infoText = (TextView) view.findViewById(R.id.info_text);
        controlPanel = view.findViewById(R.id.control);
        final ViewTreeObserver observer = controlPanel.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Set the overlay's margins when the view is available.
                overlay.setMargins(0, 0, 0, controlPanel.getHeight());
                controlPanel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraHandler.startBackgroundThread();
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable() && !showingPermissionExplanation) {
            mCameraHandler.openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        startOCRThreads();
    }

    @Override
    public void onPause() {
        mCameraHandler.closeCamera();
        mCameraHandler.stopBackgroundThread();
        stopTesseractThreads();
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void requestPermissions() {
        if (!showingPermissionExplanation) {
            // If currently showing the rationale, this will be called again after dismissal.
            if (Build.VERSION.SDK_INT >= 23) {
                // If API 23 or up onRequestPermissionsResult is handled by this fragment, otherwise this method will be called in the Activity
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_WRITE_CAMERA_PERMISSIONS);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_WRITE_CAMERA_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_CAMERA_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                switch (permissions[i]) {
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            showingPermissionExplanation = true;
                            new AlertDialog.Builder(getActivity())
                                    .setMessage(getString(R.string.storage_permission_explanation))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            showingPermissionExplanation = false;
                                            requestPermissions();
                                        }
                                    })
                                    .show();
                        }
                        break;
                    case Manifest.permission.CAMERA:
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            showingPermissionExplanation = true;
                            new AlertDialog.Builder(getActivity())
                                    .setMessage(getString(R.string.ocr_camera_permission_explanation))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            showingPermissionExplanation = false;
                                            requestPermissions();
                                        }
                                    })
                                    .show();
                        }
                        break;
                    default:
                        Log.w(TAG, "Callback for unknown permission: " + permissions[i]);
                        break;

                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Start the threads that will run the OCR scanner.
     */
    private void startOCRThreads() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            return;
        }
        int i = 0;
        for(TesseractOCR ocr : tesseractThreads) {
            ocr.initialize();
            ocr.startScanner(i);
            i += DELAY_BETWEEN_OCR_THREADS_MILLIS;
        }
    }



    private void stopTesseractThreads() {
        for (TesseractOCR ocr : tesseractThreads) {
            ocr.stopScanner();
        }
    }

    /**
     * Method for delivering correct MRZ when found. This method returns the MRZ as result data and
     * then exits the activity. This method is synchronized and checks for a boolean to make sure
     * it is only executed once in this fragments lifetime.
     * @param mrz Mrz
     */
    public synchronized void scanResultFound(final Mrz mrz) {
        if (!resultFound) {
            for (TesseractOCR thread : tesseractThreads) {
                thread.stopping = true;
            }
            Intent returnIntent = new Intent(getActivity(), PassportConActivity.class);
            DocumentData data = mrz.getPrettyData();
            returnIntent.putExtra(DocumentData.identifier, data);
            getActivity().setResult(Activity.RESULT_OK, returnIntent);
            resultFound = true;
            startActivity(returnIntent);
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    public void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
        overlay.setRect(CameraFragmentUtil.getScanRect(scanSegment));
    }

    /**
     * Extract a bitmap from the textureview of this fragment.
     * @return
     */
    public Bitmap extractBitmap() {
        try {
            Bitmap bitmap = mTextureView.getBitmap();
            int rotate = Surface.ROTATION_0;
            switch (getActivity().getWindowManager().getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_0:
                    rotate = 0;
                    break;
                case Surface.ROTATION_90:
                    rotate = 270;
                    break;
                case Surface.ROTATION_180:
                    rotate = 180;
                    break;
                case Surface.ROTATION_270:
                    rotate = 90;
                    break;
            }
            if (rotate != Surface.ROTATION_0) {
                bitmap = CameraFragmentUtil.rotateBitmap(bitmap, rotate);
            }
            Bitmap croppedBitmap = CameraFragmentUtil.cropBitmap(bitmap, scanSegment);

            return CameraFragmentUtil.getResizedBitmap(croppedBitmap, croppedBitmap.getWidth(), croppedBitmap.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public Size getPreviewSize() {
        return mPreviewSize;
    }

    public AutoFitTextureView getTextureView() {
        return mTextureView;
    }

    public ImageView getScanSegment() {
        return scanSegment;
    }

    public Runnable getScanningTakingLongTimeout() {
        return scanningTakingLongTimeout;
    }

    public void setPreviewSize(Size size) {
        mPreviewSize = size;
    }

    /**
     * Sets the aspect ratio of the textureview
     * @param width
     * @param height
     */
    public void setAspectRatio(int width, int height) {
        mTextureView.setAspectRatio(width, height);
    }

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    public void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}