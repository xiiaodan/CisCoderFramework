package com.baichang.android.request;

import android.text.TextUtils;
import android.util.Log;

import com.baichang.android.common.ConfigurationImpl;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by iscod on 2016/6/22.
 */
public class ResponseConverterFactory1 extends Converter.Factory {
    private final Gson mGson;
    private final GsonConverterFactory mGsonConverterFactory;

    public static ResponseConverterFactory1 create(GsonConverterFactory mGsonConverterFactory) {
        return create(new Gson(), mGsonConverterFactory);
    }

    public static ResponseConverterFactory1 create(Gson mGson, GsonConverterFactory mGsonConverterFactory) {
        return new ResponseConverterFactory1(mGson, mGsonConverterFactory);
    }

    private ResponseConverterFactory1(Gson mGson, GsonConverterFactory mGsonConverterFactory) {
        if (mGson == null) throw new NullPointerException("mGson == null");
        this.mGson = mGson;
        this.mGsonConverterFactory = mGsonConverterFactory;
    }

    /**
     * 服务器相应处理
     * 根据具体Result API 自定义处理逻辑
     *
     * @param mType
     * @param annotations
     * @param retrofit
     * @return 返回Data相应的实体
     */
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type mType,
                                                            Annotation[] annotations,
                                                            Retrofit retrofit) {
        TypeAdapter<?> mAdapter = mGson.getAdapter(TypeToken.get(mType));
        return new BaseResponseBodyConverter<>(mGson, mAdapter, mType);//响应
    }

    /**
     * 请求处理
     * request body 我们无需特殊处理，直接返回 GsonConverterFactory 创建的 converter。
     *
     * @param mType
     * @param parameterAnnotations
     * @param methodAnnotations
     * @param retrofit
     * @return 返回 GsonConverterFactory 创建的 converter
     */
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type mType,
                                                          Annotation[] parameterAnnotations,
                                                          Annotation[] methodAnnotations,
                                                          Retrofit retrofit) {
        return mGsonConverterFactory.requestBodyConverter(mType, parameterAnnotations,
                methodAnnotations, retrofit);
    }

    /**
     * 自定义的result Api处理逻辑
     *
     * @param <T> 泛型
     */
    private class BaseResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        private final TypeAdapter<T> mAdapter;
        private Gson mGson;
        private Type mType;//泛型，当服务器返回的数据为数组的时候回用到
        //error
        private static final String SERVICE_ERROR = "请求服务器异常";
        private static final String DATA_ERROR = "请求数据异常";
        private static final String TAG = "Request";

        private BaseResponseBodyConverter(Gson mGson, TypeAdapter<T> mAdapter, Type mType) {
            this.mAdapter = mAdapter;
            this.mGson = mGson;
            this.mType = mType;
        }

        /**
         * 自定义转换器-处理服务器返回数据
         *
         * @param response
         * @return 返回data的实体or列表
         * @throws IOException
         */
        @Override
        public T convert(ResponseBody response) throws IOException {
            String strResponse = response.string();
            if (TextUtils.isEmpty(strResponse)) {
                throw new HttpException(SERVICE_ERROR);
            }
            Log.i(TAG, "  answer->->->->->->->->->->->->->->->->->->-->->->->->->->-->-" +
                    ">->->->->->->->->->->->->->->->->->->->->->->->->->");
            Log.d(TAG, "response->\n" + strResponse + "  ");
            Log.i(TAG, "<------------------------------------------------------end" +
                    "------------------------------------------------------>");
            HttpResponse httpResponse = mGson.fromJson(strResponse, HttpResponse.class);
            if (httpResponse.getState() != 1) {
                // 服务器异常
                throw new HttpException(httpResponse.getMsg());
            }
            if (httpResponse.getRes().getCode() == 4000) {
                String parameters = httpResponse.getRes().getData().toString();
                if (TextUtils.isEmpty(parameters)) {
                    throw new HttpException(DATA_ERROR);
                }
                return mGson.fromJson(parameters, mType);
            } else if (httpResponse.getRes().getCode() == 30000) {
                ConfigurationImpl.get().refreshToken();
                throw new HttpException(httpResponse.getRes().getMsg());
            } else {
                //接口异常
                throw new HttpException(httpResponse.getRes().getMsg());
            }
        }
    }
}


