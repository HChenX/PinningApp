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
package com.hchen.pinningapp;

import com.hchen.pinningapp.home.LockApp;
import com.hchen.pinningapp.hook.Hook;
import com.hchen.pinningapp.securitycenter.ScLockApp;
import com.hchen.pinningapp.system.SyLockApp;
import com.hchen.pinningapp.systemui.UiLockApp;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookMain implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    public static String modulePath;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        switch (lpparam.packageName) {
            case "android" -> {
                initHook(new SyLockApp(), lpparam);
            }
            case "com.miui.home" -> {
                initHook(new LockApp(), lpparam);
            }
            case "com.android.systemui" -> {
                initHook(new UiLockApp(), lpparam);
            }
            case "com.miui.securitycenter" -> {
                initHook(new ScLockApp(), lpparam);
            }
        }
    }

    public static void initHook(Hook hook, LoadPackageParam param) {
        hook.runHook(param);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        modulePath = startupParam.modulePath;
    }
}
