package com.dji.GSDemo.PathPlanning;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.RandomAccessFile;

public class Tools {
    public static int getInt(String str, int defaultValue) {
        try {
            return Integer.valueOf(str);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static float getFloat(String str, float defaultValue) {
        try {
            return Float.valueOf(str);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static void showToast(final Activity activity, final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void writeTxtToFile(String strcontent, String filePath,
                               String fileName) {
        makeFilePath(filePath, fileName);// 生成文件

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("error:", e + "");
        }
    }


    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }
}
