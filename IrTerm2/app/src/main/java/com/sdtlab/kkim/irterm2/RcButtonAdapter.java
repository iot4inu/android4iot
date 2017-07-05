package com.sdtlab.kkim.irterm2;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/** This class provides data as Views. It is designed to support both ListView and GridView by
 * changing a layout resource file to inflate.
 */
public class RcButtonAdapter extends BaseAdapter implements AbsListView.OnItemClickListener {
    // Debugging
    private static final String TAG = "RcButtonAdapter";
    // Message types sent from the RcButtonAdapter Handler
    public static final int MESSAGE_RC_CODE = 1;
    public static final int MESSAGE_CHECKED_ITEM = 2;

    public static final int MODE_CODE_INPUT = 0;
    public static final int MODE_CODE_OUTPUT = 1;
    private final LayoutInflater mLayoutInflater;
    private final int mResourceId;
    private final Handler mHandler;
    private int mMode = MODE_CODE_OUTPUT;
    private int mPosition = -1;
    /** Create a new instance of {@link RcButtonAdapter}.
     * @param inflater   The layout inflater.
     * @param resourceId The resource ID for the layout to be used. The layout should contain an
     *           ImageView with ID of "rc_button_image" and a TextView with ID of "rc_button_title".
     */
    public RcButtonAdapter(LayoutInflater inflater, int resourceId, Handler handler) {
        mLayoutInflater = inflater;
        mResourceId = resourceId;
        mHandler = handler;
    }
    @Override
    public int getCount() {
        return RcButton.getCount();
    }
    @Override
    public RcButton getItem(int position) {
        return RcButton.getButton(position);
    }
    @Override
    public long getItemId(int position) {
        RcButton rcButton = RcButton.getButton(position);
        return ((rcButton == null) ? 0 : rcButton.mResourceId);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;
        if(null == convertView) {
            view = mLayoutInflater.inflate(mResourceId, parent, false);
        } else {
            view = convertView;
        }
        RcButton rcButton = getItem(position);
        final ImageView image = (ImageView) view.findViewById(R.id.rc_button_image);
        final TextView text = (TextView) view.findViewById(R.id.rc_button_title);
        final TextView code = (TextView) view.findViewById(R.id.rc_button_code);
        if(rcButton != null) {
            if(image != null) image.setImageResource(rcButton.mResourceId);
            if(text != null) text.setText(rcButton.mTitle);
            if(code != null) code.setText("0x" + Long.toString(rcButton.mCode, 16));
        } else {
            if(image != null) image.setImageResource(0);
            if(text != null) text.setText("");
            if(code != null) code.setText("");
        }
        return view;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RcButton rcButton = (RcButton) getItem(position);
//      Context context = view.getContext();
        if (null != rcButton && null != rcButton.mTitle /* && null != context */) {
//          Toast.makeText(context, context.getString(
//                  R.string.item_clicked, rcButton.mTitle), Toast.LENGTH_SHORT).show();
            if(mMode == MODE_CODE_INPUT) {
                mPosition = position;
                mHandler.obtainMessage(MESSAGE_CHECKED_ITEM, position).sendToTarget();
            } else {
                mHandler.obtainMessage(MESSAGE_RC_CODE, rcButton.mCode).sendToTarget();
            }
        }
    }
    public void setMode(int mode) { mMode = mode; }
    public void setCode(long code) {
        if(mPosition >= 0) {
            getItem(mPosition).mCode = code;
            notifyDataSetChanged();
        }
    }
}
