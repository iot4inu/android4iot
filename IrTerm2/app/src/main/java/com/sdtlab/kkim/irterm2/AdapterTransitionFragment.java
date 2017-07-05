package com.sdtlab.kkim.irterm2;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.transition.AutoTransition;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

// Main screen for Adapter Transition.
public class AdapterTransitionFragment extends Fragment implements Transition.TransitionListener {
    // Debugging
    private static final String TAG = "ATFragment";

    // Since the transition framework requires all relevant views in a view hierarchy to be marked
    // with IDs, we use this ID to mark the root view.
    private static final int ROOT_ID = 1;
    // A tag for saving state whether the mAbsListView is ListView, or GridView (3- or 5-column).
    private static final String STATE_GRID_STYLE = "grid style";
    // This is where we place our AdapterView (ListView / GridView (3- or 5-column).
    private FrameLayout mContent;
    // This is where we carry out the transition.
    private FrameLayout mCover;
    // This list shows our contents. It can be ListView or GridView, and we toggle between them
    // using the transition framework.
    private AbsListView mAbsListView;
    // This is our contents.
    private static RcButtonAdapter mRbAdapter;
    private int mGridStyle = RcButton.ONE_COLUMN;

    // Intent request code
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    // Local Bluetooth adapter
    private BluetoothAdapter mBtAdapter = null;
    // Member object for the bluetooth services
    private static BluetoothService mBtService = null;
    // Name of the connected device
    private static String mConnectedDeviceName = null;
    // IR code file name
    private static final String CODE_FILENAME = "code.txt";

