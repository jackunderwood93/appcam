package com.appcam.sdk.appstate;

/**
 * Listener for app state changes.
 */
public interface AppStateListener {

  /**
   * Called whenever the app comes into foreground.
   */
  void onAppDidEnterForeground();

  /**
   * Called whenever the app goes into background.
   */
  void onAppDidEnterBackground();

  //@formatter:off
  AppStateListener NO_OP = new AppStateListener() {
    @Override public void onAppDidEnterForeground() {}
    @Override public void onAppDidEnterBackground() {}
  };//@formatter:on
}