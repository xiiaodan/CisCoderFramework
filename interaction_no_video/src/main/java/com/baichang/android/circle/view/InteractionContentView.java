package com.baichang.android.circle.view;


import android.content.Context;
import com.baichang.android.common.IBaseView;
import com.baichang.android.circle.entity.InteractionListData;

/**
 * Created by iCong on 2017/3/20.
 */

public interface InteractionContentView extends IBaseView {

  void gotoDetail(InteractionListData id);

  void gotoInfo(boolean isOneself, String userId);

  int getTypeId();

  String getUserId();

  Context getContext();
}
