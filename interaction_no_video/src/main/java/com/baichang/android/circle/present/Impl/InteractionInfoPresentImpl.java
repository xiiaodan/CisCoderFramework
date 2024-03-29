package com.baichang.android.circle.present.Impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.baichang.android.circle.InteractionContentFragment;
import com.baichang.android.circle.InteractionReplyFragment;
import com.baichang.android.circle.R;
import com.baichang.android.circle.common.InteractionAPIConstants;
import com.baichang.android.circle.common.InteractionConfig;
import com.baichang.android.circle.common.InteractionConfig.InteractionListener;
import com.baichang.android.circle.common.InteractionFlag.Event;
import com.baichang.android.circle.entity.InteractionNumberData;
import com.baichang.android.circle.entity.InteractionUserInfo;
import com.baichang.android.circle.model.Impl.InteractInteractionImpl;
import com.baichang.android.circle.model.InteractInteraction;
import com.baichang.android.circle.present.InteractionInfoPresent;
import com.baichang.android.circle.utils.UIUtil;
import com.baichang.android.circle.view.InteractionMeView;
import com.baichang.android.common.BaseEventData;
import com.baichang.android.common.IBaseInteraction.BaseListener;
import com.baichang.android.config.Configuration;
import com.baichang.android.config.ConfigurationImpl;
import com.baichang.android.widget.magicIndicator.MagicIndicator;
import com.baichang.android.widget.magicIndicator.ViewPagerHelper;
import com.baichang.android.widget.magicIndicator.buildins.commonnavigator.CommonNavigator;
import com.baichang.android.widget.magicIndicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import com.baichang.android.widget.magicIndicator.buildins.commonnavigator.abs.IPagerIndicator;
import com.baichang.android.widget.magicIndicator.buildins.commonnavigator.abs.IPagerTitleView;
import com.baichang.android.widget.magicIndicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import com.baichang.android.widget.magicIndicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import com.baichang.android.widget.magicIndicator.buildins.commonnavigator.titles.SimplePagerTitleView;
import com.baichang.android.widget.magicIndicator.buildins.commonnavigator.titles.badge.BadgePagerTitleView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import jp.wasabeef.glide.transformations.BlurTransformation;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by iCong on 2017/3/28.
 */

