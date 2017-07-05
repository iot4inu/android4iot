package com.sdtlab.kkim.irterm1;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class  MainActivity extends Activity implements View.OnClickListener {
    // Intent request code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    // remote control buttons
    private static final int [] rcId = {
            R.id.rc1, R.id.rc2, R.id.rc3, R.id.rc4, R.id.rc5, R.id.rc6, R.id.rc7, R.id.rc8
    };
    private static final int RC_BUTTONS = rcId.length;
    private ImageButton [] rcButton;
    private ImageButton btConnect;

    private BluetoothService btService = null;
    private static final Handler mHandler;
    static {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (btService == null) {
            btService = new BluetoothService(this, mHandler);
        }
        rcButton = new ImageButton [RC_BUTTONS];
        for(int i = 0; i < RC_BUTTONS; i++)
            rcButton[i] = (ImageButton) findViewById(rcId[i]);
        btConnect = (ImageButton) findViewById(R.id.bt_connect);
        btConnect.setOnClickListener(this);
        for(int i = 0; i < RC_BUTTONS; i++)
            rcButton[i].setOnClickListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {
        if(v == btConnect) {
            if (btService.getDeviceState()) {
                btService.enableBluetooth();
            } else {
                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
//              finish();
            }
        } else {
            for(int i = 0; i < RC_BUTTONS; i++) {
                if(v == rcButton[i]) {
                    byte [] value = String.valueOf(i + 1).getBytes();
                    btService.write(value);
                }
            }
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    btService.getDeviceInfo(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    btService.scanDevice();
                }
                break;
        }
    }
}
