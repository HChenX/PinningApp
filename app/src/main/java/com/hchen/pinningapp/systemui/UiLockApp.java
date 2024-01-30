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
package com.hchen.pinningapp.systemui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.hchen.pinningapp.R;
import com.hchen.pinningapp.hook.Hook;
import com.hchen.pinningapp.hook.Log;
import com.hchen.pinningapp.utils.ToastHelper;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class UiLockApp extends Hook {
    public Context mContext;
    public static Handler mHandler = new LockAppHandler();
    public final static int WILL_LOCK_APP = 0;
    public final static int LOCK_APP = 1;
    public final static int UNLOCK_APP = 2;
    public final static int WILL_UNLOCK_APP = 3;
    public final static int UNKNOWN_ERROR = 4;
    public final static int RESTORE = 5;
    boolean isListen = false;
    public int taskId = -1;

    public int count = 0;
    public int eCount = 0;
    boolean isLock = false;
    public static Resources resources;

    @Override
    public void init() {
        hookAllConstructors("com.android.systemui.statusbar.phone.AutoHideController",
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        Context context = (Context) param.args[0];
                        if (resources == null) {
                            resources = addModuleRes(context);
                        }
                        if (!isListen) {
                            ContentObserver contentObserver = new ContentObserver(new Handler(context.getMainLooper())) {
                                @Override
                                public void onChange(boolean selfChange) {
                                    isLock = getLockApp(context) != -1;
                                    if (getLockApp(context) != -1) {
                                        XposedHelpers.callMethod(param.thisObject, "scheduleAutoHide");
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

        findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBarView",
                "onTouchEvent", MotionEvent.class, new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        MotionEvent motionEvent = (MotionEvent) param.args[0];
                        // logE(tag, "mo: " + motionEvent.getActionMasked());
                        mContext = (Context) XposedHelpers.callMethod(param.thisObject, "getContext");
                        int action = motionEvent.getActionMasked();
                        int lockId = getLockApp(mContext);
                        if (getSystemLockEnable(mContext)) {
                            setSystemLockApp(mContext);
                        }
                        if (getSystemLockScreen(mContext)) {
                            setSystemLockScreen(mContext, 0);
                        }
                        if (action == 2) {
                            count = count + 1;
                            if (count > 6) {
                                remoAllMes();
                                count = 0;
                                return;
                            }
                        }
                        if (action == 0) {
                            Class<?> ActivityManagerWrapper = findClassIfExists("com.android.systemui.shared.system.ActivityManagerWrapper");
                            ActivityManager.RunningTaskInfo runningTaskInfo;
                            if (ActivityManagerWrapper != null) {
                                try {
                                    ActivityManagerWrapper.getDeclaredMethod("getInstance");
                                    Object getInstance = XposedHelpers.callStaticMethod(
                                            ActivityManagerWrapper,
                                            "getInstance");
                                    runningTaskInfo = (ActivityManager.RunningTaskInfo) XposedHelpers.callMethod(
                                            getInstance, "getRunningTask");
                                } catch (NoSuchMethodException e) {
                                    Object sInstance = XposedHelpers.getStaticObjectField(ActivityManagerWrapper, "sInstance");
                                    runningTaskInfo = (ActivityManager.RunningTaskInfo) XposedHelpers.callMethod(
                                            sInstance, "getRunningTask");
                                }
                            } else {
                                logE(tag, "ActivityManagerWrapper is null");
                                return;
                            }
                            if (runningTaskInfo == null) {
                                logE(tag, "runningTaskInfo is null");
                                return;
                            }
                            // ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo) XposedHelpers.callMethod(
                            //     XposedHelpers.callStaticMethod(findClassIfExists("com.miui.home.recents.RecentsModel"), "getInstance",
                            //         mContext), "getRunningTaskContainHome");
                            taskId = runningTaskInfo.taskId;
                            ComponentName topActivity = runningTaskInfo.topActivity;
                            String pkg = topActivity.getPackageName();
                            if ("com.miui.home".equals(pkg)) {
                                return;
                            }
                            // logE(tag, "task id: " + taskId + " a: " + pkg);
                            remoAllMes();
                            if (lockId == -1) {
                                mHandler.sendMessageDelayed(mHandler.obtainMessage(WILL_LOCK_APP), 1000);
                                mHandler.sendMessageDelayed(mHandler.obtainMessage(LOCK_APP, taskId), 1500);
                                // XposedHelpers.callMethod(param.thisObject, "updateLayoutForCutout");
                            } else {
                                if (lockId == taskId) {
                                    mHandler.sendMessageDelayed(mHandler.obtainMessage(WILL_UNLOCK_APP), 1000);
                                    mHandler.sendMessageDelayed(mHandler.obtainMessage(UNLOCK_APP), 1500);
                                } else {
                                    if (lockId != -1) {
                                        if (eCount < 2) {
                                            mHandler.sendMessage(mHandler.obtainMessage(UNKNOWN_ERROR));
                                            eCount = eCount + 1;
                                        } else {
                                            mHandler.sendMessage(mHandler.obtainMessage(RESTORE));
                                            eCount = 0;
                                        }
                                    }
                                }
                            }
                        }
                        if (action == 1) {
                            remoAllMes();
                        }
                        if (getLockApp(mContext) == taskId && lockId != -1) {
                            param.setResult(true);
                        }
                    }
                }
        );

        findAndHookMethod("com.android.systemui.shared.system.ActivityManagerWrapper",
                "isLockTaskKioskModeActive", new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        param.setResult(false);
                    }
                }
        );

        findAndHookMethod("com.android.systemui.shared.system.ActivityManagerWrapper",
                "isScreenPinningActive", new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        param.setResult(false);
                    }
                }
        );
        Class<?> ScreenPinningNotify = findClassIfExists("com.android.systemui.navigationbar.ScreenPinningNotify");
        if (ScreenPinningNotify != null) {
            Method[] methods = ScreenPinningNotify.getDeclaredMethods();
            for (Method method : methods) {
                switch (method.getName()) {
                    case "showPinningStartToast", "showPinningExitToast", "showEscapeToast" -> {
                        if (method.getReturnType().equals(void.class)) hookToast(method);
                    }
                }
            }
        }
    }

    public void hookToast(Method method) {
        hookMethod(method,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        param.setResult(null);
                    }
                }
        );
    }

    public void remoAllMes() {
        mHandler.removeMessages(WILL_LOCK_APP);
        mHandler.removeMessages(LOCK_APP);
        mHandler.removeMessages(WILL_UNLOCK_APP);
        mHandler.removeMessages(UNLOCK_APP);
    }

    public static int getLockApp(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), "key_lock_app");
        } catch (Settings.SettingNotFoundException e) {
            logE("LockApp", "getInt hyceiler_lock_app will set E: " + e);
            setLockApp(context, -1);
        }
        return -1;
    }

    public boolean getSystemLockEnable(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), "lock_to_app_enabled") == 1;
        } catch (Settings.SettingNotFoundException e) {
            logE(tag, "getSystemLock E will set " + e);
            setSystemLockApp(context);
        }
        return false;
    }

    public boolean getSystemLockScreen(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), "lock_to_app_exit_locked") == 1;
        } catch (Settings.SettingNotFoundException e) {
            logE(tag, "getSystemLockScreen E will set " + e);
            setSystemLockScreen(context, 0);
        }
        return false;
    }

    public static void setLockApp(Context context, int id) {
        Settings.Global.putInt(context.getContentResolver(), "key_lock_app", id);
    }

    public static void setSystemLockApp(Context context) {
        Settings.System.putInt(context.getContentResolver(), "lock_to_app_enabled", 0);
    }

    public static void setSystemLockScreen(Context context, int value) {
        Settings.Secure.putInt(context.getContentResolver(), "lock_to_app_exit_locked", value);
    }

    /**
     * @noinspection deprecation
     */
    public static class LockAppHandler extends Handler {


        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Context context = findContext(FLAG_CURRENT_APP);
            if (context == null) {
                mHandler.sendMessageDelayed(mHandler.obtainMessage(msg.what), 500);
                return;
            }
            if (resources == null) {
                Log.logE("UiLockApp", "resources is null!!");
                return;
            }
            switch (msg.what) {
                case WILL_LOCK_APP -> {
                    ToastHelper.makeText(context,
                            resources.getString(
                                    R.string.home_other_lock_app_will_lock),
                            false);
                }
                case LOCK_APP -> {
                    int taskId = (int) msg.obj;
                    setLockApp(context, taskId);
                    ToastHelper.makeText(context,
                            resources.getString(
                                    R.string.home_other_lock_app_lock),
                            false);
                }
                case WILL_UNLOCK_APP -> {
                    ToastHelper.makeText(context,
                            resources.getString(
                                    R.string.home_other_lock_app_will_unlock),
                            false);
                }
                case UNLOCK_APP -> {
                    setLockApp(context, -1);
                    ToastHelper.makeText(context,
                            resources.getString(
                                    R.string.home_other_lock_app_unlock),
                            false);
                }
                case UNKNOWN_ERROR -> {
                    ToastHelper.makeText(context,
                            resources.getString(
                                    R.string.home_other_lock_app_e),
                            false);
                }
                case RESTORE -> {
                    setLockApp(context, -1);
                    ToastHelper.makeText(context,
                            resources.getString(
                                    R.string.home_other_lock_app_r),
                            false);
                }
            }
        }
    }
}

