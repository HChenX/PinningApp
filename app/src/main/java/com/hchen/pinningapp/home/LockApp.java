/*
 * This file is part of PinningApp.

 * PinningApp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.

 * Copyright (C) 2023-2024 PinningApp Contributions
 */
package com.hchen.pinningapp.home;

import static com.hchen.pinningapp.utils.Device.isPad;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.View;

import com.hchen.pinningapp.hook.Hook;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class LockApp extends Hook {
    boolean isListen = false;
    boolean isListen2 = false;
    boolean isLock = false;

    @Override
    public void init() {
        if (isPad()) {
            // 平板
            findAndHookConstructor("com.miui.home.recents.GestureStubView",
                    Context.class,
                    new HookAction() {
                        @Override
                        protected void after(XC_MethodHook.MethodHookParam param) {
                            Context context = (Context) param.args[0];
                            if (!isListen) {
                                ContentObserver contentObserver = new ContentObserver(new Handler(context.getMainLooper())) {
                                    @Override
                                    public void onChange(boolean selfChange) {
                                        isLock = getLockApp(context) != -1;
                                        if (isLock) {
                                            setGestureLine(context, 1);
                                        } else {
                                            setGestureLine(context, 0);
                                        }
                                    }
                                };
                                context.getContentResolver().registerContentObserver(
                                        Settings.Global.getUriFor("key_lock_app"),
                                        false, contentObserver);
                                isListen = true;
                            }
                        }
                    }
            );

            findAndHookMethod("com.miui.home.recents.GestureInputHelper",
                    "onInputEvent", InputEvent.class,
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            if (isLock) param.setResult(null);
                        }
                    }
            );

            findAndHookConstructor("com.miui.home.launcher.dock.DockControllerImpl",
                    "com.miui.home.launcher.hotseats.HotSeats", "com.miui.home.launcher.Launcher",
                    new HookAction() {
                        @Override
                        protected void after(XC_MethodHook.MethodHookParam param) {
                            Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                            if (!isListen2) {
                                ContentObserver contentObserver = new ContentObserver(new Handler(context.getMainLooper())) {
                                    @Override
                                    public void onChange(boolean selfChange) {
                                        Object getMDockStateMachine = callMethod(param.thisObject, "getMDockStateMachine");
                                        Object getMDockWindowManager = callMethod(param.thisObject, "getMDockWindowManager");
                                        View mDockRootView = (View) getObjectField(getMDockWindowManager, "mDockRootView");
                                        if (context == null) {
                                            logE(tag, "DockControllerImpl context must not null");
                                            return;
                                        }
                                        if (getLockApp(context) != -1) {
                                            callMethod(getMDockStateMachine, "notifyPinnedStateChanged", false);
                                            mDockRootView.setVisibility(View.GONE);
                                        } else {
                                            callMethod(getMDockStateMachine, "notifyPinnedStateChanged", true);
                                            mDockRootView.setVisibility(View.VISIBLE);
                                        }
                                    }
                                };
                                context.getContentResolver().registerContentObserver(
                                        Settings.Global.getUriFor("key_lock_app"),
                                        false, contentObserver);
                                isListen2 = true;
                            }
                        }
                    }
            );
        } else {
            // 手机
            findAndHookConstructor("com.miui.home.recents.NavStubView",
                    Context.class,
                    new HookAction() {
                        @Override
                        protected void after(XC_MethodHook.MethodHookParam param) {
                            Context context = (Context) param.args[0];
                            if (!isListen) {
                                ContentObserver contentObserver = new ContentObserver(new Handler(context.getMainLooper())) {
                                    @Override
                                    public void onChange(boolean selfChange) {
                                        isLock = getLockApp(context) != -1;
                                    }
                                };
                                context.getContentResolver().registerContentObserver(
                                        Settings.Global.getUriFor("key_lock_app"),
                                        false, contentObserver);
                                isListen = true;
                            }
                        }
                    }
            );

            findAndHookMethod("com.miui.home.recents.NavStubView",
                    "onTouchEvent", MotionEvent.class,
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            if (isLock) param.setResult(false);
                        }
                    }
            );
        }
    }

    public static int getLockApp(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), "key_lock_app");
        } catch (Settings.SettingNotFoundException e) {
            logE("LockApp", "getInt hyceiler_lock_app E: " + e);
        }
        return -1;
    }

    public static void setGestureLine(Context context, int type) {
        Settings.Global.putInt(context.getContentResolver(), "hide_gesture_line", type);
    }
}