    public static AdapterTransitionFragment newInstance() {
        return new AdapterTransitionFragment();
    }
    public AdapterTransitionFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "in onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBtAdapter == null) {
            Activity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
//          activity.finish();
        }
    }
    @Override
    public void onStart() {
        Log.d(TAG, "in onStart");
        super.onStart();
        if (mBtService == null) {
            mBtService = new BluetoothService(mBtHandler);
        }
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "in onDestroy");
        super.onDestroy();
        if (mBtService != null) {
            mBtService.stop();
            mBtService = null;
        }
        mConnectedDeviceName = null;
        mRbAdapter = null;
    }
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "in onCreateView");
        // If savedInstanceState is available, we restore the frame id state..
        if (null != savedInstanceState) {
            mGridStyle = savedInstanceState.getInt(STATE_GRID_STYLE, RcButton.ONE_COLUMN);
        }
        inflateAbsList(inflater, container);
        return inflater.inflate(R.layout.fragment_adapter_transition, container, false);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_GRID_STYLE, mGridStyle);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Retaining references for FrameLayouts that we use later.
        mContent = (FrameLayout) view.findViewById(R.id.content);
        mCover = (FrameLayout) view.findViewById(R.id.cover);
        // We are attaching the list to the screen here.
        mContent.addView(mAbsListView);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_adapter_transition, menu);
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // We change the look of the icon every time
        // the user toggles between list and grid.
        MenuItem item = menu.findItem(R.id.action_cycle);
        if (null != item) {
            if (mGridStyle == RcButton.ONE_COLUMN) {
                item.setIcon(R.drawable.ic_action_3c_grid);
                item.setTitle(getString(R.string.show_as_3c_grid));
            } else if(mGridStyle == RcButton.THREE_COLUMNS) {
                item.setIcon(R.drawable.ic_action_5c_grid);
                item.setTitle(R.string.show_as_5c_grid);
            } else {
                item.setIcon(R.drawable.ic_action_list);
                item.setTitle(getString(R.string.show_as_list));
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "in onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.action_cycle:
                cycleViews();
                return true;
            case R.id.bt_connect:
                if (getDeviceState()) {
                    enableBluetooth();
                } else {
                    Activity activity = getActivity();
                    Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
//                  getActivity().finish();
                }
                return true;
            case R.id.save_code:
                try {
                    FileOutputStream outputStream
                        = getActivity().openFileOutput(CODE_FILENAME, Context.MODE_PRIVATE);
                    RcButton.writeCode(outputStream);
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.load_code:
                try {
                    FileInputStream inputStream
                            = getActivity().openFileInput(CODE_FILENAME);
                    RcButton.readCode(inputStream);
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.export_code:
                try {
                    File file = new File(Environment.getExternalStorageDirectory(), CODE_FILENAME);
                    FileOutputStream outputStream = new FileOutputStream(file);
                    RcButton.writeCode(outputStream);
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.import_code:
                try {
                    File file = new File(Environment.getExternalStorageDirectory(), CODE_FILENAME);
                    if(file.exists()) {
                        FileInputStream inputStream = new FileInputStream(file);
                        RcButton.readCode(inputStream);
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mAbsListView.invalidateViews();
                return true;
        }
        return false;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "in onActivityResult");
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String address
                            = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
                    mBtService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    scanDevice();
                }
                break;
        }
    }
    @Override
    public void onTransitionStart(Transition transition) {
    }
    // BEGIN_INCLUDE(on_transition_end)
    @Override
    public void onTransitionEnd(Transition transition) {
        // When the transition ends, we remove all the views from the overlay and hide it.
        mCover.removeAllViews();
        mCover.setVisibility(View.INVISIBLE);
    }
    // END_INCLUDE(on_transition_end)
    @Override
    public void onTransitionCancel(Transition transition) {
    }
    @Override
    public void onTransitionPause(Transition transition) {
    }
    @Override
    public void onTransitionResume(Transition transition) {
    }
    /** Inflate a ListView or a GridView with a corresponding ListAdapter.
     * @param inflater The LayoutInflater.
     * @param container The ViewGroup that contains this AbsListView. The AbsListView won't be
     *                  attached to it.
     */
    private void inflateAbsList(LayoutInflater inflater, ViewGroup container) {
        if (mGridStyle == RcButton.ONE_COLUMN) {
            mAbsListView = (AbsListView) inflater.inflate(R.layout.fragment_rc_button_list,
                    container, false);
            mAbsListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
//          mAbsListView.setSelector(R.color.selector);
            mRbAdapter = new RcButtonAdapter(inflater, R.layout.item_rc_button_list, mRbHandler);
            mRbAdapter.setMode(RcButtonAdapter.MODE_CODE_INPUT);
        } else {
            mAbsListView = (AbsListView) inflater.inflate(
                    (mGridStyle == RcButton.THREE_COLUMNS) ? R.layout.fragment_rc_button_grid_3c
                            : R.layout.fragment_rc_button_grid_5c,
                    container, false);
            mAbsListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
            mRbAdapter = new RcButtonAdapter(inflater, R.layout.item_rc_button_grid, mRbHandler);
            mRbAdapter.setMode(RcButtonAdapter.MODE_CODE_OUTPUT);
        }
        RcButton.setLayout(mGridStyle);
        mAbsListView.setAdapter(mRbAdapter);
        mAbsListView.setOnItemClickListener(mRbAdapter);
    }
    // Toggle the UI between ListView and GridView.
    private void cycleViews() {
        // We use mCover as the overlay on which we carry out the transition.
        mCover.setVisibility(View.VISIBLE);
        // This FrameLayout holds all the visible views in the current list or grid. We use this as
        // the starting Scene of the Transition later.
        FrameLayout before = copyVisibleViews();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mCover.addView(before, params);
        // Swap the actual list.
        nextAbsListView();
        // We also swap the icon for the toggle button.
        getActivity().invalidateOptionsMenu();
        // It is now ready to start the transition.
        mAbsListView.post(new Runnable() {
            @Override
            public void run() {
                // BEGIN_INCLUDE(transition_with_listener)
                Scene scene = new Scene(mCover, copyVisibleViews());
                Transition transition = new AutoTransition();
                transition.addListener(AdapterTransitionFragment.this);
                TransitionManager.go(scene, transition);
                // END_INCLUDE(transition_with_listener)
            }
        });
    }
    // Swap ListView with GridView, or GridView with ListView.
    private void nextAbsListView() {
        // We save the current scrolling position before removing the current list.
        int first = mAbsListView.getFirstVisiblePosition();
        // If the current list is a ListView,  we replace it with a GridView (Control).
        // If it is a GridView (Control), a GridView (Grid).
        // And if it is a GridView(Grid), a ListView.
        mGridStyle = (mGridStyle + 1) % 3;
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        inflateAbsList(inflater, (ViewGroup) mAbsListView.getParent());
        mAbsListView.setAdapter(mRbAdapter);
        // We restore the scrolling position here.
        mAbsListView.setSelection(first);
        // The new list is ready, and we replace the existing one with it.
        mContent.removeAllViews();
        mContent.addView(mAbsListView);
    }
    /** Copy all the visible views in the mAbsListView into a new FrameLayout and return it.
     * @return a FrameLayout with all the visible views inside.
     */
    private FrameLayout copyVisibleViews() {
        // This is the FrameLayout we return afterwards.
        FrameLayout layout = new FrameLayout(getActivity());
        // The transition framework requires to set ID for all views to be animated.
        layout.setId(ROOT_ID);
        // We only copy visible views.
        int first = mAbsListView.getFirstVisiblePosition();
        int index = 0;
        while (true) {
            // This is one of the views that we copy. Note that the argument for getChildAt is a
            // zero-oriented index, and it doesn't usually match with its position in the list.
            View source = mAbsListView.getChildAt(index);
            if (null == source) {
                break;
            }
            // This is the copy of the original view.
            View destination = mRbAdapter.getView(first + index, null, layout);
            assert destination != null;
            destination.setId(ROOT_ID + first + index);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    source.getWidth(), source.getHeight());
            params.leftMargin = (int) (mAbsListView.getX() + source.getX());
            params.topMargin = (int) (mAbsListView.getY() + source.getY());
            layout.addView(destination, params);
            ++index;
        }
        return layout;
    }
    public boolean getDeviceState() {
        return (mBtAdapter != null);
    }
    public void enableBluetooth() {
        Log.d(TAG, "in enableBluetooth");
        if (mBtAdapter.isEnabled()) {
            scanDevice();
        } else {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }
    public void scanDevice() {
        Log.d(TAG, "in scanDevice");
        Intent serverIntent
                = new Intent(getActivity(), com.sdtlab.kkim.irterm2.DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }
    /** Instances of static inner classes do not hold an implicit
     * reference to their outer class.
     */
    private static class BtHandler extends Handler {
        private final WeakReference<AdapterTransitionFragment> mFragement;

        public BtHandler(AdapterTransitionFragment fragment) {
            mFragement = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final AdapterTransitionFragment atFragment = mFragement.get();
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(mFragement.get().getString(R.string.title_connected_to,
                                    mConnectedDeviceName));
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_WRITEN:
//                  byte[] writeBuf = (byte[]) msg.obj;
                    break;
                case BluetoothService.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, msg.arg1, msg.arg2);
//                  Toast.makeText(activity, readMessage, Toast.LENGTH_SHORT).show();
                    mRbAdapter.setCode(Long.parseLong(readMessage, 16));
                    break;
                case BluetoothService.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName
                            = msg.getData().getString(BluetoothService.ARG_DEVICE_NAME);
                    if(atFragment != null) {
                        final Activity activity = atFragment.getActivity();
                        if(activity != null)
                            Toast.makeText(atFragment.getActivity(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case BluetoothService.MESSAGE_TOAST:
                    if(atFragment != null) {
                        final Activity activity = atFragment.getActivity();
                        if(activity != null)
                            Toast.makeText(atFragment.getActivity(),
                                msg.getData().getString(BluetoothService.ARG_TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
        // Updates the status on the action bar.
        // @param resId a string resource ID
        private void setStatus(int resId) {
            final AdapterTransitionFragment atFragment = mFragement.get();
            if(atFragment == null) return;
            final Activity activity = atFragment.getActivity();
            if (null == activity) return;
            final ActionBar actionBar = activity.getActionBar();
            if(null == actionBar) return;
            actionBar.setSubtitle(resId);
        }
        /** Updates the status on the action bar.
         * @param subTitle status
         */
        private void setStatus(CharSequence subTitle) {
            final AdapterTransitionFragment atFragment = mFragement.get();
            if(atFragment == null) return;
            final Activity activity = atFragment.getActivity();
            if (null == activity) return;
            final ActionBar actionBar = activity.getActionBar();
            if (null == actionBar) return;
            actionBar.setSubtitle(subTitle);
        }
    }
    private final BtHandler mBtHandler = new BtHandler(this);

    private static class RbHandler extends Handler {
        private final WeakReference<AdapterTransitionFragment> mFragement;

        public RbHandler(AdapterTransitionFragment fragment) {
            mFragement = new WeakReference<>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final AdapterTransitionFragment atFragment = mFragement.get();
            switch (msg.what) {
                case RcButtonAdapter.MESSAGE_RC_CODE:
                    if(!sendBtMessage(Long.toString((long) msg.obj, 16))) {
                        if(atFragment != null)
                            Toast.makeText(atFragment.getActivity(),
                                    R.string.not_connected, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case RcButtonAdapter.MESSAGE_CHECKED_ITEM:
                    if(atFragment != null)
                        atFragment.mAbsListView.setItemChecked((int) msg.obj, true);
                    break;
            }
        }
    }
    private final RbHandler mRbHandler = new RbHandler(this);

    /** Sends a message.
     * @param message A string of text to send.
     */
    public static boolean sendBtMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mBtService.getState() != BluetoothService.STATE_CONNECTED) {
            return false;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = (message + '\n').getBytes();
            mBtService.write(send);
        }
        return true;
    }
}
