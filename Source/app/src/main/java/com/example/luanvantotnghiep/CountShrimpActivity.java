package com.example.luanvantotnghiep;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CountShrimpActivity extends CameraActivity {

    public final String ACTION_USB_PERMISSON = "com.hariharan.arduinousb.USB_PERMISSION";

    UsbDevice device;
    UsbManager usbManager;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    //....................//
    private Switch swMode;
    private boolean brac = true; // có kết nối với serial không
    private int mode = 30;  // kích thước tôm
    private int width = 3, height = 3, sigmaX = -1; //GausianBlur
    private int blockSize = 33; //adaptiveThreshold
    private int countBegin = 1; //giá trị bắt đầu đếm
    private boolean shrimpLarge = true;
    //................//
    private Mat mRgba;
    private Mat mGray;
    private static String LOGTAG = "OpenCV_Log";
    private CameraBridgeViewBase mOpenCvCameraView;
    private TextView tvKetQua, tvShrimp, tvNote;
    private Button btnKetqua, btnBackHome;
    private int take_img=11;
    int number = 0;
    int so = 0;
    int ketqua;

    int ketquaImgPro=0;
    double []  lan={0,0,0,0,0,0,0,0,0,0,0};

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.v(LOGTAG, "OpenCV Loaded");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    //..........................//
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSON)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) {
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallBack);
                            Toast.makeText(context, "Serial connection Opend", Toast.LENGTH_SHORT).show();
                            brac = false;
                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                            Toast.makeText(context, "PORT NOT OPEN", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                        Toast.makeText(context, "PORT IS NULL", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                    Toast.makeText(context, "PERM NOT GRANTED", Toast.LENGTH_SHORT).show();
                }
            }else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                brac = false;
                HashMap<String, UsbDevice> usbDevices   = usbManager.getDeviceList();
                if (!usbDevices.isEmpty()){
                    boolean keep = true;
                    for (Map.Entry<String, UsbDevice>entry:usbDevices.entrySet()){
                        device = entry.getValue();
                        int deviceVID = device.getVendorId();
                        if (deviceVID == 6790 || deviceVID==1659)
                        //if(deviceVID == 0x2341)
                        {
                            PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSON),PendingIntent.FLAG_MUTABLE);
                            usbManager.requestPermission(device, pi);
                            keep = false;
                        }else {
                            connection = null;
                            device = null;
                        }

                        if (!keep)
                            break;
                    }
                }

                //  onClickconectar(BtnIniciar);
            }else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                brac = true;
                Toast.makeText(context, "Serial desconnection", Toast.LENGTH_SHORT).show();
                // onClickdesconectar(BtnDetener);
            }
        };
    };

    UsbSerialInterface.UsbReadCallback mCallBack = (arg0)->{
        String data = null;
        try {
            data = new String(arg0, "UTF-8");
            data.concat("\n");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    };

    private void closeLed() {
        if (brac){      // chưa kết nối

        }else {
            if (take_img == 10) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String string = "O";
                serialPort.write(string.getBytes());
            }
        }
    }

    //...................//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_shrimp);

        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSON);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
        //............//
        swMode = findViewById(R.id.sw_mode);
        tvNote = findViewById(R.id.tv_note);
        tvShrimp = findViewById(R.id.tv_shrimp);
        tvKetQua = findViewById(R.id.ketqua_shrimp);
        btnKetqua = findViewById(R.id.btn_ket_qua_shrimp);
        btnBackHome = findViewById(R.id.back_home_shrimp);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(cvCameraViewListener);
        mOpenCvCameraView.enableFpsMeter();


        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CountShrimpActivity.this, MainActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });


        btnKetqua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (brac){      // chưa kết nối
                    if (take_img == 0) {

                        take_img = 1;

                    } else {
                        take_img = 0;
                    }
                }else {
                    String string = "L";
                    serialPort.write(string.getBytes());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (take_img == 0) {

                        take_img = 1;

                    } else {
                        take_img = 0;
                    }
                }

            }
        });

    }


    public void onClickMode(View view) {
        if (view.getId()==R.id.sw_mode){
            if (swMode.isChecked()){
                mode = 12;
                width = 1;
                height = 3;
                sigmaX = 2;
                blockSize = 37;
                countBegin = 0;
                tvShrimp.setText("Count Shrimp Small");
                shrimpLarge = false;
            }else {
                mode = 30;
                width = 3;
                height = 3;
                sigmaX = -1;
                blockSize = 33;
                countBegin = 1;
                tvShrimp.setText("Count Shrimp Large");
                shrimpLarge = true;
            }
        }
    }

    private void Note(){
        if(take_img == 10){
            if(shrimpLarge) {
                if(ketqua>550){
                    tvNote.setText("Please remove the shrimp");
                }else {
                    tvNote.setText("");
                }

            }else {
                if (ketqua>800){
                    tvNote.setText("Please remove the shrimp");
                }else {
                    tvNote.setText("");
                }
            }
         }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    private CameraBridgeViewBase.CvCameraViewListener2 cvCameraViewListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {

        }

        @Override
        public void onCameraViewStopped() {

        }


        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            mRgba = inputFrame.rgba();
            mGray = inputFrame.gray();

            take_img = take_picture_function_rgb(take_img, mGray);

            Note();

            return mRgba;
        }
    };

    private int take_picture_function_rgb(int take_img, Mat mGray) {
        if (take_img<11){

            imageProcessing();

            lan[take_img] = ketquaImgPro;
            ketqua = (int) Math.round((lan[1] + lan[2] + lan[3] + lan[4] + lan[5] + lan[6] + lan[7] + lan[8]+ lan[9] + lan[10]) / 10);  // ép kiểu từ double sang int và làm tròn lên
            tvKetQua.setText("Amount: "+ketqua);
            Log.d("TAG", "ket qua: " + lan[1]+" "+lan[2]+" "+lan[3]+" "+ lan[4]+" "+lan[5]+" "+lan[6]+" "+ lan[7]+" "+lan[8]+" "+ lan[9] +" "+ lan[10]+" ket qua "+ ketqua);

            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            take_img+=1;
        }

        closeLed();

        return take_img;
    }

    private void imageProcessing(){

        Mat imgResize = new Mat();
        Mat mask = new Mat();

        //thay đổi kích thước ảnh
        Imgproc.resize(mGray, imgResize, new Size(640,480));

        // làm mờ ảnh
        Imgproc.GaussianBlur(imgResize, mask, new Size(width,height), sigmaX);

        Mat mIntermediateMat = new Mat();
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new LinkedList<>();

        contours = new ArrayList<MatOfPoint>();

        // nhị phân hóa
        Imgproc.adaptiveThreshold(mask, mIntermediateMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, blockSize, 16);

        //tìm viền
        Imgproc.findContours(mIntermediateMat, contours, hierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        hierarchy.release();

        number = 0;


        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(contourIdx).toArray());

            //vẽ viền với sấp sĩ
            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.01;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

            MatOfPoint point = new MatOfPoint(approxCurve.toArray());

            //xác định vùng quan tâm
            Rect rect = Imgproc.boundingRect(point);

            double height = rect.height;
            double width = rect.width;

            //tính diện tích bên trong của bao hình
            int area = (int) Imgproc.contourArea(point);

            for (int count=countBegin; count <= area; count +=mode){
                //vẽ hình chữ nhật bao xung quanh vật thể
                Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width,
                        rect.y + rect.height), new Scalar(0, 255, 0, 0), 0);
                number +=1;
            }

        }

        ketquaImgPro = number;

        Log.d("TAG", "ket qua contour: "+contours.size());
        Log.d("TAG", "ket qua number: "+ number);



    }


    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(LOGTAG, "OpenCV not found, Initializing");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

}