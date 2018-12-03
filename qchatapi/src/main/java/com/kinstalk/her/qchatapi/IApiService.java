/*
 * Copyright (c) 2017. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchatapi;

import okhttp3.HttpUrl;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by Knight.Xu on 2018/4/10.
 * APi service
 */
public interface IApiService {

    @POST()
    Call<ResponseBody> engineApi(@Url HttpUrl url, @Body RequestBody body);

}
