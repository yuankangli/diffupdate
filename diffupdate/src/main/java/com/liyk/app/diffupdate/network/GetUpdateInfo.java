package com.liyk.app.diffupdate.network;

import android.content.Context;
import android.util.Log;

import com.liyk.app.diffupdate.listener.OnToastListener;
import com.liyk.app.diffupdate.util.FileUitls;
import com.liyk.app.diffupdate.util.UpdateUtil;

import java.io.File;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GetUpdateInfo {


    /**
     * todo 获取更新地址, 需要自己实现
     * 上传参数: versionCode(当前设备版本号)
     * 返回参数: patchFileUrl(差分包下载路径)
     * updatedMd5(新安装包的md5, 用于后续合成新安装包后进行对比)
     * modifyContent(更新信息)
     * updatedVersionCode(新安装包版本号)
     */
    public static final String GET_UPDATE_INFO_URL = "http://192.168.1.126:8888/diffUpdate/";

    private static final String TAG = "diffUpdate";

    private static OnToastListener onToastListener;

    public static void setOnToastListener(OnToastListener onToastListener) {
        GetUpdateInfo.onToastListener = onToastListener;
    }

    public static void getUpdatePatchInfo(Context context, boolean autoInstall, String versionCode) {
        Retrofit retrofit = new Retrofit.Builder()
                //指定baseurl，这里有坑，最后后缀一定要带着“/”
                .baseUrl(GET_UPDATE_INFO_URL)
                //设置内容格式,这种对应的数据返回值是String类型
                .addConverterFactory(GsonConverterFactory.create())
                //定义client类型camera.o
                .client(new OkHttpClient())
                //创建
                .build();
        Call<Map<String, Object>> updateInfo = retrofit.create(APIServer.class).getAppUpdateInfo(versionCode);
        showToast("更新请求地址: " + updateInfo.request().url());
        Log.i(TAG, "[检查更新] 请求地址为: " + updateInfo.request().url());
        updateInfo.enqueue(new Callback<Map<String, Object>>() {

            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Map<String, Object> body = response.body();
                if (body != null) {
                    Boolean needUpdate = (Boolean) body.get("needUpdate");
                    if (needUpdate) {
                        String patchFileUrl = body.get("patchFileUrl").toString();
                        String updatedMd5 = body.get("updatedMd5").toString();
                        String modifyContent = body.get("modifyContent").toString();
                        String updatedVersionCode = body.get("updatedVersionCode").toString();
                        String url = GET_UPDATE_INFO_URL + "/downloadApp?patchFileUrl=" + patchFileUrl;
                        Log.i(TAG, "[检查更新] 成功: 当前新版本为: v" + updatedVersionCode);
                        new Thread(() -> {
                            showToast(String.format("新版本号: v%s\r\n  更新说明: %s\n" +
                                    "  新版本md5: %s", updatedVersionCode, modifyContent, updatedMd5));
                            Log.i(TAG, "[检查更新] : 开始下载");
                            downloadPatch(context, patchFileUrl, url, autoInstall, updatedMd5);
                        }).start();
                    } else {
                        showToast("当前已是最新版");
                        Log.i(TAG, "[检查更新] 结果: 没有新版本可用");
                        // 删除文件包
                        FileUitls.deleteDir(FileUitls.getSDPath() + "/appDownload");
                    }
                } else {
                    Log.i(TAG, "[检查更新] 失败, 服务器没有返回任何资料");
                    showToast("检查更新时, 服务器未正确响应");
                    // 删除文件包
                    FileUitls.deleteDir(FileUitls.getSDPath() + "/appDownload");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showToast("检查更新时, 发生错误: " + t.getLocalizedMessage());
                Log.i(TAG, "[检查更新] 失败, 原因: " + t.getLocalizedMessage());
            }
        });
    }

    private static void downloadPatch(Context context, String patchFileUrl, String url, boolean autoInstall, String updatedMd5) {
        showToast("新版文件下载地址: " + url);
        DownloadUtil.get().download(url, FileUitls.getSDPath() + "/appDownload",
                patchFileUrl, new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {
//                        showToast("更新文件下载成功, 即将开始合并文件");
                        Log.i(TAG, "[检查更新] 下载结果: 文件下载成功");
                        showToast("更新文件下载成功, 等待合并中...");
                        UpdateUtil.patchFile(context, file.getAbsolutePath(), updatedMd5, autoInstall);
                    }

                    @Override
                    public void onDownloading(int progress) {

                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        showToast("更新文件下载失败, 原因: " + e.getLocalizedMessage());
                        Log.i(TAG, "[检查更新] 下载结果: 下载失败, 原因: " + e.getLocalizedMessage());
                    }
                });
    }

    private static void showToast(String content) {
        if (onToastListener != null) {
            onToastListener.showToastMessage(content);
        }
    }
}
