/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchatcomm.utils;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Knight.Xu on 2018/4/6.
 */
public final class FileUtils {

    private static final String TAG = "FileUtils";

    public static void debugFile(Context mContext, String str) throws Exception {
        File file = FileUtils.getSaveFile(mContext.getPackageName()
                        + File.separator + "debug",
                SystemTool.getDataTime("yyyy-MM-dd-HH-mm-ss") + ".log");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(str);
        bw.flush();
        bw.close();


    }

    public static void saveFileCache(byte[] fileData, String folderPath,
                                     String fileName) {
        File folder = new File(folderPath);
        folder.mkdirs();
        File file = new File(folderPath, fileName);
        ByteArrayInputStream is = new ByteArrayInputStream(fileData);
        OutputStream os = null;
        if (file.exists())
            return;
        try {
            file.createNewFile();
            os = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while (-1 != (len = is.read(buffer))) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(new Closeable[]{is, os});
        }
    }

    public static File getSaveFile(String folderPath, String fileNmae) {
        File file = new File(getSavePath(folderPath) + File.separator
                + fileNmae);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static String getSavePath(String folderName) {
        return getSaveFolder(folderName).getAbsolutePath();
    }

    public static File getSaveFolder(String folderName) {
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsoluteFile()
                + File.separator
                + folderName
                + File.separator);
        file.mkdirs();
        return file;
    }

