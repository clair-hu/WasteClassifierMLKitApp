package com.example.wasteclassifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;

public class BaseActivity extends AppCompatActivity {
    // constants
    public static final int RC_STORAGE_PERMS1 = 101;
    public static final int RC_STORAGE_PERMS2 = 102;
    public static final int RC_SELECT_PICTURE = 103;
    public static final int RC_TAKE_PICTURE = 104;

    public File imageFile;

    /* check storage permission */
    public void checkStoragePermission(int requestCode) {
        switch (requestCode) {
            case RC_STORAGE_PERMS1:
                int hasWriteExternalStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (PackageManager.PERMISSION_GRANTED == hasWriteExternalStoragePermission) {
                    selectPictureFromAlbum();
                }
        }
    }

    private static File createNewImageFile (File file) {
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/com.example.wasteclassifier");
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        if (null == file) {
            file = new File(dir, "original.jpg");
        }
        return file;
    }

    private void selectPictureFromAlbum () {
        imageFile = createNewImageFile(imageFile);
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RC_SELECT_PICTURE);
    }

}
