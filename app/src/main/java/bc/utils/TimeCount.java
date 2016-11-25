package bc.utils;

import android.os.CountDownTimer;

/**
 * Created by iscod.
 * Time:2016/8/30-16:59.
 */
public class TimeCount extends CountDownTimer {
    private static TimeCount INSTANCE;

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public TimeCount(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    /**
     * @param millisInFuture    总时长 毫秒
     * @param countDownInterval 每次执行减少的时长  毫秒
     * @return
     */
    public static TimeCount create(long millisInFuture, long countDownInterval) {
        synchronized (TimeCount.class) {
            if (INSTANCE == null) {
                INSTANCE = new TimeCount(millisInFuture, countDownInterval);
            }
        }
        return INSTANCE;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (listener == null) return;
        listener.onTick(millisUntilFinished / 1000);//剩余时间
    }

    @Override
    public void onFinish() {
        if (listener == null) return;
        listener.onFinish();                        //计时完毕
    }

    private TimeCountListener listener;

    public TimeCount setListener(TimeCountListener listener) {
        this.listener = listener;
        return this;
    }

    public interface TimeCountListener {
        //void onTick(long millisUntilFinished); //毫秒
        void onTick(long second);                //秒

        void onFinish();
    }

    /**
     * 取消，防止倒计时没有结束 Activity销毁造成的内存泄露
     */
    public void destroy() {
        if (INSTANCE != null) {
            INSTANCE.cancel();
        }
        INSTANCE = null;
    }
}
