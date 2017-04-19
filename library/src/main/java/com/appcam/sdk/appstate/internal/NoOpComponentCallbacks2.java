package com.appcam.sdk.appstate.internal;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;

class NoOpComponentCallbacks2 implements ComponentCallbacks2 {

  @Override public void onTrimMemory(int level) {}
  @Override public void onConfigurationChanged(Configuration newConfig) {}
  @Override public void onLowMemory() {}
}
