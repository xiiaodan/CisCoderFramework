package com.baichang.android.circle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.CamcorderProfile;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baichang.android.circle.common.InteractionCommonActivity;
import com.baichang.android.circle.common.InteractionConfig;
import com.baichang.android.circle.common.InteractionFlag.Event;
import com.baichang.android.circle.present.Impl.InteractionPublishImpl;
import com.baichang.android.circle.present.InteractionPublishPresent;
import com.baichang.android.circle.utils.AnimatorUtil;
import com.baichang.android.circle.utils.BoxingPicassoLoader;
import com.baichang.android.circle.video.ERecorderActivityImpl;
import com.baichang.android.circle.video.VideoConfig;
import com.baichang.android.circle.video.exception.NullProfileException;
import com.baichang.android.circle.video.exception.NullRecordTimeException;
import com.baichang.android.circle.video.model.VideoInfo;
import com.baichang.android.circle.view.InteractionPublishView;
import com.baichang.android.common.BaseEventData;
import com.baichang.android.utils.photo.BCPhotoUtil;
import com.bilibili.boxing.Boxing;
import com.bilibili.boxing.BoxingMediaLoader;
import com.bilibili.boxing.loader.IBoxingMediaLoader;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.config.BoxingConfig.Mode;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.bilibili.boxing.model.entity.impl.ImageMedia;
import com.bilibili.boxing_impl.ui.BoxingActivity;
import java.util.ArrayList;
import java.util.logging.Logger;
import mabeijianxi.camera.util.FileUtils;
import org.greenrobot.eventbus.EventBus;

