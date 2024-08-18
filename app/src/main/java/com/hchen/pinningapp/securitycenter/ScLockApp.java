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
import android.view.View;

import com.hchen.pinningapp.hook.Hook;
import com.hchen.pinningapp.utils.DexKit;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindField;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.FieldData;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;

public class ScLockApp extends Hook {
    boolean isListen = false;
    boolean isLock = false;

    int value = 0;

    @Override
    public void init() {
        DexKitBridge dexKitBridge = DexKit.init(loadPackageParam);
        try {
            if (dexKitBridge == null) {
                logE(tag, "dexKitBridge is null");
                return;
            }
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
            FieldData fieldData = null;
            if (methodData == null) {
                value = 1;
                methodData = dexKitBridge.findMethod(
                        FindMethod.create()
                                .matcher(MethodMatcher.create()
                                        .declaredClass(ClassMatcher.create()
                                                .usingStrings("SidebarTouchListener")
                                        )
                                        .name("onTouch")
                                )
                ).singleOrNull();
                data = dexKitBridge.findClass(
                        FindClass.create()
                                .matcher(ClassMatcher.create()
                                        .usingStrings("onTouch: \taction = ")
                                )
                ).singleOrNull();
                fieldData = dexKitBridge.findField(
                        FindField.create()
                                .matcher(FieldMatcher.create()
                                        .declaredClass(ClassMatcher.create()
                                                .usingStrings("onTouch: \taction = ")
                                        )
                                        .type(View.class)
                                )
                ).singleOrNull();
            }
            try {
                Field field = null;
                if (data == null) {
                    logE(tag, "Class is null");
                    return;
                }
                if (fieldData == null && value == 1) {
                    logE(tag, "Field is null");
                    return;
                } else if (fieldData != null)
                    field = fieldData.getFieldInstance(loadPackageParam.classLoader);
                // logE(tag, "data: " + data + " fieldData: " + fieldData + " methodData: " + methodData);
                Field finalField = field;
                hookAllConstructors(data.getInstance(loadPackageParam.classLoader), new HookAction() {
                    @Override
                    protected void after(MethodHookParam param) {
                        Context context = null;
                        if (value == 1) {
                            try {
                                if (finalField == null) {
                                    logE(tag, "finalField is null!");
                                    return;
                                }
                                context = ((View) finalField.get(param.thisObject)).getContext();
                            } catch (IllegalAccessException e) {
                                logE(tag, "getContext E: " + e);
                            }
                        } else {
                            context = (Context) param.args[0];
                        }
                        if (context == null) {
                            logE(tag, "Context is null");
                            return;
                        }
                        if (!isListen) {
                            Context finalContext = context;
                            ContentObserver contentObserver = new ContentObserver(new Handler(finalContext.getMainLooper())) {
                                @Override
                                public void onChange(boolean selfChange) {
                                    isLock = getLockApp(finalContext) != -1;
                                }
                            };
                            context.getContentResolver().registerContentObserver(
                                    Settings.Global.getUriFor("key_lock_app"),
                                    false, contentObserver);
                            isListen = true;
                        }
                    }
                });
            } catch (ClassNotFoundException | NoSuchFieldException e) {
                logE(tag, "hook Constructor E: " + data);
            }

            if (methodData == null) {
                logE(tag, "Method is null");
                return;
            }
            hookMethod(methodData.getMethodInstance(loadPackageParam.classLoader),
                    new HookAction() {
                        @Override
                        protected void before(XC_MethodHook.MethodHookParam param) {
                            if (isLock) {
                                param.setResult(false);
                            }
                        }
                    }
            );
        } catch (Exception e) {
            logE(tag, "unknown E: " + e);
        }
        DexKit.close(dexKitBridge);
    }

    public static int getLockApp(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), "key_lock_app");
        } catch (Settings.SettingNotFoundException e) {
            logE("LockApp", "getLockApp will set E: " + e);
        }
        return -1;
    }
}
