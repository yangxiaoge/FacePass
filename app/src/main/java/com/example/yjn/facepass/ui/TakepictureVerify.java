package com.example.yjn.facepass.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.yjn.facepass.R;
import com.example.yjn.facepass.bean.Constant;
import com.example.yjn.facepass.bean.FaceCompare;
import com.example.yjn.facepass.utils.SPUtils;
import com.example.yjn.facepass.widget.CameraPreview;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import top.zibin.luban.Luban;

/**
 * 登录验证
 */
public class TakepictureVerify extends AppCompatActivity {
    private Button mButton1;
    private ImageButton imageButton2;
    private Camera cm;
    private FrameLayout mflayout;
    //等待画面
    ProgressDialog progDialog = null;

    private static final int REQUEST_CODE_CHOOSE = 10;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.activity_takepicture);
        mButton1 = (Button) findViewById(R.id.button1);
        imageButton2 = (ImageButton) findViewById(R.id.imageButton2);
        mflayout = (FrameLayout) findViewById(R.id.flayout);
        Intent intent = getIntent();
        final String username = intent.getStringExtra("UserName");

        cm = getCameraInstance();//创建camera实例
        CameraPreview preview = new CameraPreview(this, cm);//初始化预览
        mflayout.addView(preview);//输出预览到screen
        boolean hascamera = checkCameraHardware(this);
        if (hascamera) {
            Toast.makeText(this, "请务必保持平稳，不要抖动镜头", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "未检测到摄像头", Toast.LENGTH_SHORT).show();
        }
        mButton1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                try {
                    cm.takePicture(null, null, new Camera.PictureCallback() {
                                @Override
                                public void onPictureTaken(byte[] data, Camera camera) {
                                    try {
                                        File file = new File(Environment.getExternalStorageDirectory(), "test1");
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

                                        //
                                        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        File
                                                mfile = new File(Environment.getExternalStorageDirectory(), "test1");
                                        BufferedOutputStream

                                                bos = new BufferedOutputStream(new FileOutputStream(file));
                                        bMap.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩到流中
                                        bos.flush();//输出
                                        bos.close();//关闭
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                    );//camera end
                } catch (Exception e) {
                    // 防止再次点击崩溃
                    e.printStackTrace();
                }
                Dialog alertDialog = new AlertDialog.Builder(TakepictureVerify.this).setTitle("提示信息：").setMessage("点击确定键查看检测结果！")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //开始匹配用户
                                compareFace(Environment.getExternalStorageDirectory() + "/test1");
                            }
                        })
                        .create();
                alertDialog.show();
            }//此处是点击事件的结束


        });

        // 从相册选择
        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Matisse.from(TakepictureVerify.this)
                        .choose(MimeType.ofImage())
                        .theme(R.style.Matisse_Zhihu)
                        .countable(true)
                        .maxSelectable(1) //可以选择几张图片
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .imageEngine(new GlideEngine())
                        .forResult(REQUEST_CODE_CHOOSE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            //Matisse.obtainResult(data); //uri
            //Matisse.obtainPathResult(data); //path
            compareFace(Matisse.obtainPathResult(data).get(0));
        }
    }

    private void compareFace(String imgPath) {
        if (progDialog == null) {
            progDialog = new ProgressDialog(TakepictureVerify.this);
        }
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在验证信息，请稍候...");
        progDialog.show();
        SPUtils face_pass = SPUtils.getInstance("face_pass");
        face_pass.getString("name", ""); //存用户名
        String registerImg = face_pass.getString("imgPath", Environment.getExternalStorageDirectory() + "/test0"); //用户的照片

        // https://console.faceplusplus.com.cn/documents/4887586
        try {
            OkGo.<String>post("https://api-cn.faceplusplus.com/facepp/v3/compare")
                    .tag(this)
                    .params("api_key", Constant.KEY)
                    .params("api_secret", Constant.SECRET)
                    .params("image_file1", Luban.with(this).load(new File(imgPath)).get())//自己拍照的
                    .params("image_file2", Luban.with(this).load(new File(registerImg)).get()) //对比的对象图
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            Log.i("yjn", "成功 response = " + response.body());

                            if (response.body().contains("error_message")){
                                finishThen2Main();
                                Toast.makeText(TakepictureVerify.this, "匹配出错", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            FaceCompare faceCompare = new Gson().fromJson(response.body(), FaceCompare.class);

                            if (faceCompare.getFaces1() != null) {
                                /**
                                 * 一组用于参考的置信度阈值，包含以下三个字段。每个字段的值为一个 [0,100] 的浮点数，小数点后 3 位有效数字。
                                 1e-3：误识率为千分之一的置信度阈值；
                                 1e-4：误识率为万分之一的置信度阈值；
                                 1e-5：误识率为十万分之一的置信度阈值；
                                 如果置信值低于“千分之一”阈值则不建议认为是同一个人；如果置信值超过“十万分之一”阈值，则是同一个人的几率非常高。
                                 */
                                if (faceCompare.getThresholds() == null) {
                                    //如果传入图片但图片中未检测到人脸，则无法进行比对，本字段不返回。
                                    Toast.makeText(TakepictureVerify.this, "未识别到人脸", Toast.LENGTH_SHORT).show();
                                    finishThen2Main();
                                }

                                //不为空
                                if (!TextUtils.isEmpty(faceCompare.getThresholds().get_$1e3() + "")) {
                                    if (faceCompare.getConfidence() < faceCompare.getThresholds().get_$1e3()) {
                                        Toast.makeText(TakepictureVerify.this, "不是同一个人", Toast.LENGTH_SHORT).show();
                                    } else if (faceCompare.getConfidence() > faceCompare.getThresholds().get_$1e5()) {
                                        Toast.makeText(TakepictureVerify.this, "匹配成功", Toast.LENGTH_SHORT).show();

                                        // 验证成功，跳转浏览器， 测试下
                                        Intent intent = new Intent(Intent.ACTION_VIEW);    //为Intent设置Action属性
                                        intent.setData(Uri.parse("http://www.baidu.com")); //为Intent设置DATA属性
                                        startActivity(intent);
                                    }
                                }else {
                                    finishThen2Main();
                                    Toast.makeText(TakepictureVerify.this, "不是同一个人", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // {"time_used": 372, "error_message": "IMAGE_ERROR_UNSUPPORTED_FORMAT: image_file1", "request_id": "1503306112,dd26b29f-c22a-4d7f-98ab-b796febcc578"}
                                // 可能是图片格式不支持，传Gif是不行的
                                Log.e("yjn", "返回成功，但是参数有问题");
                                Toast.makeText(TakepictureVerify.this, "匹配失败", Toast.LENGTH_SHORT).show();
                            }

                            finishThen2Main();
                        }

                        @Override
                        public void onError(Response<String> response) {
                            finishThen2Main();
                            Log.e("yjn", "上传失败");
                            Toast.makeText(TakepictureVerify.this, "匹配出错", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void finishThen2Main() {
        progDialog.dismiss();
        finish();
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.初始化相机*
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(1); // attempt to get a Camera instance
            c.setDisplayOrientation(90);//将摄像头翻转90度
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

}