public class InteractionInfoPresentImpl implements
    InteractionInfoPresent, BaseListener<InteractionNumberData> {

  private InteractionMeView mView;
  private InfoFragmentAdapter mAdapter;
  private InteractInteraction mInteraction;
  private boolean isOneself;
  private String mUserId;
  private int dynamicNumbers = 0;
  private int replayNumbers = 0;
  private int collectNumbers = 0;
  private boolean isBusiness = false;
  private NavigatorAdapter mNavigator;

  public InteractionInfoPresentImpl(InteractionMeView view) {
    mView = view;
    mInteraction = new InteractInteractionImpl();
    EventBus.getDefault().register(this);
  }


  @Override
  public void onStart() {
    mInteraction.getNumbers(mUserId, this);
    mInteraction.getUserInfo(mUserId, userInfoListener);
  }

  @Override
  public void onDestroy() {
    mView = null;
    EventBus.getDefault().unregister(this);
  }

  @Override
  public void attachView(ViewPager viewPager, MagicIndicator indicator) {
    viewPager.setOffscreenPageLimit(3);
    viewPager.setAdapter(mAdapter);
    mNavigator = new NavigatorAdapter(viewPager);
    CommonNavigator mCommonNavigator = new CommonNavigator(mView.getContext());
    mCommonNavigator.setAdjustMode(true);
    mCommonNavigator.setAdapter(mNavigator);
    indicator.setNavigator(mCommonNavigator);
    ViewPagerHelper.bind(indicator, viewPager);
  }

  @Override
  public void setIsOneself(boolean isOneself, String userId) {
    this.isOneself = isOneself;
    mUserId = userId;
    mAdapter = new InfoFragmentAdapter(mView.getManager());
  }

  @Override
  public void jumpBusiness() {
    InteractionListener listener = InteractionConfig.getInstance().getListener();
    if (listener != null && isBusiness) {
      listener.businessClick(mUserId);
    }
  }

  @Override
  public void success(InteractionNumberData data) {
    dynamicNumbers = data.trendsNum;
    replayNumbers = data.replayNum;
    collectNumbers = data.collectionNum;
    mNavigator.notifyDataSetChanged();
  }

  @Override
  public void error(String error) {
    mView.showMsg(error);
  }


  private class InfoFragmentAdapter extends FragmentPagerAdapter {

    InfoFragmentAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
        case DYNAMIC:
          if (isOneself) {
            return InteractionContentFragment.newInstance(mUserId, DYNAMIC);
          } else {
            return InteractionContentFragment.newInstance(mUserId, NORMAL);
          }
        case REPORT:
          return InteractionReplyFragment.newInstance(mUserId);
        case COLLECT:
          return InteractionContentFragment.newInstance(mUserId, COLLECT);
        default:
          return null;
      }
    }

    @Override
    public int getCount() {
      if (isOneself) {
        return 3;
      } else {
        return 2;
      }
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void event(BaseEventData data) {
    if (data.key == Event.INTERACTION_LIST_DELETE ||
        data.key == Event.INTERACTION_LIST_CANCEL_COLLECT) {
      mInteraction.getNumbers(mUserId, this);
    }
  }

  private class NavigatorAdapter extends CommonNavigatorAdapter {

    ViewPager viewPager;
    Context context;

    NavigatorAdapter(ViewPager viewPager) {
      this.viewPager = viewPager;
      context = viewPager.getContext();
    }

    @Override
    public int getCount() {
      if (isOneself) {
        return 3;
      } else {
        return 2;
      }
    }

    @Override
    public IPagerTitleView getTitleView(Context context, final int index) {
      BadgePagerTitleView badgePagerTitleView = new BadgePagerTitleView(context);
      SimplePagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
      simplePagerTitleView.setNormalColor(Color.GRAY);

      int textColor = InteractionConfig.getInstance().getTextFontColor();
      if (textColor != -1) {
        simplePagerTitleView.setSelectedColor(ContextCompat.getColor(context, textColor));
      } else {
        simplePagerTitleView.setSelectedColor(ContextCompat.getColor(context, R.color.interaction_text_font));
      }
      switch (index) {
        case DYNAMIC:
          simplePagerTitleView
              .setText(getString(R.string.str_interaction_info_dynamic, dynamicNumbers));
          break;
        case REPORT:
          simplePagerTitleView
              .setText(getString(R.string.str_interaction_info_report, replayNumbers));
          // 是否显示小红点
//          if (isOneself) {
//            ImageView badge = new ImageView(context);
//            badge.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.interaction_ic_red_dot));
//            badgePagerTitleView.setBadgeView(badge);
//            badgePagerTitleView.setAutoCancelBadge(true);
//            badgePagerTitleView.setXBadgeRule(new BadgeRule(BadgeAnchor.CONTENT_RIGHT, -UIUtil.dip2px(context, 6)));
//            badgePagerTitleView.setYBadgeRule(new BadgeRule(BadgeAnchor.CONTENT_TOP, 0));
//          }
          break;
        case COLLECT:
          simplePagerTitleView
              .setText(getString(R.string.str_interaction_info_collect, collectNumbers));
          break;
      }
      simplePagerTitleView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          viewPager.setCurrentItem(index);
        }
      });
      simplePagerTitleView.setTextSize(14);
      badgePagerTitleView.setInnerPagerTitleView(simplePagerTitleView);
      return badgePagerTitleView;
    }

    @Override
    public IPagerIndicator getIndicator(Context context) {
      LinePagerIndicator linePagerIndicator = new LinePagerIndicator(context);
      linePagerIndicator.setMode(LinePagerIndicator.MODE_EXACTLY);
      linePagerIndicator.setLineWidth(UIUtil.dip2px(context, 36));
      linePagerIndicator.setLineHeight(UIUtil.dip2px(context, 1));

      int textColor = InteractionConfig.getInstance().getTextFontColor();
      if (textColor != -1) {
        linePagerIndicator.setColors(ContextCompat.getColor(context, textColor));
      } else {
        linePagerIndicator.setColors(ContextCompat.getColor(context, R.color.interaction_text_font));
      }
      return linePagerIndicator;
    }

    String getString(int resId, int number) {
      return context.getString(resId) + "(" + number + ")";
    }
  }

  private BaseListener<InteractionUserInfo> userInfoListener = new BaseListener<InteractionUserInfo>() {
    @Override
    public void success(InteractionUserInfo userInfo) {
      Glide.with(mView.getContext())
          .load(TextUtils.isEmpty(userInfo.headPic) ? R.mipmap.interaction_icon_default
              : ConfigurationImpl.get().getApiLoadImage() + userInfo.headPic)
          .into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
              mView.setAvatar(resource);
            }
          });
      Glide.with(mView.getContext())
          .load(TextUtils.isEmpty(userInfo.headPic) ? R.mipmap.interaction_icon_default
              : ConfigurationImpl.get().getApiLoadImage() + userInfo.headPic)
          .asBitmap()
          .transform(new BlurTransformation(mView.getContext()))
          .into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
              mView.setBackground(new BitmapDrawable(mView.getContext().getResources(), resource));
            }
          });
      mView.setUserName(userInfo.name);
      // 1 汽修厂
      isBusiness = userInfo.type != 1;
      // 是否显示联系商家
      mView.setBusinessVisitState(userInfo.type != 1);
    }

    @Override
    public void error(String error) {
      mView.showMsg(error);
    }
  };
}
