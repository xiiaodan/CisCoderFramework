package com.baichang.android.circle.video;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import com.baichang.android.circle.video.global.CachePath;
import com.baichang.android.circle.video.model.VideoInfo;
import com.baichang.android.circle.video.util.FileUtils;
import com.baichang.android.circle.video.util.ImageUtils;
import java.io.File;
import java.util.List;
import mabeijianxi.camera.LocalMediaCompress;
import mabeijianxi.camera.model.AutoVBRMode;
import mabeijianxi.camera.model.LocalMediaConfig;
import mabeijianxi.camera.model.OnlyCompressOverBean;
import mabeijianxi.camera.util.DeviceUtils;
import mabeijianxi.camera.util.Log;

/**
 * 当前作者: Fyj<br>
 * 时间: 2017/4/28<br>
 * 邮箱: f279259625@gmail.com<br>
 * 修改次数: <br>
 * 描述:
 */

public class ERecorderActivityImpl {

    /**
     * resultkey 视频信息
     */
    private static final String MEDIA_VIDEO_INFO = "media_video_info";
    /**
     * resultkey 原始视频文件地址
     */
    private static final String MEDIA_ORIGIN_PATH = "media_origin_path";

    /**
     * resultkey 压缩视频文件地址
     */
    private static final String MEDIA_PATH = "media_path";
    /**
     * resultkey 视频缩略图地址
     */
    private static final String MEDIA_THUMBLE_PATH = "media_thumble_path";

    /**
     * param 录制时间
     */
    private static final String PARAM_RECORD_TIME = "record_time";
    /**
     * param 录制配置文件
     */
    private static final String PARAM_RECORD_PROFILE = "record_profile";
    /**
     * param 是否压缩
     */
    private static final String PARAM_RECORD_ISCOMPRESS = "record_iscompress";
    /**
     * param 压缩模式
     */
    private static final String PARAM_RECORD_COMPRESSMODE = "record_compressmode";

    /**
     * 获取意图
     *
     * @param context 上下文
     * @param recordTime 录制时间
     * @param profile 配置
     * @param isCompress 是否压缩
     * @param compressMode 压缩模式
     * @return 意图
     */
    static Intent getMediaIntent(Context context, int recordTime, int profile, boolean isCompress,
            String compressMode) {
        Intent i = new Intent(context, ERecorderActivity.class);
        i.putExtra(PARAM_RECORD_TIME, recordTime);
        i.putExtra(PARAM_RECORD_PROFILE, profile);
        i.putExtra(PARAM_RECORD_ISCOMPRESS, isCompress);
        i.putExtra(PARAM_RECORD_COMPRESSMODE, compressMode);
        return i;
    }

    /**
     * 设置返回值并关闭页面
     *
     * @param activity activity
     * @param videoPath 压缩地址
     * @param thumblePath 视频截图地址
     * @param originPath 当前地址
     */
    static void setResultAndFinish(Activity activity, String videoPath, String thumblePath,
            String originPath) {
        Intent i = new Intent();
        i.putExtra(MEDIA_PATH, TextUtils.isEmpty(videoPath) ? "" : videoPath);
        i.putExtra(MEDIA_THUMBLE_PATH, TextUtils.isEmpty(thumblePath) ? "" : thumblePath);
        i.putExtra(MEDIA_ORIGIN_PATH, TextUtils.isEmpty(originPath) ? "" : originPath);
        activity.setResult(Activity.RESULT_OK, i);
        activity.finish();
    }

    public static void setResultAndFinish(Activity activity, VideoInfo videoInfo) {
        Intent i = new Intent();
        i.putExtra(MEDIA_VIDEO_INFO, videoInfo);
        activity.setResult(Activity.RESULT_OK, i);
        activity.finish();
    }

    /**
     * 销毁持有
     */
    static void onDestory() {

    }

    /**
     * 获取录制时间
     * 默认10s
     *
     * @param i intent
     * @return 时间
     */
    static int getRecordTime(Intent i) {
        return i.getIntExtra(PARAM_RECORD_TIME, 10 * 1000);
    }

    /**
     * 获取配置文件
     * 默认480P
     *
     * @param i intent
     * @return 配置
     */
    static int getRecordProfile(Intent i) {
        return i.getIntExtra(PARAM_RECORD_PROFILE, CamcorderProfile.QUALITY_480P);
    }

    /**
     * 获取是否压缩
     * 默认压缩
     *
     * @param i intent
     * @return 是否压缩
     */
    static boolean getRecordIsCompress(Intent i) {
        return i.getBooleanExtra(PARAM_RECORD_ISCOMPRESS, true);
    }

    /**
     * 获取压缩模式
     * 默认中等压缩
     *
     * @param i intent
     * @return 压缩模式
     */
    static String getRecordCompressMode(Intent i) {
        String param = i.getStringExtra(PARAM_RECORD_COMPRESSMODE);
        return (TextUtils.isEmpty(param) ? "medium" : param);
    }

    /**
     * 获取拍摄视频信息
     *
     * @param i intent
     * @return videoinfo
     */
    public static VideoInfo getVedioInfo(Intent i) {
        if (i == null || !i.hasExtra(MEDIA_VIDEO_INFO)) {
            return null;
        }
        return i.getParcelableExtra(MEDIA_VIDEO_INFO);
    }

