package com.appcam.sdk.appstate.internal;


import com.appcam.sdk.appstate.AppStateListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


class CompositeAppStateListener implements AppStateListener {

   private final List<AppStateListener> listeners = new CopyOnWriteArrayList<>();

  @Override
  public void onAppDidEnterForeground() {
    for (AppStateListener listener : listeners) {
      listener.onAppDidEnterForeground();
    }
  }

  @Override
  public void onAppDidEnterBackground() {
    for (AppStateListener listener : listeners) {
      listener.onAppDidEnterBackground();
    }
  }

  void addListener( AppStateListener listener) {
    listeners.add(listener);
  }

  void removeListener( AppStateListener listener) {
    listeners.remove(listener);
  }
}
