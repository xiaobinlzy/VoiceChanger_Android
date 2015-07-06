package com.dll.util;

import java.io.File;

import android.content.Context;

/**
 * 文件路径的获取和拼接
 * 
 * @author DLL email: xiaobinlzy@163.com
 * 
 */
public class FilePathUtil {

    /**
     * 生成存储文件的路径，如果有sd卡则获取sd卡路径，否则获取应用缓存区路径。
     * 
     * @param context
     *            应用Context
     * @param folderPath
     *            文件夹路径
     * @param fileName
     *            文件名
     * @return 生成的文件路径
     */
    public static String makeFilePath(Context context, String folderPath, String fileName) {
	File file = null;
	if (android.os.Environment.getExternalStorageState().equals(
		android.os.Environment.MEDIA_MOUNTED)) {
	    file = new File(android.os.Environment.getExternalStorageDirectory(),
		    folderPath);
	} else {
	    file = context.getApplicationContext().getCacheDir();
	}
	if (!file.exists() || !file.isDirectory()) {
	    file.mkdirs();
	}
	StringBuilder absoluteFolderPath = new StringBuilder(file.getAbsolutePath());
	if (!absoluteFolderPath.toString().endsWith("/")) {
	    absoluteFolderPath.append("/");
	}
	if (fileName != null) {
	    absoluteFolderPath.append(fileName);
	}
	return absoluteFolderPath.toString();
    }

    /**
     * 清空某一路径下的文件
     * 
     * @param context
     * @param filePath
     */
    public static void clearFilePath(Context context, File filePath) {
	if (!filePath.exists()) {
	    return;
	}
	if (filePath.isFile()) {
	    filePath.delete();
	    return;
	}
	if (filePath.isDirectory()) {
	    File[] folders = filePath.listFiles();
	    for (int i = 0; i < folders.length; i++) {
		clearFilePath(context, folders[i]);
	    }
	}
    }

}
