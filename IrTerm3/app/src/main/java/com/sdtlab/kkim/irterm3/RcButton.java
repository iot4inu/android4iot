package com.sdtlab.kkim.irterm3;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class RcButton {
    private static final String TAG = "RcButton";

    public String mTitle;
    public int mResourceId;
    public long mCode;

    public RcButton(String title, int resourceId, int code) {
        mTitle = title;
        mResourceId = resourceId;
        mCode = code;
    }

    private static final RcButton RB_BLANK = new RcButton("Blank", R.drawable.rc_blank, 'P');
    private static final RcButton RB_POWER = new RcButton("Power", R.drawable.rc_power, 'P');
    private static final RcButton RB_SOURCE = new RcButton("Source", R.drawable.rc_tv, 'S');
    private static final RcButton RB_1 = new RcButton("1", R.drawable.rc_1, '1');
    private static final RcButton RB_2 = new RcButton("2", R.drawable.rc_2, '2');
    private static final RcButton RB_3 = new RcButton("3", R.drawable.rc_3, '3');
    private static final RcButton RB_4 = new RcButton("4", R.drawable.rc_4, '4');
    private static final RcButton RB_5 = new RcButton("5", R.drawable.rc_5, '5');
    private static final RcButton RB_6 = new RcButton("6", R.drawable.rc_6, '6');
    private static final RcButton RB_7 = new RcButton("7", R.drawable.rc_7, '7');
    private static final RcButton RB_8 = new RcButton("8", R.drawable.rc_8, '8');
    private static final RcButton RB_9 = new RcButton("9", R.drawable.rc_9, '9');
    private static final RcButton RB_0 = new RcButton("0", R.drawable.rc_0, '0');
    private static final RcButton RB_VOLUME_UP = new RcButton("Volume Up", R.drawable.rc_plus, '+');
    private static final RcButton RB_VOLUME_DOWN = new RcButton("Volume Down", R.drawable.rc_minus, '-');
    private static final RcButton RB_MUTE = new RcButton("Mute", R.drawable.rc_mute, 'h');
    private static final RcButton RB_CHANNEL_UP = new RcButton("Channel Up", R.drawable.rc_up, '^');
    private static final RcButton RB_CHANNEL_DOWN = new RcButton("Channel Down", R.drawable.rc_down, 'v');
    private static final RcButton RB_PREVIOUS = new RcButton("Previous", R.drawable.rc_return, 'p');
    private static final RcButton RB_MENU = new RcButton("Menu", R.drawable.rc_menu, 'M');
    private static final RcButton RB_GUIDE = new RcButton("Guide", R.drawable.rc_tv, 'G');
    private static final RcButton RB_TOOLS = new RcButton("Tools", R.drawable.rc_tools, 'T');
    private static final RcButton RB_MENU_UP = new RcButton("Menu Up", R.drawable.rc_up, 'u');
    private static final RcButton RB_MENU_DOWN = new RcButton("Menu Down", R.drawable.rc_down, 'd');
    private static final RcButton RB_MENU_LEFT = new RcButton("Menu Left", R.drawable.rc_left, 'l');
    private static final RcButton RB_MENU_RIGHT = new RcButton("Menu Right", R.drawable.rc_right, 'r');
    private static final RcButton RB_MENU_OK = new RcButton("Menu OK", R.drawable.rc_ok, 'o');
    private static final RcButton RB_MENU_RETURN = new RcButton("Menu Return", R.drawable.rc_return, 'R');
    private static final RcButton RB_PLAY_BACK = new RcButton("Play Back", R.drawable.rc_playback, '>');
    private static final RcButton RB_STOP = new RcButton("Stop", R.drawable.rc_stop, '.');
    private static final RcButton RB_PAUSE = new RcButton("Pause", R.drawable.rc_pause, '|');
    private static final RcButton RB_REWIND = new RcButton("Rewind", R.drawable.rc_rewind, 'B');
    private static final RcButton RB_FAST_FORWARD = new RcButton("Fast Forward", R.drawable.rc_fast_forward, 'F');

    private static final RcButton[] RC_BUTTONS_1C = {
            RB_BLANK, RB_POWER, RB_SOURCE,
            RB_1, RB_2, RB_3, RB_4, RB_5, RB_6, RB_7, RB_8, RB_9, RB_0,
            RB_VOLUME_UP, RB_VOLUME_DOWN, RB_MUTE,
            RB_CHANNEL_UP, RB_CHANNEL_DOWN, RB_PREVIOUS,
            RB_MENU, RB_GUIDE, RB_TOOLS,
            RB_MENU_UP, RB_MENU_DOWN, RB_MENU_LEFT, RB_MENU_RIGHT,
            RB_MENU_OK, RB_MENU_RETURN,
            RB_PLAY_BACK, RB_STOP, RB_PAUSE, RB_REWIND, RB_FAST_FORWARD
    };
    private static final RcButton[] RC_BUTTONS_3C = {
            RB_POWER, null, RB_SOURCE,
            RB_1, RB_2, RB_3,
            RB_4, RB_5, RB_6,
            RB_7, RB_8, RB_9,
            null, RB_0, RB_PREVIOUS,
            RB_VOLUME_UP, RB_MUTE, RB_CHANNEL_UP,
            RB_VOLUME_DOWN, null, RB_CHANNEL_DOWN,
            RB_MENU, null, RB_GUIDE,
            RB_TOOLS, RB_MENU_UP, null,
            RB_MENU_LEFT, RB_MENU_OK, RB_MENU_RIGHT,
            RB_MENU_RETURN, RB_MENU_DOWN, null,
            RB_STOP,  RB_PAUSE, RB_PLAY_BACK,
            RB_REWIND, null, RB_FAST_FORWARD
    };
    private static final RcButton[] RC_BUTTONS_5C = {
            RB_POWER, null,                     null, null, RB_SOURCE,
            RB_CHANNEL_UP, RB_VOLUME_UP,      RB_1, RB_2, RB_3,
            RB_CHANNEL_DOWN, RB_VOLUME_DOWN, RB_4, RB_5, RB_6,
            RB_PREVIOUS, RB_MUTE,              RB_7, RB_8, RB_9,
            RB_MENU, RB_GUIDE,                 null, RB_0, null,
            RB_PLAY_BACK, RB_STOP,            RB_TOOLS, RB_MENU_UP, null,
            RB_PAUSE, null,                    RB_MENU_LEFT, RB_MENU_OK, RB_MENU_RIGHT,
            RB_REWIND, RB_FAST_FORWARD,      RB_MENU_RETURN, RB_MENU_DOWN, null
    };

    private static RcButton[] mButtons = RC_BUTTONS_1C;
    private static int mnColumns = 1;

    public static final int ONE_COLUMN = 0;
    public static final int THREE_COLUMNS = 1;
    public static final int FIVE_COLUMNS = 2;
    public static void setLayout(int layout) {
        if(layout == ONE_COLUMN) {
            mButtons = RC_BUTTONS_1C;
            mnColumns = 1;
        } else if(layout == THREE_COLUMNS) {
            mButtons = RC_BUTTONS_3C;
            mnColumns = 3;
        } else if(layout == FIVE_COLUMNS) {
            mButtons = RC_BUTTONS_5C;
            mnColumns = 5;
        }
    }
    public static int getCount() { return mButtons.length; }
    public static int getColumnCount() { return mnColumns; }
    public static RcButton getButton(int position) {
        if((position < 0) || (position >= mButtons.length)) return null;
        return(mButtons[position]);
    }
    public static RcButton getButton(String title) {
        for(int i = 0; i < mButtons.length; i++) {
            if(mButtons[i] == null) continue;
            if(title.compareToIgnoreCase(mButtons[i].mTitle) == 0)
                return mButtons[i];
        }
        return null;
    }
    public static boolean writeCode(FileOutputStream outputStream) {
        RcButton rcButton;
        for(int i = 0; i < RC_BUTTONS_1C.length; i++) {
            rcButton = RC_BUTTONS_1C[i];
            try {
                outputStream.write(rcButton.mTitle.getBytes());
                outputStream.write('\n');
                outputStream.write(Long.toHexString(rcButton.mCode).getBytes());
                outputStream.write('\n');
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    public static boolean readCode(FileInputStream inputStream) {
        RcButton rcButton;
        int position;
        long[] codes = new long[RC_BUTTONS_1C.length];
        String title = null, code = null;
        for(position = 0; position < RC_BUTTONS_1C.length; position++)
            codes[position] = -1;

        byte[] buffer = new byte[1024];
        int bytes;
        int i, first, offset;
        try {
            position = 0; offset = 0;
            while((bytes = inputStream.read(buffer, offset, 1024 - offset)) > 0) {
                bytes += offset;
                for (i = 0, first = 0; i < bytes; i++) {
                    if (buffer[i] == '\n') {
                        if(title == null) {
                            title = new String(buffer, first, i - first);
                            Log.d(TAG, "readCode: title = " + title);
                            Log.d(TAG, "readCode: position = " + position);
                            if(!title.equals(RC_BUTTONS_1C[position].mTitle)) {
                                for(position = 0; position < RC_BUTTONS_1C.length; position++) {
                                    if(title.equals(RC_BUTTONS_1C[position].mTitle))
                                        break;
                                }
                                if(position == RC_BUTTONS_1C.length) {
                                    Log.e(TAG, "readCode: unknown button title.");
                                    return false;
                                }
                            }
                        } else {
                            code = new String(buffer, first, i - first);
                            codes[position] = Long.parseLong(code, 16);
                            title = null;
                            position++; // hint for the next button
                        }
                        first = i + 1; // skip '\n'
                    }
                }
                if ((first == 0) && (bytes == 1024)) {
                    Log.e(TAG, "readCode: line too long.");
                    return false;
                } else {
                    for (i = first; i < bytes; i++)
                        buffer[i - first] = buffer[i];
                    offset = bytes - first;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
        for(position = 0; position < RC_BUTTONS_1C.length; position++) {
            if(codes[position] < 0)
                continue;
            rcButton = RC_BUTTONS_1C[position];
            rcButton.mCode = codes[position];
        }
        return true;
    }
}