public class InteractionPublishActivity extends InteractionCommonActivity
        implements InteractionPublishView, OnClickListener {

    EditText etTitle;
    EditText etContent;
    RecyclerView rvList;
    TextView tvType;
    TextView tvPublish;
    ImageButton mBack;
    ProgressDialog mProgress;

    private InteractionPublishPresent mPresent;
    private VideoConfig mVideoConfig;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interaction_activity_publish);
        etTitle = (EditText) findViewById(R.id.interaction_publish_et_title);
        etContent = (EditText) findViewById(R.id.interaction_publish_et_content);
        rvList = (RecyclerView) findViewById(R.id.interaction_publish_rv_images);
        tvType = (TextView) findViewById(R.id.interaction_publish_tv_model);
        tvPublish = (TextView) findViewById(R.id.interaction_publish_tv_publish);
        mBack = (ImageButton) findViewById(R.id.back);
        mBack.setOnClickListener(this);
        initDialog();
        initConfig();
        initVideo();
        init();
    }

    private void initDialog() {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            mProgress = new ProgressDialog(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            mProgress = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
        }
        mProgress.setTitle("发表互动");
        mProgress.setCancelable(false);
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setMessage("正在发表...");
    }

    private void initConfig() {
        int drawableRes = InteractionConfig.getInstance().getBackDrawableRes();
        if (drawableRes != -1) {
            mBack.setImageResource(drawableRes);
        }

        int topColor = InteractionConfig.getInstance().getTopBarColor();
        if (topColor != -1) {
            RelativeLayout titleLayout = (RelativeLayout) findViewById(R.id.title);
            titleLayout.setBackgroundResource(topColor);
            TextView tvTitle = (TextView) findViewById(R.id.interaction_publish_tv_title);
            int color = InteractionConfig.getInstance().getPublishTitleColor();
            if (color != -1) {
                tvTitle.setTextColor(ContextCompat.getColor(this, color));
                tvPublish.setTextColor(ContextCompat.getColor(this, color));
            } else {
                if (ContextCompat.getColor(this, topColor) == Color.WHITE) {
                    tvTitle.setTextColor(Color.BLACK);
                    tvPublish.setTextColor(Color.BLACK);
                } else {
                    tvTitle.setTextColor(Color.WHITE);
                    tvPublish.setTextColor(Color.WHITE);
                }
            }
        }
    }

    private void init() {
        //Boxing
        IBoxingMediaLoader loader = new BoxingPicassoLoader();
        BoxingMediaLoader.getInstance().init(loader);
        tvType.setOnClickListener(this);
        findViewById(R.id.interaction_publish_tv_publish).setOnClickListener(this);
        mPresent = new InteractionPublishImpl(this);
        mPresent.attachView(rvList);
    }

    @Override public void showProgressBar() {
        mProgress.show();
    }

    @Override public void hideProgressBar() {
        mProgress.dismiss();
    }

    @Override protected void onStart() {
        super.onStart();
        mPresent.onStart();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        mPresent.onDestroy();
    }

    @Override public void showMsg(String msg) {
        showMessage(msg);
    }

    @Override public void close(String typeId) {
        EventBus.getDefault()
                .post(new BaseEventData<Integer, Integer>(Event.INTERACTION_JUMP_PAGE,
                        Integer.parseInt(typeId)));
        finish();
    }

    @Override public FragmentManager getManager() {
        return getSupportFragmentManager();
    }

    @Override public void selectImages(int num) {
        BoxingConfig config = new BoxingConfig(Mode.MULTI_IMG).withMaxCount(num);
        Boxing.of(config).withIntent(this, BoxingActivity.class).start(this, REQUEST_CODE_BOXING);
    }

    @Override public void setTypeName(String name) {
        tvType.setText(name);
    }

    @Override public Activity getActivity() {
        return this;
    }

    @Override public void takeVideo() {
        if (mVideoConfig != null) {
            mVideoConfig.start(this);
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BOXING && resultCode == RESULT_OK) {
            final ArrayList<BaseMedia> medias = Boxing.getResult(data);
            mPresent.onBindImages(medias);
        } else if (requestCode == REQUEST_CODE_TAKE && resultCode == RESULT_OK) {
            BCPhotoUtil.photoZoomFree(null);
        } else if (requestCode == REQUEST_CODE_CROP && resultCode == RESULT_OK) {
            // 为了兼容Boxing
            BaseMedia media =
                    new ImageMedia(BCPhotoUtil.getPhotoName(), BCPhotoUtil.getPhotoPath());
            mPresent.onBindImages(media);
            BCPhotoUtil.cleanActivity();
        } else if (requestCode == VideoConfig.REQUEST_RECORD_MEDIA && resultCode == RESULT_OK) {
            VideoInfo videoInfo = ERecorderActivityImpl.getVedioInfo(data);
            if (videoInfo == null) {
                Toast.makeText(getActivity(), "视频拍摄失败", Toast.LENGTH_SHORT).show();
                return;
            }

            //压缩后视频地址
            String mVideoPath = videoInfo.getVideoPath();
            //视频截图
            String mVideoPicPath = videoInfo.getPicPath();
            //视频原地址
            String mVideoOriginPath = videoInfo.getOriginVideoPath();
            if (TextUtils.isEmpty(mVideoPath) || TextUtils.isEmpty(mVideoOriginPath)) {
                Toast.makeText(getActivity(), "视频拍摄失败", Toast.LENGTH_SHORT).show();
                return;
            }
            // 为了兼容Boxing
            BaseMedia media = new ImageMedia(mVideoOriginPath, mVideoPicPath);
            mPresent.onBindImages(media);
        }
    }

    @Override public void onClick(View v) {
        int i = v.getId();
        if (i == tvType.getId()) {
            mPresent.selectModel();
        } else if (i == tvPublish.getId()) {
            AnimatorUtil.scale(v);
            mPresent.publish(etTitle.getText().toString(), etContent.getText().toString());
        } else if (i == mBack.getId()) {
            AnimatorUtil.scale(v);
            onBackPressed();
        }
    }

    private void initVideo() {
        VideoConfig.init(this, "");
        try {
            mVideoConfig = VideoConfig.get()
                    .setTime(10 * 1000)
                    .setProfile(CamcorderProfile.QUALITY_480P)
                    .setCompress(false)
                    .check();
        } catch (NullRecordTimeException e) {
            mVideoConfig.setTime(10 * 1000);
        } catch (NullProfileException e) {
            mVideoConfig.setProfile(CamcorderProfile.QUALITY_480P);
        }
    }
}
