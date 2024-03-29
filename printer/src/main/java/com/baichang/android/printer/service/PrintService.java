package com.baichang.android.printer.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.companion.CompanionDeviceManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.res.TypedArrayUtils;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.baichang.android.printer.gpsdk.DeviceConnFactoryManager;
import com.baichang.android.printer.gpsdk.PrinterCommand;
import com.baichang.android.printer.gpsdk.ThreadFactoryBuilder;
import com.baichang.android.printer.gpsdk.ThreadPool;
import com.baichang.android.printer.utils.BluetoothUtil;
import com.gprinter.command.EscCommand;
import com.gprinter.command.LabelCommand;
import java.io.Serializable;
import java.util.Vector;

import static com.baichang.android.printer.gpsdk.DeviceConnFactoryManager.CONN_STATE_FAILED;

/**
 * Created by iCong on BLUETOOTH_PORT1/BLUETOOTH_PORT3/2BLUETOOTH_PORT18.
 */

public class PrintService extends Service {

    private static final String TAG = PrintService.class.getSimpleName();

    // 打印机蓝牙地址
    public static final String BLUETOOTH_ADDRESS = "bluetooth_address";
    // 连接模式 wifi or 蓝牙
    public static final String CONNECT_TYPE = "connect_type";
    // 打印数据
    public static final String PRINT_DATA = "print_data";
    // 打印模式
    public static final String PRINT_MODEL = "print_model";
    private String mBluetoothAddress = "";
    private String mWIFIAddress = "";
    // 默认蓝牙端口
    private static final int BLUETOOTH_PORT = 0;
    // Wifi 端口(默认)
    private static final int WIFI_PORT = 9100;
    // 蓝牙适配器
    private BluetoothUtil mBluetoothUtil;
    // 打印机连接参数
    private DeviceConnFactoryManager.Build mBuild;
    // 打印机管理
    private DeviceConnFactoryManager[] mDeviceManagers;
    private ThreadFactoryBuilder mThreadFactoryBuilder;
    // handle
    public static final int MESSAGE_CONNECT = 1;
    /**
     * 使用打印机指令错误
     */
    private static final int PRINTER_COMMAND_ERROR = 0x008;
    /**
     * 打印机断开连接
     */
    public static final int CONN_STATE_DISCONN = 0x007;
    // 打印内容
    private PrintData mPrintData;
    // 打印模式 默认正常
    private MODEL model = MODEL.NORMAL;
    // 打印机连接 模式(默认蓝牙)
    private TYPE type = TYPE.BLUE;

    public enum MODEL implements Serializable {
        NORMAL, TEST
    }

    public enum TYPE implements Serializable {
        BLUE, WIFI
    }

    @Nullable @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public void onCreate() {
        mThreadFactoryBuilder = new ThreadFactoryBuilder("PrintService");
        IntentFilter filter = new IntentFilter(DeviceConnFactoryManager.ACTION_CONN_STATE);
        registerReceiver(mDeviceStateReceiver, filter);
        mBluetoothUtil = new BluetoothUtil(getContext(), false);
        super.onCreate();
    }

    @Override public void onDestroy() {
        unregisterReceiver(mDeviceStateReceiver);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.hasExtra(CONNECT_TYPE)) {
                type = (TYPE) intent.getSerializableExtra(CONNECT_TYPE);
            }
            if (type == TYPE.BLUE) {
                mBluetoothAddress = intent.getStringExtra(BLUETOOTH_ADDRESS);
            } else {
                mWIFIAddress = getIP();
            }

