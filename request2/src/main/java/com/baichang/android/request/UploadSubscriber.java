package com.baichang.android.request;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import io.reactivex.observers.DisposableObserver;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

/**
 * Created by iCong.
 * Time:2017年7月6日
 */

public abstract class UploadSubscriber<T> extends DisposableObserver<T> {
  private Context mContext = null;
  private ProgressDialog mDialog = null;
  private static final String TAG = "Upload";

  public UploadSubscriber(Context context) {
    if (context == null) throw new NullPointerException("Context not null");
    WeakReference<Context> wf = new WeakReference<Context>(context);
    mContext = wf.get();
  }

  public UploadSubscriber(ProgressDialog dialog) {
    if (dialog == null) throw new NullPointerException("ProgressDialog not null");
    mDialog = dialog;
  }

  @Override public void onStart() {
    super.onStart();
    if (!NetWorkStateUtils.isNetworkConnected()) {
      Toast.makeText(mContext, R.string.net_error_tips, Toast.LENGTH_SHORT).show();
      onComplete();
    }
    if (mDialog != null) {
      mDialog.show();
    } else {
      UploadDialogUtils.show(mContext);
    }
  }

  @Override public void onComplete() {
    if (mDialog != null) {
      mDialog.dismiss();
    } else {
      UploadDialogUtils.dismiss();
    }
  }

  @Override public void onError(Throwable e) {
    if (mDialog != null) {
      mDialog.dismiss();
    } else {
      UploadDialogUtils.dismiss();
    }
    if (e instanceof SocketTimeoutException) {
      Toast.makeText(mContext, R.string.net_request_time_out, Toast.LENGTH_SHORT).show();
    } else if (e instanceof ConnectException) {
      Toast.makeText(mContext, R.string.net_error_tips, Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
      Log.i(TAG, e.getMessage(), e);
    }
  }

  @Override public void onNext(T t) {
  }
}
