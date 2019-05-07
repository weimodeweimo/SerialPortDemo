package com.weimo.serialportdemo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.weimo.serialportdemo.Utils.SerialDataUtils;
import com.weimo.serialportdemo.Utils.SerialPortUtils;

import android_serialport_api.SerialPortFinder;

/**
 * @author weimo
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private SerialPortUtils serialPortUtils;
    private EditText etPath;
    private EditText etBaudrate;
    private Button btOpen;
    private Button btSend;
    private Button btClose;
    private Button btFinish;
    private EditText etSendData;
    /**
     * 串口路径
     */
    private String path;

    private String baudrateStr;
    /**
     * 发送的数据
     */
    private String sendData;
    /**
     * 波特率
     */
    private int baudrate;
    private AlertDialog.Builder alertDialog;
    private SerialPortFinder serialPortFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        etPath = findViewById(R.id.etPath);
        etBaudrate = findViewById(R.id.etBaudrate);
        btOpen = findViewById(R.id.btOpen);
        btSend = findViewById(R.id.btSend);
        btClose = findViewById(R.id.btClose);
        btFinish = findViewById(R.id.btFinish);
        etSendData = findViewById(R.id.etSendData);
    }

    private void initData() {
        /*serialPortFinder = new SerialPortFinder();
        String[] strings = serialPortFinder.getAllDevices();*/
        btOpen.setOnClickListener(this);
        btSend.setOnClickListener(this);
        btClose.setOnClickListener(this);
        btFinish.setOnClickListener(this);
        serialPortUtils = SerialPortUtils.getInstance();
        alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer, int size) {
                final String receiveStr = SerialDataUtils.ByteArrToHex(buffer);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(receiveStr)) {

                        } else {
                            alertDialog.setTitle("接收到的数据：").setMessage(receiveStr).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialog.create().show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btOpen:
                path = etPath.getText().toString().trim();
                baudrateStr = etBaudrate.getText().toString().trim();
                if (TextUtils.isEmpty(path)) {
                    Toast.makeText(this, "请输入串口路径", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(baudrateStr)) {
                    Toast.makeText(this, "请输入波特率", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    baudrate = Integer.parseInt(baudrateStr);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "请输入数字类型的波特率", Toast.LENGTH_SHORT).show();
                    return;
                }
                serialPortUtils.init(this, path, baudrate);
                break;
            case R.id.btSend:
                sendData = etSendData.getText().toString().trim();
                if (TextUtils.isEmpty(sendData)) {
                    Toast.makeText(this, "发送的数据不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                serialPortUtils.sendSerialPort(this, sendData);
                break;
            case R.id.btClose:
                serialPortUtils.closeSerialPort(this,true);
                break;
            case R.id.btFinish:
                serialPortUtils.closeSerialPort(this,false);
                finish();
                break;
            default:
                break;
        }
    }
}
