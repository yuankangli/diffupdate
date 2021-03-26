package com.liyk.app.diffupdate.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.liyk.app.diffupdate.listener.OnToastListener;
import com.liyk.app.diffupdate.network.GetUpdateInfo;

import java.io.File;


/**
 * @author : liyk
 * @version 1.0
 * @date : 2020/9/17 9:57
 */
public class UpdateUtil {

    private static final String TAG = "diffUpdate";

    private static Handler updateHandler = null;
    private static final String UPDATE_INFO = "重大更新";

    private static boolean updating = false;


    private static OnToastListener onToastListener;

    public static void setOnToastListener(OnToastListener onToastListener) {
        UpdateUtil.onToastListener = onToastListener;
    }

    private static String getNewAppPath(Context context) {
        String versionCode = APKVersionCodeUtils.getVerName(context);
//        return FileUitls.getSDPath() + "/debug_v6.1.5.apk";
        return FileUitls.getSDPath() + "/appDownload/app_update_for_v" + versionCode + ".apk";
    }

    public static void installLocalApp(Context context) {
        // 判断本地文件是否存在
        String newAPKPath = getNewAppPath(context);
        File file = new File(newAPKPath);
        if (file.exists()) {
            install(context);
        } else {
            Log.i(TAG, "[检查更新] 本地不存在更新文件, 删除更新文件夹");
            FileUitls.deleteDir(FileUitls.getSDPath() + "/appDownload");
        }
    }

    public static void patchFile(Context context, String patchFilePath, String md5, boolean autoInstall) {
        String oldApkSource = ApkUtils.getSourceApkPath(context, context.getPackageName());
        String newAPKPath = getNewAppPath(context);
        int patchResult = PatchUtils.patch(oldApkSource, newAPKPath, patchFilePath);
        if (patchResult == 0) {
//            if (true) {
            if (SignUtils.checkMd5(newAPKPath, md5)) {
                Log.i(TAG, "[检查更新] 更新文件已成功下载且md5正确");
                showToast("更新文件已成功下载, 且md5校验成功");
                if (autoInstall) {
                    installLocalApp(context);
                } else {
                    showAppUpdateDialog(context);
                }
            } else {
                Log.i(TAG, "[检查更新] 更新文件已成功下载但md5错误, 删除更新文件夹");
                FileUitls.deleteDir(FileUitls.getSDPath() + "/appDownload");
                showToast("更新文件合并出错, md5校验出错");
//                ToastUtils.showToast("更新文件md5校验失败");
            }
        } else {
            Log.i(TAG, "[检查更新] 无法合并差异文件, 删除更新文件夹");
            FileUitls.deleteDir(FileUitls.getSDPath() + "/appDownload");
            showToast("无法合并更新文件");
        }
    }

    public static void install(Context context) {
        String apkPath = getNewAppPath(context);
        Log.i(TAG, "[检查更新] 开始安装新APP");
        boolean hasRoot = ShellUtils.checkRootPermission();
        if (hasRoot) {
            showToast("拥有root权限, 开始进行静默安装, 全程无需操作, 更新后自动重启");
            Log.i(TAG, "开始静默安装， 并等待重启");
            // todo 如果有更好的重启APP方法, 请issue联系我, 谢谢
            Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage(context.getPackageName());
            PendingIntent restartIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, 0);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.set(AlarmManager.RTC, System.currentTimeMillis() + 15000, restartIntent); // 15秒钟后重启应用
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15000, restartIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                manager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15000, restartIntent);
            } else {
                manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, restartIntent);
            }
            int resultCode = PackageUtils.installSilent(context, apkPath);


            if (resultCode != PackageUtils.INSTALL_SUCCEEDED) {
                showToast("安装失败, 可手动打开安装包/appDownload/");
//                manager.cancel(restartIntent);
                // TODO 正式使用需删除文件包, 不然会进入更新死循环
//                FileUitls.deleteDir(FileUitls.getSDPath() + "/appDownload");
            }
            System.exit(0);
        } else {
//            ToastUtils.showToast("没有ROOT权限");
            showToast("没有ROOT权限, 调用 android 安装器");

            File apkFile = new File(apkPath);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.w(TAG, "版本大于 N ，开始使用 fileProvider 进行安装");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(
                        context
                        , "com.liyk.app.diffupdate.fileprovider"
                        , apkFile);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                Log.w(TAG, "正常进行安装");
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            }
            ((Activity) context).startActivity(intent);
        }
    }


    private static void showToast(String content) {
        if (onToastListener != null) {
            onToastListener.showToastMessage(content);
        }
    }

    private static void showAppUpdateDialog(Context context) {
        // todo 自己实现 dialog 显示, 此处直接调用更新操作
        showToast("请自己实现对应dialog提示, 并调用 installLocalApp(context) 方法, 下载的apk文件在sd/appDownload/app_update_for_vxx.xx.apk中, 可手动点击查看");
    }

    /**
     * @param context
     * @param autoInstall 是否直接安装, 如果为false, 则弹出dialog, 提示用户更新信息并在确定后进行安装
     */
    public static void updateApp(Context context, boolean autoInstall) {
        showToast("开始检查更新");
        Log.i(TAG, "开始进行 [检查更新] 操作");
        if (updating) {
            Log.i(TAG, "当前正在进行 [检查更新] 操作, 请勿重复点击");
            showToast("当前已有更新任务, 请勿重复点击");
            return;
        }
        updating = true;
        try {
            String versionCode = APKVersionCodeUtils.getVerName(context);
            String newAPKPath = getNewAppPath(context);
            // 判断本地是否已存在文件, 如果存在, 则直接更新
            if (new File(newAPKPath).exists()) {
                showToast("更新文件已存在, 直接更新");
                Log.i(TAG, "[检查更新] 更新文件已在本地文件中存在, 直接进行更新操作");
                if (autoInstall) {
                    installLocalApp(context);
                } else {
                    showAppUpdateDialog(context);
                }
            } else {
                GetUpdateInfo.getUpdatePatchInfo(context, autoInstall, versionCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getLocalizedMessage());
        } finally {
            updating = false;
        }

    }


}
