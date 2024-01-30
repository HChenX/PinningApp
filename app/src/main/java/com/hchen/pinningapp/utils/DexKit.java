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
