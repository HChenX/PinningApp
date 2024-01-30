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
package com.hchen.pinningapp.securitycenter;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

import com.hchen.pinningapp.hook.Hook;
import com.hchen.pinningapp.utils.DexKit;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.MethodData;

import de.robv.android.xposed.XC_MethodHook;

public class ScLockApp extends Hook {
    boolean isListen = false;
    boolean isLock = false;
    Context context;

    @Override
    public void init() {
        DexKitBridge dexKitBridge = DexKit.init(loadPackageParam);
        try {

            MethodData methodData = dexKitBridge.findMethod(
                    FindMethod.create()
                            .matcher(MethodMatcher.create()
                                    .declaredClass(ClassMatcher.create()
                                            .usingStrings("startRegionSampling")
                                    )
                                    .name("dispatchTouchEvent")
                            )
            ).singleOrThrow(() -> new IllegalStateException("No such dispatchTouchEvent"));
            ClassData data = dexKitBridge.findClass(
                    FindClass.create()
                            .matcher(ClassMatcher.create()
                                    .usingStrings("startRegionSampling")
                            )
            ).singleOrThrow(() -> new IllegalStateException("No such Constructor"));
            try {
                // logE(tag, "dispatchTouchEvent: " + methodData + " Constructor: " + data + " class: " + data.getInstance(lpparam.classLoader));
                findAndHookConstructor(data.getInstance(loadPackageParam.classLoader),
                        Context.class,
                        new HookAction() {
                            @Override
                            protected void after(XC_MethodHook.MethodHookParam param) {
                                context = (Context) param.args[0];
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
            } catch (ClassNotFoundException e) {
                logE(tag, "hook Constructor E: " + data);
            }

            hookMethod(methodData.getMethodInstance(loadPackageParam.classLoader),
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            if (isLock) {
                                if (getSidebar(context) == 1) param.setResult(false);
                            }
                        }
                    }
            );
        } catch (Exception e) {
            logE(tag, "unknown E: " + e);
        }
        DexKit.close(dexKitBridge);
    }

    public int getLockApp(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), "key_lock_app");
        } catch (Settings.SettingNotFoundException e) {
            logE("LockApp", "getInt key_lock_app will set E: " + e);
        }
        return -1;
    }

    public int getSidebar(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), "lock_app_sidebar");
        } catch (Settings.SettingNotFoundException e) {
            logE("LockApp", "getInt lock_app_sidebar will set E: " + e);
            setSidebar(context);
        }
        return -1;
    }

    public void setSidebar(Context context) {
        Settings.Global.putInt(context.getContentResolver(), "lock_app_sidebar", 0);
    }
}