            if (intent.hasExtra(PRINT_MODEL)) {
                model = (MODEL) intent.getSerializableExtra(PRINT_MODEL);
            }
            if (model != MODEL.TEST && intent.hasExtra(PRINT_DATA)) {
                mPrintData = intent.getParcelableExtra(PRINT_DATA);
            }
        }
        if (type == TYPE.BLUE) {
            mBluetoothUtil.setListener(new BluetoothUtil.BluetoothUtlListener() {
                @Override public void opened() {
                    if (model != MODEL.TEST && (mPrintData == null
                            || mPrintData.getDatas() == null
                            || mPrintData.getDatas().isEmpty())) {
                        // 打印数据为空
                    } else {
                        // 构建打印蓝牙参数
                        if (mBuild == null) {
                            mBuild = new DeviceConnFactoryManager.Build(getApplicationContext());
                            mBuild.setMacAddress(mBluetoothAddress);
                            mBuild.build();
                            mDeviceManagers =
                                    DeviceConnFactoryManager.getDeviceConnFactoryManagers();
                        }
                        // 开始连接打印机
                        if (mDeviceManagers[BLUETOOTH_PORT].getConnState()) {   // 已连接
                            print();
                        } else {
                            mHandler.sendEmptyMessage(MESSAGE_CONNECT);
                        }
                    }
                }

                @Override public void noOpen() {
                    Toast.makeText(getContext(), "请开启设备蓝牙", Toast.LENGTH_LONG).show();
                }
            });
            mBluetoothUtil.openBluetooth();
        } else {
            if (TextUtils.isEmpty(mWIFIAddress)) {
                Toast.makeText(getContext(), "获取WIfi地址失败，请连接Wifi重试", Toast.LENGTH_LONG).show();
            } else if (model != MODEL.TEST && (mPrintData == null
                    || mPrintData.getDatas() == null
                    || mPrintData.getDatas().isEmpty())) {
                // 打印数据为空
            } else {
                // 构建打印蓝牙参数
                if (mBuild == null) {
                    mBuild = new DeviceConnFactoryManager.Build(getApplicationContext());
                    mBuild.setConnMethod(DeviceConnFactoryManager.CONN_METHOD.WIFI);
                    mBuild.setIp(mWIFIAddress);
                    mBuild.setPort(WIFI_PORT);
                    mBuild.build();
                    mDeviceManagers = DeviceConnFactoryManager.getDeviceConnFactoryManagers();
                }
                // 开始连接打印机
                if (mDeviceManagers[WIFI_PORT].getConnState()) {   // 已连接
                    print();
                } else {
                    mHandler.sendEmptyMessage(MESSAGE_CONNECT);
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("HandlerLeak") Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_CONNECT:
                    if (mDeviceManagers[type == TYPE.BLUE ? BLUETOOTH_PORT : WIFI_PORT] == null) {
                        Toast.makeText(getContext(), "请先设置打印机", Toast.LENGTH_LONG).show();
                    } else {
                        ThreadPool.getInstantiation()
                                .addTask(mThreadFactoryBuilder.newThread(new Runnable() {
                                    @Override public void run() {
                                        mDeviceManagers[type == TYPE.BLUE ? BLUETOOTH_PORT
                                                : WIFI_PORT].openPort();
                                    }
                                }));
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private BroadcastReceiver mDeviceStateReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                //Usb连接断开、蓝牙连接断开广播
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    mHandler.obtainMessage(CONN_STATE_DISCONN).sendToTarget();
                    break;
                case DeviceConnFactoryManager.ACTION_CONN_STATE:
                    int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                    switch (state) {
                        case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
                            Log.i(TAG, "连接状态：未连接");
                            Toast.makeText(getContext(), "打印机未连接，请检查打印机是否开启", Toast.LENGTH_LONG)
                                    .show();
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                            Log.i(TAG, "连接中...");
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                            Log.i(TAG, "连接状态：已连接");
                            // 连接成功，开始打印
                            print();
                            break;
                        case CONN_STATE_FAILED:
                            Log.e(TAG, "连接失败！");
                            Toast.makeText(getContext(), "打印机连接失败", Toast.LENGTH_LONG).show();
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 开始打印
     */
    private void print() {
        if (mDeviceManagers[type == TYPE.BLUE ? BLUETOOTH_PORT : WIFI_PORT] == null
                || !mDeviceManagers[type == TYPE.BLUE ? BLUETOOTH_PORT
                : WIFI_PORT].getConnState()) {
            Toast.makeText(getContext(), "请先设置打印机", Toast.LENGTH_LONG).show();
            return;
        }
        ThreadPool.getInstantiation().addTask(new Runnable() {
            @Override public void run() {
                if (mDeviceManagers[type == TYPE.BLUE ? BLUETOOTH_PORT
                        : WIFI_PORT].getCurrentPrinterCommand() == PrinterCommand.ESC) {
                    if (model == MODEL.TEST) {
                        mDeviceManagers[type == TYPE.BLUE ? BLUETOOTH_PORT
                                : WIFI_PORT].sendDataImmediately(getTestData());
                    } else {
                        mDeviceManagers[type == TYPE.BLUE ? BLUETOOTH_PORT
                                : WIFI_PORT].sendDataImmediately(mPrintData.getDatas());
                    }
                } else {
                    mHandler.obtainMessage(PRINTER_COMMAND_ERROR).sendToTarget();
                }
            }
        });
    }

    /**
     * 测试
     */
    private Vector<Byte> getTestData() {
        EscCommand esc = new EscCommand();
        esc.addInitializePrinter();
        esc.addPrintAndFeedLines((byte) 3);
        // 设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        // 设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON,
                EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        // 打印文字
        esc.addText("打印测试\n");
        esc.addPrintAndLineFeed();
        // 开钱箱
        esc.addGeneratePlus(LabelCommand.FOOT.F5, (byte) 255, (byte) 255);
        esc.addPrintAndFeedLines((byte) 8);
        // 加入查询打印机状态，打印完成后，此时会接收到GpCom.ACTION_DEVICE_STATUS广播
        esc.addQueryPrinterStatus();
        return esc.getCommand();
    }

    private Context getContext() {
        return this;
    }

    public String getIP() {
        String ip = "";
        WifiManager manager = (WifiManager) PrintService.this.getApplication()
                .getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (manager != null) {
            int i = manager.getConnectionInfo().getIpAddress();
            ip = (i & 0xff) + "." + (i >> 8 & 0xff) + "." + (i >> 16 & 0xff) + "." + (i >> 24
                    & 0xff);
        }
        return ip;
    }
}
