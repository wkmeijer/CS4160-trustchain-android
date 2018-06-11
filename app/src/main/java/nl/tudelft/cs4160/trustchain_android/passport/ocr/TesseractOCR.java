package nl.tudelft.cs4160.trustchain_android.passport.ocr;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;

import nl.tudelft.cs4160.trustchain_android.passport.ocr.camera.CameraFragment;
import nl.tudelft.cs4160.trustchain_android.util.Util;

public class TesseractOCR {
    private static final String TAG = "TesseractOCR";

    private static final long INTER_SCAN_DELAY_MILLIS = 100;
    private static final long OCR_SCAN_TIMEOUT_MILLIS = 1000;

    private static final String trainedData = "ocrb.traineddata";

    private static final String FOLDER_TESSERACT_DATA = "tessdata";
    private static final String FOLDER_TRAINED_DATA = "TesseractData";
    private static final String TRAINED_DATA_EXTENSION = ".traineddata";


    private final String name;

    private TessBaseAPI baseApi;
    private HandlerThread myThread;
    private Handler myHandler;
    private Handler cleanHandler;
    private Handler timeoutHandler;

    private AssetManager assetManager;
    private CameraFragment fragment;
    public boolean stopping = false;
    public boolean isInitialized = false;

    // Filled with OCR run times for analysis
    private ArrayList<Long> times = new ArrayList<>();

    /**
     * Lock to ensure only one thread can start copying to device storage.
     */
    private static Semaphore mDeviceStorageAccessLock = new Semaphore(1);

    /**
     * Timeout Thread, ends OCR detection when timeout occurs
     */
    private Runnable timeout = new Runnable() {
        @Override
        public void run() {
            Log.v(TAG, "Timeout");
            baseApi.stop();
        }
    };


    private Runnable scan = new Runnable() {
        @Override
        public void run() {
            while (!stopping) {
                Log.v(TAG, "Start Scan");
                timeoutHandler.postDelayed(timeout, OCR_SCAN_TIMEOUT_MILLIS);
                long time = System.currentTimeMillis();
                Bitmap b = fragment.extractBitmap();
                Mrz mrz = ocr(b);
                long timetook = System.currentTimeMillis() - time;
                Log.i(TAG, "took " + timetook / 1000f + " sec");
                times.add(timetook);
                if (mrz != null && mrz.valid()) {
                    Log.i(TAG, "Valid MRZ was scanned in " + timetook/1000f + " sec");
                    fragment.scanResultFound(mrz);
                }
                timeoutHandler.removeCallbacks(timeout);
                try {
                    Thread.sleep(INTER_SCAN_DELAY_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Stopping scan");
        }
    };

    public TesseractOCR(String name, CameraFragment fragment, final AssetManager assetManager) {
        this.assetManager = assetManager;
        this.fragment = fragment;
        this.name = name;
    }

    /**
     * Starts OCR scan routine with delay in msec
     * @param delay int how msec before start
     */
    public void startScanner(int delay) {
        myHandler.postDelayed(scan, delay);
    }

    /**
     * Starts (enqueues) a stop routine in a new thread, then returns immediately.
     */
    public void stopScanner() {
        stopping = true;
        if (baseApi != null) {
            baseApi.stop();
        }

        HandlerThread ht = new HandlerThread("cleaner");
        ht.start();
        cleanHandler = new Handler(ht.getLooper());
        cleanHandler.post(new Runnable() {
            @Override
            public void run() {
                cleanup();
            }
        });
        ht.quitSafely();
    }

    /**
     * Starts a new thread to do OCR and enqueues an initialization task;
     */
    public void initialize() {
        myThread = new HandlerThread(name);
        myThread.start();
        timeoutHandler = new Handler();
        myHandler = new Handler(myThread.getLooper());
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                init();
                Log.e(TAG, "INIT DONE");
            }
        });
        isInitialized = true;
    }

    /**
     * Initializes Tesseract library using traineddata file.
     * Should not be called directly, is public for testing.
     */
    public void init() {
        baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        String path = Environment.getExternalStorageDirectory() + "/" + TesseractOCR.FOLDER_TRAINED_DATA + "/";
        File trainedDataFile = new File(path, TesseractOCR.FOLDER_TESSERACT_DATA + "/" + trainedData);
        try {
            mDeviceStorageAccessLock.acquire();
            // Copy trained data in a synchronized block, as this only needs to happen once but is executed by multiple threads
            if (!trainedDataFile.exists()) {
                Log.i(TAG, "No existing trained data found, copying from assets..");
                Util.copyFile(assetManager.open(trainedData), trainedDataFile);
            } else {
                Log.i(TAG, "Existing trained data found");
            }
            mDeviceStorageAccessLock.release();
            baseApi.init(path, trainedData.replace(TesseractOCR.TRAINED_DATA_EXTENSION, "")); //extract language code from trained data file
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            //TODO show error to user, coping failed
        }
    }

    /**
     * Performs OCR scan to bitmap provided, if tesseract is initialized and not currently stopping.
     * Should not be called directly, is public for testing.
     * @param bitmap Bitmap image to be scanned
     * @return Mrz Object containing result data
     */
    public Mrz ocr(Bitmap bitmap) {
        if (bitmap == null) return null;
        if (isInitialized && !stopping) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Log.v(TAG, "Image dims x: " + bitmap.getWidth() + ", y: " + bitmap.getHeight());
            baseApi.setImage(bitmap);
            baseApi.setVariable("tessedit_char_whitelist",
                    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ<");
            String htmlFormattedText = baseApi.getHOCRText(0);
            String recognizedText = htmlFormattedText == null ? null : // On timeout HTML text is null
                    android.text.Html.fromHtml(htmlFormattedText).toString();
            Log.v(TAG, "OCR Result: " + recognizedText);
            return new Mrz(recognizedText);
        } else {
            Log.e(TAG, "Trying ocr() while not initalized or stopping!");
            return null;
        }
    }

    /**
     * Cleans memory used by Tesseract library and closes OCR thread.
     * After this has been called initialize() needs to be called to restart the thread and init Tesseract
     */
    public void cleanup () {
        if (isInitialized) {
            myThread.quitSafely();
            myHandler.removeCallbacks(scan);
            timeoutHandler.removeCallbacks(timeout);
            try {
                myThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            myThread = null;
            myHandler = null;
            baseApi.end();
            isInitialized = false;
            stopping = false;
        }
    }
}
