package ru.superplushkin.twofactorauthapp.subclasses;

import android.app.Activity;
import android.os.Build;

public class TransitionHelper {
    @SuppressWarnings("deprecation")
    public static void setOnStart(Activity activity, int enterAnim) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, enterAnim, 0);
        } else {
            activity.overridePendingTransition(enterAnim, 0);
        }
    }

    @SuppressWarnings("deprecation")
    public static void setOnClose(Activity activity, int exitAnim) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, exitAnim);
        } else {
            activity.overridePendingTransition(0, exitAnim);
        }
    }
}