package com.reactnativekeyboardnavigationview;

import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.util.ReactFindViewUtil;
import com.facebook.react.views.view.ReactClippingViewManager;
import com.facebook.react.views.view.ReactViewGroup;

import java.util.Map;
import android.app.Instrumentation;
import android.view.inputmethod.BaseInputConnection;

public class KeyboardNavigationViewViewManager extends ReactClippingViewManager<ReactViewGroup> {
    public static final String REACT_CLASS = "KeyboardNavigationViewView";

    public final int COMMAND_INIT = 1;
    public final int COMMAND_BLOCK_ENTER = 2;
    public final int COMMAND_DOWN = 3;
    public final int COMMAND_UP = 4;
    public final int COMMAND_FOCUS = 5;
    public final int COMMAND_UP_LISTENER = 6;

    @Override
    @NonNull
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    @NonNull
    public ReactViewGroup createViewInstance(ThemedReactContext reactContext) {
        return new ReactViewGroup(reactContext);
    }

    @ReactProp(name = "color")
    public void setColor(View view, String color) {
        view.setBackgroundColor(Color.parseColor(color));
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        Log.d("React", " View manager getCommandsMap:");
        return MapBuilder.of(
            "COMMAND_INIT", COMMAND_INIT,
            "COMMAND_BLOCK_ENTER", COMMAND_BLOCK_ENTER,
            "COMMAND_DOWN",COMMAND_DOWN,
            "COMMAND_UP",COMMAND_UP,
            "COMMAND_FOCUS",COMMAND_FOCUS,
            "COMMAND_UP_LISTENER",COMMAND_UP_LISTENER
        );
    }

    @Override
    public void receiveCommand(final ReactViewGroup root, String commandId, @Nullable ReadableArray args) {
        int commandIdInt = Integer.parseInt(commandId);
        View rootView = root.getRootView();
        View targetView = ReactFindViewUtil.findView(rootView, args.getString(0));
        targetView.setFocusableInTouchMode(true);
        switch (commandIdInt) {
            case COMMAND_INIT:
                // 添加回车键监听
                Log.d("React native","INIT:" );
                targetView.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN) {
                            Log.d("React native","COMMAND_LISTENER_ACTION_UP:" );
                            v.onKeyDown(keyCode, event);
                            return true;
                        }
                        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_UP) {
                            WritableMap params = Arguments.createMap(); // add here the data you want to send
                            params.putString("nativeID", args.getString(0));
                            ReactContext reactContext = (ReactContext) v.getContext();
                            reactContext
                                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                    .emit("enterPressed", params);
                            Log.d("React native","COMMAND_LISTENER_ACTION_UP:" );
                            v.onKeyUp(keyCode, event);
                            return true;
                        }
                        return false;
                    }
                });
                break;
            case COMMAND_BLOCK_ENTER:
                targetView.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_UP) {
                            Log.d("React native","COMMAND_BLOCK_ENTER:" );
//                            retrun true 会block
                            return true;
                        }
                        return false;
                    }
                });
                break;
            case COMMAND_DOWN:
                targetView.requestFocus();
                BaseInputConnection down = new BaseInputConnection(targetView, true);
                down.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case COMMAND_UP:
                targetView.requestFocus();
                BaseInputConnection up = new BaseInputConnection(targetView, true);
                up.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                View defaultFocus = ReactFindViewUtil.findView(rootView, "defaultFocus");
                defaultFocus.requestFocus();
                break;
            case COMMAND_FOCUS:
                targetView.requestFocus();
                break;
            case COMMAND_UP_LISTENER:
                targetView.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_UP) {
                            View toFocus = ReactFindViewUtil.findView(rootView, args.getString(1));
                            toFocus.requestFocus();
                            Log.d("React native","COMMAND_BLOCK_ENTER:" );
                            return false;
                        }
                        return false;
                    }
                });
                break;
        }
    }
}
