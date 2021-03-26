package com.liyk.app.diffupdate.network;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIServer {

    /**
     * 获取更新信息
     *
     * @return
     */
    @GET("getAppUpdateInfo")
    Call<Map<String, Object>> getAppUpdateInfo(@Query("versionCode") String versionCode);

}
