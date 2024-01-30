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
package com.hchen.pinningapp.utils;

import org.luckypray.dexkit.DexKitBridge;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DexKit {

    public static DexKitBridge init(XC_LoadPackage.LoadPackageParam param) {
        String hostDir = param.appInfo.sourceDir;
        System.loadLibrary("dexkit");
        return DexKitBridge.create(hostDir);
    }

    public static void close(DexKitBridge dexKitBridge) {
        if (dexKitBridge != null) {
            dexKitBridge.close();
        }
    }
}