    public static final byte[] input2byte(InputStream inStream) {
        if (inStream == null)
            return null;
        byte[] in2b = null;
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        try {
            while ((rc = inStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            in2b = swapStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(new Closeable[]{swapStream});
        }
        return in2b;
    }

    public static File uri2File(Activity aty, Uri uri) {
        if (SystemTool.getSDKVersion() < 11) {
            String[] proj = {"_data"};

            Cursor actualimagecursor = aty.managedQuery(uri, proj, null, null,
                    null);
            int actual_image_column_index = actualimagecursor
                    .getColumnIndexOrThrow("_data");
            actualimagecursor.moveToFirst();
            String img_path = actualimagecursor
                    .getString(actual_image_column_index);
            return new File(img_path);
        }

        String[] projection = {"_data"};
        CursorLoader loader = new CursorLoader(aty, uri, projection, null,
                null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow("_data");
        cursor.moveToFirst();
        return new File(cursor.getString(column_index));
    }

    public static void copyFile(File from, File to) {
        if ((from == null) || (!(from.exists())))
            return;
        if (to == null)
            return;
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(from);
            if (!(to.exists())) {
                to.createNewFile();
            }
            os = new FileOutputStream(to);
            copyFileFast(is, os);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(new Closeable[]{is, os});
        }
    }

    public static void copyFileFast(FileInputStream is, FileOutputStream os)
            throws IOException {
        FileChannel in = is.getChannel();
        FileChannel out = os.getChannel();
        in.transferTo(0L, in.size(), out);
    }

    public static void closeIO(Closeable[] closeables) {
        if ((closeables == null) || (closeables.length <= 0))
            return;
        for (Closeable cb : closeables)
            try {
                if (cb != null) {
                    cb.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static boolean bitmapToFile(Bitmap bitmap, String filePath) {
        boolean isSuccess = false;
        if (bitmap == null)
            return isSuccess;
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(filePath), 8192);
            isSuccess = bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeIO(new Closeable[]{out});
        }
        return isSuccess;
    }

    public static String readFile(String filePath) {
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
        } catch (Exception e) {
            e.printStackTrace();
//			throw new KJException(FileUtils.class.getName() + "readFile---->"
//					+ filePath + " not found");
        }
        return inputStream2String(is);
    }

    public static String readFileFromAssets(Context context, String name) {
        InputStream is = null;
        try {
            is = context.getResources().getAssets().open(name);
        } catch (Exception e) {
            e.printStackTrace();
//			throw new KJException(FileUtils.class.getName()
//					+ ".readFileFromAssets---->" + name + " not found");
        }
        return inputStream2String(is);
    }

    public static String inputStream2String(InputStream is) {
        if (is == null)
            return null;
        StringBuilder resultSb = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            resultSb = new StringBuilder();
            String len;
            while ((len = br.readLine()) != null) {
                resultSb.append(len);
            }
        } catch (Exception localException) {
        } finally {
            closeIO(new Closeable[]{is});
        }
        return ((resultSb == null) ? null : resultSb.toString());
    }

    public static String getMimeType(String fileUrl) throws IOException,
            MalformedURLException {
        String type = null;
        URL u = new URL(fileUrl);
        URLConnection uc = null;
        uc = u.openConnection();
        type = uc.getContentType();
        return type;
    }

    public static void saveCrashFile(String fileDir, String fileName, String crashContent, boolean append) {

        File outputPath = new File(fileDir);
        if (!outputPath.exists()) {
            outputPath.mkdirs();
        }

        File crashFile = new File(fileDir, fileName);
        if (!crashFile.exists()) {
            try {
                crashFile.createNewFile();
            } catch (IOException var10) {
                var10.printStackTrace();
            }
        }

        try {
            FileWriter fw = new FileWriter(crashFile, append);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(crashContent);
            bw.close();
            fw.close();
        } catch (FileNotFoundException var9) {
            var9.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveFile(byte[] buffer, String fileName, String typeName, boolean append) {
        String fileDir = Environment.getExternalStorageDirectory().getPath() + "/szjyvoicerecord/";
        saveFile(fileDir, buffer, fileName, typeName, append);
    }

    public static void saveFile(String fileDir, byte[] buffer, String fileName, String typeName, boolean append) {
        if (buffer != null) {

            File outputPath = new File(fileDir);
            if (!outputPath.exists()) {
                outputPath.mkdirs();
            }

            File pcmFile = new File(fileDir, fileName + "." + typeName);
            if (!pcmFile.exists()) {
                try {
                    pcmFile.createNewFile();
                } catch (IOException var10) {
                    var10.printStackTrace();
                }
            }

            FileOutputStream pcmOutputStream = null;

            try {
                pcmOutputStream = new FileOutputStream(pcmFile, append);
            } catch (FileNotFoundException var9) {
                var9.printStackTrace();
            }

            try {
                if (typeName.equals("wav")) {
                    pcmOutputStream.write((new WaveHeader(buffer.length)).getHeader());
                }

                pcmOutputStream.write(buffer, 0, buffer.length);
                pcmOutputStream.flush();
                pcmOutputStream.close();
            } catch (IOException var8) {
                var8.printStackTrace();
            }

        }
    }


    /**
     * Tries to delete a file from the device.
     * If it fails, the error will be printed in the LogCat.
     *
     * @param fileToDelete file to delete
     * @return true if the file has been deleted, otherwise false.
     */
    public static boolean deleteFile(File fileToDelete) {
        boolean deleted = false;

        try {
            if (fileToDelete.exists()) {
                deleted = fileToDelete.delete();
                QAILog.d(TAG, "File exist and delete");
            } else {
                QAILog.d(TAG, "File does not exist");
            }

            if (!deleted) {
                QAILog.e(TAG, "Unable to delete: "
                        + fileToDelete.getAbsolutePath());
            } else {
                QAILog.d(TAG, "File deleted successfully");
//				QAILog.i(TAG, "Successfully deleted: "
//						+ fileToDelete.getAbsolutePath());
            }

        } catch (Exception exc) {
            QAILog.e(TAG,
                    "Error while deleting: " + fileToDelete.getAbsolutePath() +
                            " Check if you granted: android.permission.WRITE_EXTERNAL_STORAGE", exc);
        }

        return deleted;
    }

    /**
     * gzip压缩
     *
     * @param srcFile    源文件
     * @param targetFile 目标文件
     */
    public static void compressGzip(File srcFile, File targetFile, boolean deleteSrcFile) {
        FileInputStream fis = null;
        GZIPOutputStream gos = null;
        try {
            fis = new FileInputStream(srcFile);
            // 往外写的时候, 用GZIPOutputStream, 直接写成压缩文件, 包装流
            gos = new GZIPOutputStream(new FileOutputStream(targetFile));
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                gos.write(buffer, 0, len);
            }
            if (deleteSrcFile) {
                srcFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuitely(fis);
            closeQuitely(gos);
        }
    }

    /**
     * 解压缩
     *
     * @param srcFile    源文件 压缩包
     * @param targetFile 目标文件 普通文件
     */
    public static void uncompressGzip(File srcFile, File targetFile) {
        GZIPInputStream gis = null;
        FileOutputStream fos = null;
        try {
            gis = new GZIPInputStream(new FileInputStream(srcFile));
            fos = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = gis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuitely(gis);
            closeQuitely(fos);
        }
    }

    /**
     * 关闭流
     *
     * @param stream
     */
    public static void closeQuitely(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getDuration(String path){
        int dur = 0;
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(path);
            String durStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            dur = Long.valueOf(Long.parseLong(durStr)+500).intValue() / 1000;//对时间四舍五入计算，bug13927
            Log.d(TAG, "getDuration: " + dur);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return dur;
    }
    public static int getDurationOutMessage(String path){
        int dur = 0;
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(path);
            String durStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//            dur = Long.valueOf(Long.parseLong(durStr)+500).intValue() / 1000;//对时间四舍五入计算，bug13927
            dur = Long.valueOf(Long.parseLong(durStr)).intValue() / 1000;
            if (dur==0){
                //mtime计数，偶尔会有文件实际小于1000的情况。。。
                dur = 1;
            }
            Log.d(TAG, "getDuration: " + durStr+"druing:"+dur);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return dur;
    }
}