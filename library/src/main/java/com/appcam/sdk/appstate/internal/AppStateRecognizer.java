package com.appcam.sdk.appstate.internal;

import com.appcam.sdk.appstate.AppState;
import com.appcam.sdk.appstate.AppStateListener;

public interface AppStateRecognizer {

  void addListener( AppStateListener listener);

  void removeListener( AppStateListener listener);

  void start();

  void stop();

   AppState getAppState();
}
