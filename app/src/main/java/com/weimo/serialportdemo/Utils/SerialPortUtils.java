package com.weimo.serialportdemo.Utils;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import android_serialport_api.SerialPort;

/**
 * 串口操作类
 *
 * @author weimo
 */
public class SerialPortUtils {
    private final String TAG = SerialPortUtils.class.getSimpleName();
    private volatile static SerialPortUtils instance = null;
    /**
     * 串口对象
     */
    private SerialPort serialPort;
    /**
     * 串口输入流
     */
    private InputStream inputStream;
    /**
     * 串口输出流
     */
    private OutputStream outputStream;
    /**
     * 串口打开标志
     */
    private boolean serialPortStatus = false;
    /**
     * 终止线程标志位
     */
    private boolean threadStatus;
    /**
     * 数据接收监听
     */
    private OnDataReceiveListener onDataReceiveListener;
    private ReadThread readThread;
    private boolean isOpenSuccess = false;


    private SerialPortUtils() {

    }

    public static SerialPortUtils getInstance() {
        if (instance == null) {
            synchronized (SerialPortUtils.class) {
                if (instance == null) {
                    instance = new SerialPortUtils();
                }
            }
        }
        return instance;
    }

    public interface OnDataReceiveListener {
        void onDataReceive(byte[] buffer, int size);
    }

    public void setOnDataReceiveListener(OnDataReceiveListener onDataReceiveListener) {
        this.onDataReceiveListener = onDataReceiveListener;
    }

    /**
     * 打开串口
     * @param path 串口路径
     * @param baudrate 波特率
     */
    public void init(Context context,String path, int baudrate) {
        try {
            serialPort = new SerialPort(new File(path), baudrate, 0);
            serialPortStatus = true;
            threadStatus = false;
            //获取打开的串口中的输入输出流，以便于串口数据的收发
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            //开启线程监控是否有数据的收发
            readThread = new ReadThread();
            readThread.start();
            isOpenSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            isOpenSuccess = false;
            Toast.makeText(context,"打开串口失败：" + e.toString(),Toast.LENGTH_SHORT).show();
        }
        if (isOpenSuccess){
            Toast.makeText(context,"打开串口成功",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 发送数据
     *
     * @param data
     */
    public void sendSerialPort(Context context,String data) {
        data = data.replace(" ", "");
        //Log.e(TAG, "sendSerialPort:" + data);
        if (serialPortStatus) {
            try {
                byte[] sendData = SerialDataUtils.HexToByteArr(data);
                if (sendData.length > 0) {
                    outputStream.write(sendData);
                    //outputStream.write('\n');
                    outputStream.flush();
                    Toast.makeText(context,"发送数据成功",Toast.LENGTH_SHORT).show();
                    //Log.e(TAG, "sendSerialPort success: 发送串口数据成功");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                //Log.e(TAG, "sendSerialPort error:" + e.toString());
                Toast.makeText(context,"发送数据失败：" + e.toString(),Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context,"发送数据失败 ：" + e.toString(),Toast.LENGTH_SHORT).show();
                //Log.e(TAG, "sendSerialPort error:" + e.toString());
            }
        } else {
            Toast.makeText(context,"串口未打开,发送失败",Toast.LENGTH_SHORT).show();
            //Log.e(TAG, "sendSerialPort error: SerialPort is close");
        }
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (serialPortStatus && (!threadStatus) && (!isInterrupted())) {
                int size;
                try {
                    if (inputStream == null) {
                        return;
                    }
                    byte[] buffer = new byte[512];
                    size = inputStream.read(buffer);//该方法读不到数据时，会阻塞在这里
                    if (size > 0) {
                        byte[] buffer2 = new byte[size];
                        System.arraycopy(buffer, 0, buffer2, 0, size);
                        if (onDataReceiveListener != null) {
                            onDataReceiveListener.onDataReceive(buffer2, size);
                        }
                    }
                    Thread.sleep(50);//延时 50 毫秒
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort(Context context,boolean isClose) {
        try {
            if (null != inputStream) {
                inputStream.close();
            }
            if (null != outputStream) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (isClose){
                Toast.makeText(context,"关闭串口失败：" + e.toString(),Toast.LENGTH_SHORT).show();
            }
            //Log.e(TAG, "closeSerialPort error:" + e.toString());
        }
        serialPortStatus = false;
        threadStatus = true;
        if (readThread != null) {
            readThread.interrupt();
        }
        if (serialPort != null) {
            serialPort.close();
            if (isClose){
                Toast.makeText(context,"关闭串口成功",Toast.LENGTH_SHORT).show();
            }
        } else {
            if (isClose){
                Toast.makeText(context,"串口未打开",Toast.LENGTH_SHORT).show();
            }
        }
        //Log.e(TAG, "openSerialPort success: close serial");
    }

}
