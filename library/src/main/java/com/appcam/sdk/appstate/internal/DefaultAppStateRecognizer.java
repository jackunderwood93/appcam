package com.appcam.sdk.appstate.internal;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


import com.appcam.sdk.appstate.AppState;
import com.appcam.sdk.appstate.AppStateListener;

import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static com.appcam.sdk.appstate.AppState.BACKGROUND;
import static com.appcam.sdk.appstate.AppState.FOREGROUND;


public final class DefaultAppStateRecognizer implements AppStateRecognizer {

   private final CompositeAppStateListener compositeListener = new CompositeAppStateListener();
   private final ActivityLifecycleCallbacks activityStartedCallback = new ActivityStartedCallback();
   private final ComponentCallbacks2 uiHiddenCallback = new UiHiddenCallback();
   private final BroadcastReceiver screenOffBroadcastReceiver = new ScreenOffBroadcastReceiver();
   private final AtomicBoolean isFirstLaunch = new AtomicBoolean(true);

   private final Application app;

   private AppState appState = BACKGROUND;

  public DefaultAppStateRecognizer( Application app) {
    this.app = app;
  }

  @Override
  public void addListener( AppStateListener listener) {
    compositeListener.addListener(listener);
  }

  @Override
  public void removeListener( AppStateListener listener) {
    compositeListener.removeListener(listener);
  }

  @Override
  public void start() {
    app.registerActivityLifecycleCallbacks(activityStartedCallback);
    app.registerComponentCallbacks(uiHiddenCallback);
    app.registerReceiver(screenOffBroadcastReceiver, new IntentFilter(ACTION_SCREEN_OFF));
  }

  @Override
  public void stop() {
    app.unregisterActivityLifecycleCallbacks(activityStartedCallback);
    app.unregisterComponentCallbacks(uiHiddenCallback);
    app.unregisterReceiver(screenOffBroadcastReceiver);
  }

  
  @Override
  public AppState getAppState() {
    return appState;
  }

  private boolean isAppInForeground() {
    return appState == FOREGROUND;
  }

  private boolean isAppInBackground() {
    return appState == BACKGROUND;
  }

  private void onAppDidEnterForeground() {
    appState = FOREGROUND;
    compositeListener.onAppDidEnterForeground();
  }

  private void onAppDidEnterBackground() {
    appState = BACKGROUND;
    compositeListener.onAppDidEnterBackground();
  }

  private class ActivityStartedCallback extends NoOpActivityLifecycleCallbacks {

    @Override
    public void onActivityStarted(Activity activity) {
      if (isFirstLaunch.compareAndSet(true, false)) {
        onAppDidEnterForeground();
        return;
      }

      if (isAppInBackground()) {
        onAppDidEnterForeground();
      }
    }
  }

  private class UiHiddenCallback extends NoOpComponentCallbacks2 {

    @Override
    public void onTrimMemory(int level) {
      if (level >= TRIM_MEMORY_UI_HIDDEN && isAppInForeground()) {
        onAppDidEnterBackground();
      }
    }
  }

  private class ScreenOffBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      if (isAppInForeground()) {
        onAppDidEnterBackground();
      }
    }
  }
}