    /**
     * 放大动画
     *
     * @param v view
     * @param listener listener
     */
    static void scaleBigAnim(View v, Animation.AnimationListener listener) {
        ScaleAnimation an2 =
                new ScaleAnimation(0.8f, 1f, 0.8f, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
        an2.setDuration(200);
        an2.setFillAfter(true);
        v.startAnimation(an2);
        an2.setAnimationListener(listener);
    }

    /**
     * 缩小动画
     *
     * @param v view
     */
    static void scaleSmallAnim(View v) {
        ScaleAnimation an1 =
                new ScaleAnimation(1f, 0.8f, 1f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
        an1.setDuration(200);
        an1.setFillAfter(true);
        v.startAnimation(an1);
    }

    /**
     * 判断文件是否存在
     * 间接判断有没有录制权限
     *
     * @param path 地址
     * @return 存在
     */
    static boolean judgeFile(String path) {
        if (!FileUtils.isFileExists(path)) {
            return true;
        } else {
            long size = 0;
            try {
                size = FileUtils.getFileSize(new File(path));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return size == 0;
        }
    }

    static OnlyCompressOverBean compressVideo(String path, String mCompressMode) {
        LocalMediaConfig.Buidler buidler = new LocalMediaConfig.Buidler();
        AutoVBRMode autoVBRMode = new AutoVBRMode();
        autoVBRMode.setVelocity(mCompressMode);
        final LocalMediaConfig config = buidler.setVideoPath(path)
                .captureThumbnailsTime(1)
                .doH264Compress(autoVBRMode)
                .build();

        return new LocalMediaCompress(config).startCompress();
    }

    /**
     * 生成dialog
     *
     * @param context context
     * @return dialog
     */
    static ProgressDialog getCreateVideoDialog(Context context) {
        ProgressDialog dialog;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = new ProgressDialog(context, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            dialog = new ProgressDialog(context, ProgressDialog.THEME_HOLO_LIGHT);
        }
        dialog.setTitle("生成小视频");
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("生成小视频中,请稍等...");
        return dialog;
    }

    /**
     * 设置相机参数
     *
     * @param mCamera canmea
     */
    static void setCameraParam(Camera mCamera) {
        try {
            Camera.Parameters mParameters = mCamera.getParameters();
            String mode = ERecorderActivityImpl.getAutoFocusMode(mParameters);
            if (!TextUtils.isEmpty(mode)) {
                mParameters.setFocusMode(mode);
            }

            if (ERecorderActivityImpl.isSupported(mParameters.getSupportedWhiteBalance(), "auto")) {
                mParameters.setWhiteBalance("auto");
            }

            if ("true".equals(mParameters.get("video-stabilization-supported"))) {
                mParameters.set("video-stabilization", "true");
            }

            if (!DeviceUtils.isDevice("GT-N7100", "GT-I9308", "GT-I9300")) {
                mParameters.set("cam_mode", 1);
                mParameters.set("cam-mode", 1);
            }
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int orientation = 90;
        mCamera.setDisplayOrientation(orientation);
    }

    /**
     * 自动对焦
     *
     * @param mParameters 参数
     * @return 参数
     */
    private static String getAutoFocusMode(Camera.Parameters mParameters) {
        if (mParameters != null) {
            //持续对焦是指当场景发生变化时，相机会主动去调节焦距来达到被拍摄的物体始终是清晰的状态。
            List<String> focusModes = mParameters.getSupportedFocusModes();
            if ((Build.MODEL.startsWith("GT-I950")
                    || Build.MODEL.endsWith("SCH-I959")
                    || Build.MODEL.endsWith("MEIZU MX3")) && isSupported(focusModes,
                    "continuous-picture")) {
                return "continuous-picture";
            } else if (isSupported(focusModes, "continuous-video")) {
                return "continuous-video";
            } else if (isSupported(focusModes, "auto")) {
                return "auto";
            }
        }
        return null;
    }

    /**
     * 检测是否支持指定特性
     *
     * @param list list
     * @param key key
     * @return 支持
     */
    private static boolean isSupported(List<String> list, String key) {
        return list != null && list.contains(key);
    }

    /**
     * 获取视频截图
     *
     * @param context 上下文
     * @param originPath 原始地址
     * @return 获取截图
     */
    static String getVideoThumble(Context context, String originPath) {
        Bitmap videoThumbnail = ImageUtils.getVideoThumbnail(originPath, 200, 200,
                MediaStore.Images.Thumbnails.MICRO_KIND);

        return ImageUtils.saveBitmap2File(videoThumbnail, CachePath.getMediaCachePath(context),
                System.currentTimeMillis() + "_thumbnail.jpg");
    }

    /**
     * 打开摄像头
     */
    static Camera openCamera() {
        Camera mCamera = null;
        try {
            if (Camera.getNumberOfCameras() == 2) {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            } else {
                mCamera = Camera.open();
            }
        } catch (Exception e) {
            Log.e(e.getLocalizedMessage());
        }

        return mCamera;
    }
}
