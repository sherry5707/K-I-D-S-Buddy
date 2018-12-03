package com.kinstalk.her.qchat.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.text.TextUtils;

import com.kinstalk.her.qchatcomm.utils.QAILog;

public class FileUtil {

    private FileUtil() {
    }

    /**
     * 创建文件夹或者文件
     *
     * @param path 如果传入为目录则创建目录，如果传入为文件路径则创建文件
     * @return
     */
    public static boolean create(String path) {
        String separator = File.separator;
        int index = path.lastIndexOf(separator);
        if (index > 0) {

            String dir = path.substring(0, index);
            if (!createDir(dir)) {
                return false;
            }

            return creatFile(path, true);
        }

        return creatFile(path, true);
    }

    /**
     * 创建文件夹或者文件
     *
     * @param path     如果传入为目录则创建目录，如果传入为文件路径则创建文件
     * @param recreate true-> 如果为文件路径且文件已经存在则删除重建;false -> 如果为文件路径且文件已经存在则直接返回
     * @return
     */
    public static boolean create(String path, boolean recreate) {
        String separator = File.separator;
        int index = path.lastIndexOf(separator);
        if (index > 0) {

            String dir = path.substring(0, index);
            if (!createDir(dir)) {
                return false;
            }

            return creatFile(path, true);
        }

        return creatFile(path, true);
    }

    /**
     * 创建目录，如果目录已经存在则直接返回
     *
     * @param path
     * @return true -> 目录创建成功、目录已经存在;false -> 目录创建失败
     */
    public static boolean createDir(String path) {
        File file = new File(path);
        if (!file.exists())
            return file.mkdirs();
        return true;
    }

