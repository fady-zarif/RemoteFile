package com.resideo.filetransferfrompanel;

import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Base64ToFileConverter implements ISaveFile {
    private static final String TAG = "CustomVoiceUtil:";

    @Override
    public void convert(String encodedString, String destPath, String fileName, CompletionHandler completionHandler) {
        FileOutputStream osf = null;
        createDirIfNotExist(destPath);
        File file = new File(destPath, fileName);
        try {
            if(!file.exists()) {
                boolean fileStatus = file.createNewFile();
                Log.d(TAG, "Base64ToJpegConverter::fileCreate" + fileStatus);
            }

            byte[] decodedBytes = Base64.decode(encodedString, Base64.NO_WRAP);
            osf = new FileOutputStream(file);
            osf.write(decodedBytes);
            osf.flush();
            completionHandler.onSuccess(file.getPath());
        } catch (IOException e) {
            completionHandler.onFailed(e.toString());
            Log.e(TAG, "Base64ToJpegConverter" + e.toString());
        } finally {
            try {
                if(osf != null)
                    osf.close();
            } catch (IOException e) {
                Log.e(TAG, "Base64ToJpegConverter :Exception" + e.toString());
            }
        }
    }

    private void createDirIfNotExist(String dirPath) {
        File file = new File(dirPath);
        if(!file.exists() || !file.isDirectory())
            file.mkdirs();
    }
}
