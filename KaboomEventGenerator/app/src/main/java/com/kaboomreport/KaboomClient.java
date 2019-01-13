package com.kaboomreport;

import android.content.Context;

/**
 * The facade for the Kaboom client.
 */
public final class KaboomClient {
    private KaboomClient() {}

    public static void configure(String appCode) {
        Orchestrator.configure(appCode);
    }

    public static void reportLaunch(Context context) {
        Orchestrator.reportLaunch(context);
    }

    public static void saveCrashInfo(Throwable e, Context context) {
        Orchestrator.saveCrashInfo(e, context);
    }

    public static void reportLastSavedCrash(Context context) {
        Orchestrator.reportLastSavedCrash(context);
    }
}
