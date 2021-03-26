/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.liyk.app.diffupdate.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class FileUitls {
    /**
     * Checks if is sd card available.
     *
     * @return true, if is sd card available
     */
    public static boolean isSdCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Gets the SD root file.
     *
     * @return the SD root file
     */
    public static File getSDRootFile() {
        if (isSdCardAvailable()) {
            return Environment.getExternalStorageDirectory();
        } else {
            return null;
        }
    }

    public static File getADDirectory() {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "AD");
        }
        return file;
    }


    public static File getFaceDirectory() {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "faces");
            if (!file.exists()) {
                boolean success = file.mkdirs();
            }
        }

        return file;
    }

    public static File getBatchFaceDirectory(String batchDir) {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, batchDir);
            if (!file.exists()) {
                boolean success = file.mkdirs();
            }
        }

        return file;
    }

    /**
     * 写入TXT文件
     */
    public static boolean writeTxtFile(String content, File dictionary, String fileName) {
        File file = new File(dictionary, fileName);
        RandomAccessFile mm = null;
        boolean flag = false;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content.getBytes("utf-8"));
            fileOutputStream.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static boolean saveFile(File file, Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public static void deleteLicense(Context context, String licenseName) {
        String filePath = context.getFilesDir().getParent() + "/" + licenseName;
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }

        File var4 = context.getDir(licenseName, 0);
        if (var4 != null && var4.exists()) {
            var4.delete();
        }
    }

    // 判断文件是否存在
    public static boolean fileIsExists(File file) {
        try {
            if (!file.exists()) {
                Log.i("wtf", "file_state->" + "file not exits");
                return false;
            }
        } catch (Exception e) {
            Log.i("wtf", "file_state->" + e.getMessage());
            return false;
        }
        return true;
    }

    // 判断文件是否存在
    public static boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                Log.i("wtf", "file_state->" + "file not exits");
                return false;
            }
        } catch (Exception e) {
            Log.i("wtf", "file_state->" + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * @param pathDir 文件所在路径
     * @param oldName
     * @param newName
     * @return
     */
    public static boolean rename(File pathDir, String oldName, String newName) {
        boolean b = false; // 文件重命名是否成功

        try {
            File oldFile = new File(pathDir, oldName);
            File newFile = new File(pathDir, newName);
            b = oldFile.renameTo(newFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }


    // 读取文本文件中的内容
    public static String readFile(String strFilePath) {
        String path = strFilePath;
        String content = ""; // 文件内容字符串
        // 打开文件
        File file = new File(path);
        // 如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    // 分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content = line;
                    }
                    instream.close();
                }
            } catch (FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }


    // 读取文本文件中的内容
    public static ArrayList<String> readLicense(String strFilePath) {
        ArrayList<String> licenseList = new ArrayList<>();
        String path = strFilePath;
        String content = ""; // 文件内容字符串
        // 打开文件
        File file = new File(path);
        // 如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    // 分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content = line;
                        licenseList.add(content);
                    }
                    instream.close();
                }
            } catch (FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return licenseList;
    }

    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory(); // 获取跟目录
        }
        if (sdDir != null) {
            return sdDir.toString();
        }
        return null;
    }

    public static boolean assetOpen(Context context, String modelPath) {
        boolean exist = false;
        if (context == null || TextUtils.isEmpty(modelPath)) {
            return false;
        } else {
            try {
                context.getAssets().open(modelPath);
                exist = true;
            } catch (Exception e) {
            }
        }
        return exist;
    }

    /**
     * 模型加载路径包含SD卡和asset
     *
     * @param context   上下文
     * @param modelName 模型路径
     * @return 模型内容字节数组
     */
    public static byte[] getModelContent(Context context, String modelName) {

        //
        InputStream is = null;
        byte[] bytes = new byte[0];

        // 先从SD卡中读取
        File file = new File(modelName);
        if (file.exists()) {
            try {
                is = new FileInputStream(file);
                bytes = new byte[is.available()];
                is.read(bytes);
            } catch (FileNotFoundException e) {
                // e.printStackTrace();
            } catch (IOException e) {
                // e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
        }

        // 如果SD 卡中模型不为空，返回读取字节
        if (bytes.length > 0) {
            return bytes;
        }

        // 从asset 目录下读取
        try {
            is = context.getResources().getAssets().open(modelName);
            bytes = new byte[is.available()];
            is.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // e.printStackTrace();
            }
        }
        return bytes;
    }

    /**
     * @param context   上下文
     * @param imageName 图片名称
     * @return bitmap对象
     */
    public static Bitmap getBitmap(Context context, String imageName) {
        InputStream is = null;
        try {
            AssetManager assetManager = context.getResources().getAssets();
            is = assetManager.open(imageName);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static byte[] getByteArrayFromAssets(Context context, String imageName) {
        byte[] bytes = null;
        try {
            InputStream is = context.getAssets().open(imageName);
            bytes = new byte[is.available()];
            is.read(bytes);
            is.close();
        } catch (IOException e) {
            Log.e("zq", "e-->" + e.getMessage());
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * 迭代删除文件夹
     *
     * @param dirPath 文件夹路径
     */
    public static void deleteDir(String dirPath) {
        File file = new File(dirPath);
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            if (files == null) {
                file.delete();
            } else {
                for (int i = 0; i < files.length; i++) {
                    deleteDir(files[i].getAbsolutePath());
                }
                file.delete();
            }
        }
    }


    public static boolean copyFile(String oldPath, String newPath) {
        InputStream inStream = null;
        FileOutputStream fs = null;
        boolean result = false;
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            // 判断目录是否存在
            File newfile = new File(newPath);
            File newFileDir = new File(newfile.getPath().replace(newfile.getName(), ""));
            if (!newFileDir.exists()) {
                newFileDir.mkdirs();
            }
            if (oldfile.exists()) { // 文件存在时
                inStream = new FileInputStream(oldPath); // 读入原文件
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                result = true;
            } else {
                result = false;
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
