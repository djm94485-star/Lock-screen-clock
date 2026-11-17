package com.example.clockresizer;

import android.view.View;
import android.view.ViewGroup;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {
    // Clock containers in AOSP A16 keyguard (large = big lock screen clock, small = when notifications present)
    private static final String[] IDS = {"lockscreen_clock_view_large", "lockscreen_clock_view"};

    @Override
    public void handleLoadPackage(LoadPackageParam lp) {
        if (!"com.android.systemui".equals(lp.packageName)) return;
        Class<?> c = XposedHelpers.findClassIfExists("com.android.keyguard.KeyguardClockSwitch", lp.classLoader);
        if (c == null) { XposedBridge.log("ClockResizer: KeyguardClockSwitch not found"); return; }
        XposedBridge.hookAllMethods(c, "onFinishInflate", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam p) {
                try {
                    ViewGroup sw = (ViewGroup) p.thisObject;
                    for (String name : IDS) {
                        int id = sw.getResources().getIdentifier(name, "id", "com.android.systemui");
                        View v = id == 0 ? null : sw.findViewById(id);
                        if (v != null) new ClockResizer(v, name).install();
                        else XposedBridge.log("ClockResizer: id not found " + name);
                    }
                } catch (Throwable t) { XposedBridge.log(t); }
            }
        });
    }
}
