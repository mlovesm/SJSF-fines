package com.creative.fines.app.bean;

import com.creative.fines.app.menu.MainFragment;

import org.apache.http.entity.mime.content.FileBody;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

/**
 * Created by GS on 2017-05-31.
 */
public interface RetrofitService {

    @FormUrlEncoded
    @POST("Common/menuLogInsert")
    Call<Datas> menuLogInsert(@Field("module_nm") String module_nm, @Field("sabun_no") String sabun_no);

    @Multipart
    @POST("/Inno/fileUpload")
    Call<Datas> fileUpload(@Part MultipartBody.Part body);

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(MainFragment.ipAddress+MainFragment.contextPath+"/rest/")
//            .baseUrl("https://api.github.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build();



}
