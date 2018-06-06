package nl.tudelft.cs4160.trustchain_android.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import nl.tudelft.cs4160.trustchain_android.message.MessageProto;

public class OpenFileClickListener implements View.OnClickListener{

    private static final int REQUEST_STORAGE_PERMISSIONS = 1;

    private Context context;
    private Activity activity;
    private MessageProto.TrustChainBlock block;

    public OpenFileClickListener(Activity activity, MessageProto.TrustChainBlock block) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.block = block;
    }


    @Override
    public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Util.requestWriteStoragePermissions(activity, REQUEST_STORAGE_PERMISSIONS);
            return;
        }

        File file = new File(android.os.Environment.getExternalStorageDirectory()
                + "/TrustChain/" + ByteArrayConverter.bytesToHexString(block.getSignature().toByteArray())
                + "." + block.getTransaction().getFormat());
        if (file.exists()) file.delete();

        byte[] bytes = block.getTransaction().getUnformatted().toByteArray();
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);

        try {
            if (!Util.copyFile(is, file)) {
                Snackbar.make(view, "Copying file to filesystem failed.", Snackbar.LENGTH_LONG).show();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Snackbar.make(view, "Copying file to filesystem failed.", Snackbar.LENGTH_LONG).show();
            return;
        }

        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (mimeType == null) mimeType = "text/plain"; // If no mime type is found, try to open as plain text

        Intent i = new Intent();
        i.setDataAndType(Uri.fromFile(file), mimeType);
        context.startActivity(i);

    }



}
