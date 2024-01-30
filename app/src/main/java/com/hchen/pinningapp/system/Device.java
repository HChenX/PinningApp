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
package com.hchen.pinningapp.system;

import com.hchen.pinningapp.hook.Log;

import java.lang.reflect.Field;

import de.robv.android.xposed.XposedHelpers;

public class Device {
    public static boolean isPad() {
        Class<?> build = XposedHelpers.findClassIfExists("miui.os.Build", null);
        if (build == null) {
            Log.logE("Device", "Class miui.os.Build is null");
            return false;
        }
        try {
            Field tablet = build.getDeclaredField("IS_TABLET");
            tablet.setAccessible(true);
            return tablet.getBoolean(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            try {
                return (boolean) XposedHelpers.getStaticObjectField(build, "IS_TABLET");
            } catch (Exception f) {
                Log.logE("Device", "Failed get IS_TABLET");
                return false;
            }
        }
    }
}
