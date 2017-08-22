package com.example.yjn.facepass.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.yjn.facepass.R;
import com.example.yjn.facepass.utils.SPUtils;
import com.example.yjn.facepass.widget.CameraPreview;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * 拍摄用户注册照片
 */
public class UserfaceRegister extends AppCompatActivity {
    private Button mButton5;
    private Camera cm1;
    private FrameLayout mflayout5;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {

        //在主线程里请求访问网络
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
                .build());

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_userface);
        mButton5 = (Button) findViewById(R.id.button5);
        mflayout5 = (FrameLayout) findViewById(R.id.flayout5);
        cm1 = getCameraInstance();
        CameraPreview preview = new CameraPreview(this, cm1);
        mflayout5.addView(preview);
        boolean hascamera = checkCameraHardware(this);
        if (hascamera) {
            Toast.makeText(this, "请务必保持平稳，不要抖动镜头", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "未检测到摄像头", Toast.LENGTH_SHORT).show();
        }
        mButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //等待画面
                ProgressDialog progDialog = null;
                progDialog = new ProgressDialog(UserfaceRegister.this);
                progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDialog.setIndeterminate(false);
                progDialog.setCancelable(false);
                progDialog.setMessage("正在注册中，请稍候...");
                progDialog.show();

                final ProgressDialog finalProgDialog = progDialog;
                cm1.takePicture(null, null, (data, camera) -> {
                    try {
                        File file = new File(Environment.getExternalStorageDirectory(), "test0");
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(data);
                        fos.close();
                        Bitmap bMap = BitmapFactory.decodeByteArray(data, 0, data.length);

                        Bitmap bMapRotate;
                        Configuration config = getResources().getConfiguration();
                        if (config.orientation == 1) { // 坚拍
                            Matrix matrix = new Matrix();
                            matrix.reset();
                            matrix.postRotate(270);
                            bMapRotate = Bitmap.createBitmap(bMap, 0, 0,
                                    bMap.getWidth(), bMap.getHeight(),
                                    matrix, true);
                            bMap = bMapRotate;
                        }

                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                        bMap.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩到流中
                        bos.flush();//输出
                        bos.close();//关闭

                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        //将注册的用户存储到本地
                        Intent intent = getIntent();
                        final String name = intent.getStringExtra("Name");
                        SPUtils face_pass = SPUtils.getInstance("face_pass");
                        face_pass.put("name",name); //存用户名
                        face_pass.put("imgPath",Environment.getExternalStorageDirectory()+"/test0"); //用户的照片

                        finalProgDialog.dismiss();

                        Dialog alertDialog = new AlertDialog.Builder(UserfaceRegister.this).setTitle("注册结果：").setMessage("注册成功！请点击确定按钮返回主界面!")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(UserfaceRegister.this, MainActivity.class));
                                        UserfaceRegister.this.finish();
                                    }
                                })
                                .create();
                        alertDialog.show();
                    }
                });//此处camera调用结束

            }//此处是点击事件的结束


        });
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.初始化相机*
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(1);
            c.setDisplayOrientation(90);

        } catch (Exception e) {
        }
        return c;
    }


}