    /**
     * 创建文件
     *
     * @param path
     * @param recreate true -> 如果存在则删除重建；false -> 如果存在则直接返回
     * @return true -> 文件创建成功;false -> 文件创建失败
     */
    public static boolean creatFile(String path, boolean recreate) {
        File file = new File(path);

        if (recreate && file.exists()) {
            file.delete();
        }

        if (!file.exists()) {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                QAILog.e("创建文件失败 " + path);
                return false;
            }
        }
        return true;
    }

    /**
     * 检测目标文件是否存在
     *
     * @param filePath 路径
     * @return true->存在；否则表示不存在
     */
    public static boolean exist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * 检测目标文件是否存在
     *
     * @param dir      目录
     * @param fileName 文件名称
     * @return true->存在；否则表示不存在
     */
    public static boolean exist(String dir, String fileName) {
        File file = new File(dir, fileName);
        return file.exists();
    }

    /**
     * 删除指定文件
     *
     * @param filePath
     * @return
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除指定目录、文件
     *
     * @param path 目录或者文件路径
     * @return
     */
    public static void delete(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else {
                String parentPath = file.getAbsolutePath();
                String[] subFile = file.list();
                if (subFile == null || subFile.length == 0) { // 子文件列表为空则删除空目录
                    file.delete();
                } else {
                    for (String name : subFile) {
                        String filePath = parentPath + File.separator + name;
                        delete(filePath);
                    }
                }
            }
        }
    }

    /**
     * 获取文件后缀名称
     *
     * @param path 文件路径
     * @return
     */
    public static String getFileSuffix(String path) {
        if (TextUtils.isEmpty(path)) return null;
        int index = path.lastIndexOf(".");
        if (index > 0 && index + 1 < path.length() - 1) {
            return path.substring(index + 1);
        }
        return null;
    }

    /**
     * 获取文件内容
     *
     * @param filePath  文件路径
     * @param buffer    缓冲区
     * @param skipCount 文件读取起始位置
     * @return 已读数据长度
     */
    public static int readFileData(String filePath, byte[] buffer, int skipCount) {
        InputStream input = openFileInput(filePath);
        if (input != null) {
            int count = -2;
            try {
                input.skip(skipCount);
                count = input.read(buffer);
            } catch (IOException e) {
                QAILog.e("读取文件内容异常", e.getMessage());
                closeInputStream(input);
                return -1;
            }

            closeInputStream(input);
            if (count > -1) {
                byte[] data = new byte[count];
                System.arraycopy(buffer, 0, data, 0, count);
                return count;
            }
        }
        return -1;
    }

    /**
     * 打开文件输入流
     *
     * @param path
     * @return
     */
    public static InputStream openFileInput(String path) {
        File file = new File(path);
        if (file.exists() && file.canRead()) {
            try {
                FileInputStream input = new FileInputStream(file);
                return input;
            } catch (FileNotFoundException e) {
                QAILog.e("打开文件输入流失败", path);
            }
        }
        return null;
    }

    /**
     * 关闭文件输入流
     *
     * @param input
     */
    public static void closeInputStream(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                QAILog.e("关闭文件输入流失败", e.getMessage());
            }
        }
    }

    /**
     * 打开文件输出流
     *
     * @param path
     * @return
     */
    public static OutputStream openFileOutput(String path) {
        File file = new File(path);
        if (file.exists() && file.canWrite()) {
            try {
                FileOutputStream output = new FileOutputStream(file);
                return output;
            } catch (FileNotFoundException e) {
                QAILog.e("打开文件输出流失败", path);
            }
        }
        return null;
    }

    /**
     * 关闭文件输出流
     *
     * @param output
     */
    public static void closeOutputStream(OutputStream output) {
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {
                QAILog.e("关闭文件输出流失败", e.getMessage());
            }
        }
    }

    /**
     * 追加数据到当前指定的文件中
     *
     * @param data       待写入的数据
     * @param byteOffset data 起始位置
     * @param byteCount  data 要写入的长度
     * @param filePath   目标文件路径
     * @return true -> 写入成功； false -> 写入失败
     */
    public static boolean appendToFile(byte[] data, int byteOffset, int byteCount, String filePath) {

        File f = new File(filePath);
        if (!create(filePath)) {
            QAILog.e("创建文件失败", filePath);
            return false;
        }

        RandomAccessFile file;
        try {
            file = new RandomAccessFile(f, "rw");
            long seekPosition = file.length();
            file.seek(seekPosition);
            file.write(data);
            file.write(data, byteOffset, byteCount);
            file.close();
            return true;
        } catch (Exception e) {
            QAILog.e("写入数据到", filePath, "异常", e.getMessage());
            return false;
        }
    }

    /**
     * 向指定的路径写入数据
     *
     * @param data     数据
     * @param offset   数据写入开始位置
     * @param count    数据写入长度
     * @param filePath 目标路径
     * @return true -> 写入成功;false -> 写入失败
     */
    public static boolean writeToFile(byte[] data, int offset, int count, String filePath) {

        if (!FileUtil.create(filePath)) {
            QAILog.e("创建文件失败", filePath);
            return false;
        }

        OutputStream output = openFileOutput(filePath);
        if (output != null) {
            try {
                output.write(data, offset, count);
            } catch (IOException e) {
                QAILog.e("写入文件失败", filePath);
                closeOutputStream(output);
                return false;
            }
            closeOutputStream(output);
            return true;
        }
        return false;
    }

    /**
     * 关闭流
     *
     * @param stream
     */
    public static void closeStream(Closeable stream) {
        if (null == stream) return;
        try {
            stream.close();
        } catch (IOException e) {
            QAILog.e("关闭流异常", e.getMessage());
        }
    }

    /**
     * 将输入流写入到指定的文件中，写入结束后，别忘记关闭处理输入流
     *
     * @param input    输入流
     * @param filePath 存储目标文件路径
     * @return -1 表示文件写入过程中异常；>= 0 表示写入数据长度
     */
    public static long writeToFile(InputStream input, String filePath) {
        if (create(filePath)) {
            OutputStream output = openFileOutput(filePath);
            if (null != output) {
                byte[] buffer = new byte[1024];
                int read = 0;
                long count = 0;
                try {
                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                        count += read;
                    }
                } catch (IOException e) {
                    QAILog.e("读取输入流异常", e.getMessage());
                    return -1;
                } finally {
                    closeStream(output);
                }
                return count;
            }
        }
        return -1;
    }

    /**
     * 读取指定文件长度
     *
     * @param filePath
     * @return 如果此文件不存在、不可读、文件夹则返回 -1；否则返回文件长度
     */
    public static long getFileLength(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.canRead() && file.isFile()) {
            return file.length();
        }
        return -1;
    }

    /**
     * 获取指定目录下文件的总大小
     *
     * @param dirName
     * @return
     */
    public static long getDirFileLength(String dirName) {
        long size = 0;
        File file = new File(dirName);
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (null != subFiles) {
                for (File f : subFiles) {
                    if (f.isDirectory()) {
                        getDirFileLength(f.getAbsolutePath());
                    } else {
                        size += f.length();
                    }
                }
            }
        }
        return size;
    }

    /**
     * 将 byte 转化到 MB 显示
     *
     * @param size 单位为byte表示的大小
     * @return
     */
    public static String byteSizetoMBSize(long size) {
        if (size < 0) {
            QAILog.e("byteSize 大小需大于0", size);
            return null;
        }

        BigDecimal unit = new BigDecimal(1024);
        BigDecimal byteSize = new BigDecimal(size);
        BigDecimal KBSize = byteSize.divide(unit);
        BigDecimal MBSize = KBSize.divide(unit);
        return Double.toString(MBSize.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
    }

    @SuppressWarnings("unused")
    private static String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 读取某个目录下所有文件
     *
     * @param path
     * @return
     */
    public static ArrayList<String> getFiles(String path) {
        ArrayList<String> files = new ArrayList<String>();
        File file = new File(path);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
//              System.out.println("文     件：" + tempList[i]);
                files.add(tempList[i].toString());
            }
//            if (tempList[i].isDirectory()) {
//              System.out.println("文件夹：" + tempList[i]);
//            }
        }
        return files;
    }
}
