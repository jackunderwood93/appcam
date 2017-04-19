package com.appcam.sdk.appstate;

import android.app.Application;

import com.appcam.sdk.appstate.internal.AppStateRecognizer;
import com.appcam.sdk.appstate.internal.DefaultAppStateRecognizer;

import static com.appcam.sdk.appstate.AppState.BACKGROUND;
import static com.appcam.sdk.appstate.AppState.FOREGROUND;


/**
 * An app state monitor that keeps track of whenever the application
 * goes into background and comes back into foreground.
 */
@SuppressWarnings({"unused"})
public final class AppStateMonitor {

   private final AppStateRecognizer recognizer;

  /**
   * Creates a new {@link AppStateMonitor} instance for the given {@link Application}.
   *
   * @param app the application to monitor for app state changes
   * @return a new {@link AppStateMonitor} instance
   */
  
  public static AppStateMonitor create( Application app) {
    return new AppStateMonitor(app);
  }

  private AppStateMonitor( Application app) {
    this.recognizer = new DefaultAppStateRecognizer(app);
  }

  AppStateMonitor( AppStateRecognizer recognizer) {
    this.recognizer = recognizer;
  }

  /**
   * Starts monitoring the app for background / foreground changes.
   */
  public void start() {
    recognizer.start();
  }

  /**
   * Stops monitoring the app for background / foreground changes.
   */
  public void stop() {
    recognizer.stop();
  }

  /**
   * Adds a new {@link AppStateListener} to the app state monitor.
   *
   * @param appStateListener the listener to add
   */
  public void addListener( AppStateListener appStateListener) {
    recognizer.addListener(appStateListener);
  }

  /**
   * Removes the specified {@link AppStateListener} from the app state monitor.
   *
   * @param appStateListener the listener to remove
   */
  public void removeListener( AppStateListener appStateListener) {
    recognizer.removeListener(appStateListener);
  }

  /**
   * Checks whether the app is currently in the foreground.
   *
   * @return {@code true} if the app is currently in the foreground, {@code false} otherwise
   */
  public boolean isAppInForeground() {
    return recognizer.getAppState() == FOREGROUND;
  }

  /**
   * Checks whether the app is currently in the background.
   *
   * @return {@code true} if the app is currently in the background, {@code false} otherwise
   */
  public boolean isAppInBackground() {
    return recognizer.getAppState() == BACKGROUND;
  }
}