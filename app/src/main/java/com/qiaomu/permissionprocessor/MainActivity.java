package com.qiaomu.permissionprocessor;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.qiaomu.annotation.PermissionDenied;
import com.qiaomu.annotation.PermissionGrant;
import com.qiaomu.annotation.ShowRequestPermissionRationale;
import com.qiaomu.libpermission.PermissionHelper;

/**
 * Created by qiaomu on 2017/10/9.
 */

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.sdcardTip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionHelper.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 103)) {
                    PermissionHelper.requestPermissions(MainActivity.this, 103, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        findViewById(R.id.phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionHelper.requestPermissions(MainActivity.this, 100, Manifest.permission.CALL_PHONE);
            }
        });


        findViewById(R.id.sdcard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionHelper.requestPermissions(MainActivity.this, 101, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        });

        findViewById(R.id.multi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionHelper.requestPermissions(MainActivity.this, 102, Manifest.permission.READ_SMS,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS);
            }
        });
    }

    @PermissionGrant(100)
    public void requestPhoneOnGrant() {
        Toast.makeText(this, "phone grant", Toast.LENGTH_SHORT).show();
    }

    @PermissionDenied(101)
    public void requestSdOnDenied() {
        Toast.makeText(this, "sdcard denied", Toast.LENGTH_SHORT).show();
    }

    @ShowRequestPermissionRationale(103)
    public void whyNeedSdCard() {
        Toast.makeText(this, "I need write news to sdcard!", Toast.LENGTH_SHORT).show();
        PermissionHelper.requestPermissions(MainActivity.this, 103, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
