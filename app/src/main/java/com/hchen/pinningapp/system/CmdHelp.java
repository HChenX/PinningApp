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

import android.content.Context;
import android.provider.Settings;

import com.hchen.pinningapp.hook.Hook;
import com.hchen.pinningapp.hook.Log;

import java.io.PrintWriter;

public class CmdHelp extends Hook {
    Context mContext;

    int lockScreenCount = 0;
    int sidebarCount = 0;

    @Override
    public void init() {
        findAndHookMethod("com.android.server.pm.PackageManagerShellCommand",
                "onCommand", String.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        String cmd = (String) param.args[0];
                        mContext = (Context) getObjectField(param.thisObject, "mContext");
                        if (mContext == null) {
                            mContext = findContext(FlAG_ONLY_ANDROID);
                            if (mContext == null) {
                                logE(tag, "onCommand context is null!!");
                                return;
                            }
                        }
                        if (cmd == null) return;
                        if ("pinning".equals(cmd)) {
                            PrintWriter getOutPrintWriter = (PrintWriter) callMethod(param.thisObject, "getOutPrintWriter");
                            String getNextOption = (String) callMethod(param.thisObject, "getNextOption");
                            if (getOutPrintWriter == null) {
                                Log.logE(tag, "onCommand getOutPrintWriter is null!!");
                                param.setResult(-1);
                                return;
                            }
                            if (getNextOption == null) {
                                getOutPrintWriter.println("[pinning] must be followed by an option! For details, please refer to -h");
                                param.setResult(-1);
                                return;
                            }
                            switch (getNextOption) {
                                case "-h", "-help" -> {
                                    help(getOutPrintWriter);
                                    param.setResult(0);
                                }
                                case "-l", "-lockScreen" -> {
                                    try {
                                        String next = (String) callMethod(param.thisObject, "getNextArgRequired");
                                        if ("-g".equals(next)) {
                                            getOutPrintWriter.println("exit_lock_app is " + (getMyLockScreen(mContext) == 1 ? "1 Enable" : "0 Close"));
                                            param.setResult(0);
                                            return;
                                        }
                                        lockScreen(getOutPrintWriter, Integer.parseInt(next));
                                    } catch (IllegalArgumentException e) {
                                        getOutPrintWriter.println("-l must be followed by a numerical parameter! For details, please refer to - h\n" + e);
                                        param.setResult(-1);
                                        return;
                                    }
                                    param.setResult(0);
                                }
                                case "-s", "-sidebar" -> {
                                    try {
                                        String next = (String) callMethod(param.thisObject, "getNextArgRequired");
                                        if ("-g".equals(next)) {
                                            getOutPrintWriter.println("lock_app_sidebar is " + (getSidebar(mContext) == 1 ? "1 Enable" : "0 Close"));
                                            param.setResult(0);
                                            return;
                                        }
                                        sidebar(getOutPrintWriter, Integer.parseInt(next));
                                    } catch (IllegalArgumentException e) {
                                        getOutPrintWriter.println("-s must be followed by a numerical parameter! For details, please refer to - h\n" + e);
                                        param.setResult(-1);
                                        return;
                                    }
                                    param.setResult(0);
                                }
                                default -> {
                                    getOutPrintWriter.println("Unknown option! Please refer to the -h content settings!");
                                    param.setResult(-1);
                                }
                            }
                        }
                    }
                }
        );
    }

    public void help(PrintWriter printWriter) {
        printWriter.println("PinningApp Helper: ");
        printWriter.println("    [-l | lockScreen <value>]: ");
        printWriter.println("    Set this parameter to manage whether to lock the screen when canceling pinning app.");
        printWriter.println("    sample:[pm pinning -l 0] (Turn off this feature)");
        printWriter.println("    sample:[pm pinning -l 1] (Enable this feature)");
        printWriter.println("    设置此参数即可管理是否在取消固定应用时锁定屏幕。");
        printWriter.println("    举例:[pm pinning -l 0] (关闭此功能)");
        printWriter.println("    举例:[pm pinning -l 1] (开启此功能)");
        printWriter.println("-------------------------------------");
        printWriter.println("    [-l | lockScreen -g]: ");
        printWriter.println("    Obtain the current switch status of this function.");
        printWriter.println("    sample:[pm pinning -l -g]");
        printWriter.println("    获取此功能的当前开关状态。");
        printWriter.println("    举例:[pm pinning -l -g]");
        printWriter.println("-------------------------------------");
        printWriter.println("    [-s | sidebar <value>]: ");
        printWriter.println("    Set this parameter to manage whether to reject pop-up sidebars when using pinning app.");
        printWriter.println("    sample:[pm pinning -s 0] (Turn off this feature)");
        printWriter.println("    sample:[pm pinning -s 1] (Enable this feature)");
        printWriter.println("    设置此参数即可管理是否在固定应用时拒绝弹出侧边栏。");
        printWriter.println("    举例:[pm pinning -s 0] (关闭此功能)");
        printWriter.println("    举例:[pm pinning -s 1] (开启此功能)");
        printWriter.println("-------------------------------------");
        printWriter.println("    [-s | sidebar -g]: ");
        printWriter.println("    Obtain the current switch status of this function.");
        printWriter.println("    sample:[pm pinning -s -g]");
        printWriter.println("    获取此功能的当前开关状态。");
        printWriter.println("    举例:[pm pinning -s -g]");
        printWriter.println("-------------------------------------");
        printWriter.println("From PinningApp, Version v.1.2, Author: HChenX");
    }

    public void lockScreen(PrintWriter pw, int value) {
        if (mContext != null) {
            if (value != 1 && value != 0) {
                pw.println("Only 0 or 1 can be set!!");
                return;
            }
            switch (getMyLockScreen(mContext)) {
                case -1 -> {
                    if (lockScreenCount < 3) {
                        lockScreen(pw, value);
                        lockScreenCount = lockScreenCount + 1;
                    } else {
                        lockScreenCount = 0;
                        pw.println("Attempted to set exit_lock_app multiple times, but failed!");
                    }
                }
                case 0 -> {
                    if (value != 0)
                        setMyLockScreen(pw, mContext, value);
                    else
                        pw.println("exit_lock_app is already 0, there is no need to set it again!");
                }
                case 1 -> {
                    if (value != 1)
                        setMyLockScreen(pw, mContext, value);
                    else
                        pw.println("exit_lock_app is already 1, there is no need to set it again!");
                }
                default -> {
                    pw.println("Unknown value obtained from exit_lock_app, restoring to 0!");
                    setMyLockScreen(pw, mContext, 0);
                }
            }
        } else {
            pw.println("Context is null! Unable to complete the setup, please try again!");
        }
    }

    public void sidebar(PrintWriter pw, int value) {
        if (mContext != null) {
            if (value != 1 && value != 0) {
                pw.println("Only 0 or 1 can be set!!");
                return;
            }
            switch (getSidebar(mContext)) {
                case -1 -> {
                    if (sidebarCount < 3) {
                        sidebar(pw, value);
                        sidebarCount = sidebarCount + 1;
                    } else {
                        sidebarCount = 0;
                        pw.println("Attempted to set lock_app_sidebar multiple times, but failed!");
                    }
                }
                case 0 -> {
                    if (value != 0) setSidebar(pw, mContext, value);
                    else
                        pw.println("lock_app_sidebar is already 0, there is no need to set it again!");
                }
                case 1 -> {
                    if (value != 1) setSidebar(pw, mContext, value);
                    else
                        pw.println("lock_app_sidebar is already 1, there is no need to set it again!");
                }
                default -> {
                    pw.println("Unknown value obtained from lock_app_sidebar, restoring to 0!");
                    setMyLockScreen(pw, mContext, 0);
                }
            }
        } else {
            pw.println("Context is null! Unable to complete the setup, please try again!");
        }
    }

    private int getMyLockScreen(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), "exit_lock_app_screen");
        } catch (Settings.SettingNotFoundException e) {
            logE(tag, "getMyLockScreen is null will set 0 E:" + e);
            setMyLockScreen(null, context, 0);
        }
        return -1;
    }

    public int getSidebar(Context context) {
        try {
            return Settings.Global.getInt(context.getContentResolver(), "lock_app_sidebar");
        } catch (Settings.SettingNotFoundException e) {
            logE("LockApp", "getInt lock_app_sidebar is null will set 0 E: " + e);
            setSidebar(null, context, 0);
        }
        return -1;
    }

    public void setSidebar(PrintWriter pw, Context context, int value) {
        Settings.Global.putInt(context.getContentResolver(), "lock_app_sidebar", value);
        if (pw != null) {
            pw.println("Successfully set lock_app_sidebar to " + value);
        }
    }

    private void setMyLockScreen(PrintWriter pw, Context context, int value) {
        Settings.Global.putInt(context.getContentResolver(), "exit_lock_app_screen", value);
        if (pw != null) {
            pw.println("Successfully set exit_lock_app_screen to " + value);
        }
    }
}
