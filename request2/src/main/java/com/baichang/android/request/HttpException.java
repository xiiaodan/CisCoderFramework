package com.baichang.android.request;

import android.text.TextUtils;

import java.io.IOException;

/**
 * Created by iCong.
 * Time:2016/6/29-17:23.
 */
public class HttpException extends RuntimeException {
    public HttpException(String detailMessage) {
        super(detailMessage);
    }

    private static final long serialVersionUID = -2199603193956026137L;
    private static final String SERVICE_ERROR = "请求服务器异常";

    @Override
    public String getMessage() {
        if (TextUtils.isEmpty(super.getMessage()))
            return SERVICE_ERROR;
        else {
            return super.getMessage();
        }
    }
}
