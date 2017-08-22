package com.example.yjn.facepass.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yjn.facepass.R;
import com.example.yjn.facepass.runtimepermissions.PermissionsManager;
import com.example.yjn.facepass.runtimepermissions.PermissionsResultAction;
import com.example.yjn.facepass.utils.SPUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * 首页
 */
public class MainActivity extends AppCompatActivity {
    private EditText meditText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxPermissions rxPermissions = new RxPermissions(this);
        //动态权限
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .subscribe(granted -> {
                    if (granted) {
                        Log.e("yjn", "granted");
                        doOther();
                    } else {
                        Toast.makeText(MainActivity.this, "权限不足，请手动授权", Toast.LENGTH_SHORT).show();

                        //跳转授权
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });
    }

    private void doOther() {
        meditText2 = (EditText) findViewById(R.id.editText2);

        findViewById(R.id.button).setOnClickListener(v -> {
            ConnectivityManager cwjManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cwjManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {

                final String username = meditText2.getText().toString();
                if (username.length() == 0) {
                    Toast.makeText(MainActivity.this, "请先填写用户名", Toast.LENGTH_SHORT).show();
                } else {
                    SPUtils face_pass = SPUtils.getInstance("face_pass");
                    face_pass.getString("name", ""); //存用户名
                    face_pass.getString("imgPath", Environment.getExternalStorageDirectory() + "/test0"); //用户的照片
                    if (!face_pass.getString("name", "").equals(username)) {
                        Toast.makeText(MainActivity.this, "用户不存在，请先注册", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, TakepictureVerify.class);
                    intent.putExtra("UserName", username);
                    startActivity(new Intent(intent));

                }

            } else {
                Toast.makeText(MainActivity.this, "网络出错,请检查网络连接", Toast.LENGTH_SHORT).show();
            }

        });

        findViewById(R.id.button2).setOnClickListener(v -> {
            ConnectivityManager cwjManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cwjManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                startActivity(new Intent(MainActivity.this, UsernameRegister.class));
            } else {
                Toast.makeText(MainActivity.this, "网络出错，请检查网络连接", Toast.LENGTH_SHORT).show();
            }

        });
    }

    /**
     * 申请所有需要的权限
     */
    @TargetApi(23)
    private void
    requestPermissions() {
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
                Toast.makeText(MainActivity.this, "所有权限通过", Toast.LENGTH_SHORT).show();
                Log.e("yjn", "所有权限通过");
            }

            @Override
            public void onDenied(String permission) {
                Toast.makeText(MainActivity.this, "权限不足", Toast.LENGTH_SHORT).show();
                Log.e("yjn", "权限不足");
            }
        });
    }
}