package com.creative.fines.app.retrofit;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GS on 2017-08-07.
 */
public class NetworkInterceptor implements Interceptor {
    private static final String TAG = "NetworkInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();

        builder.addHeader("comp_database", "");

        Request request = chain.request();
//        UtilClass.logD(TAG, "method="+request.method());
//        UtilClass.logD(TAG, "headers="+request.headers());
//        UtilClass.logD(TAG, "connection="+chain.connection());

        Response response = chain.proceed(request);
//        UtilClass.logD(TAG, "////////////////////////////////");
//        UtilClass.logD(TAG, "url="+response.request().url());
//        UtilClass.logD(TAG, "headers="+response.headers());

        return response;
    }

}
