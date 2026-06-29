package io.github.jqssun.displaymirror;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.VolumeProvider;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import io.github.jqssun.displaymirror.job.AirPlayService;

public class AirPlayForegroundService extends Service {
  private static final String ACTION_STOP =
      "io.github.jqssun.displaymirror.action.STOP_ANDROID2APPLE4MUSIC";
  private static final String CHANNEL_ID = "Android2Apple4MusicServiceChannel";
  private static final int NOTIFICATION_ID = 3;
  private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
  private static AirPlayForegroundService instance;
  private PowerManager.WakeLock wakeLock;
  private MediaProjection activeProjection;
  private MediaProjection.Callback projectionCallback;
  private MediaSession mediaSession;
  private VolumeProvider remoteVolumeProvider;

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
    _createChannel();
    _acquireWakeLock();
    _startVolumeSession();
    startForeground(NOTIFICATION_ID, _buildNotification());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && ACTION_STOP.equals(intent.getAction())) {
      stopSelf();
      return START_NOT_STICKY;
    }
    if (intent != null && intent.hasExtra("data")) {
      MediaProjectionManager mpm =
          (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
      Intent data = intent.getParcelableExtra("data");
      if (mpm != null && data != null) {
        MediaProjection proj = mpm.getMediaProjection(RESULT_OK, data);
        if (proj != null) {
          _setActiveProjection(proj);
          AirPlayService.getInstance().onProjectionReady(proj);
        }
      }
    }
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    _clearActiveProjection();
    AirPlayService.getInstance().disconnect();
    _releaseVolumeSession();
    _releaseWakeLock();
    if (instance == this) {
      instance = null;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  public static void syncRemoteVolume(int percent) {
    AirPlayForegroundService service = instance;
    if (service == null) {
      return;
    }
    MAIN_HANDLER.post(
        () -> {
          if (service.remoteVolumeProvider != null) {
            service.remoteVolumeProvider.setCurrentVolume(percent);
          }
        });
  }

  private void _createChannel() {
    NotificationChannel channel =
        new NotificationChannel(
            CHANNEL_ID, getString(R.string.mirror_app_name), NotificationManager.IMPORTANCE_LOW);
    NotificationManager nm = getSystemService(NotificationManager.class);
    if (nm != null) nm.createNotificationChannel(channel);
  }

  private Notification _buildNotification() {
    int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
    PendingIntent openAppIntent =
        PendingIntent.getActivity(
            this, 0, new Intent(this, MirrorMainActivity.class), pendingIntentFlags);
    PendingIntent stopIntent =
        PendingIntent.getService(
            this,
            1,
            new Intent(this, AirPlayForegroundService.class).setAction(ACTION_STOP),
            pendingIntentFlags);
    return new NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(getString(R.string.mirror_app_name))
        .setContentText(getString(R.string.airplay_notification_text))
        .setSmallIcon(android.R.drawable.ic_menu_send)
        .setContentIntent(openAppIntent)
        .addAction(
            android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.stop), stopIntent)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .build();
  }

  @SuppressLint("WakelockTimeout")
  private void _acquireWakeLock() {
    if (wakeLock != null && wakeLock.isHeld()) {
      return;
    }
    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    if (powerManager == null) {
      return;
    }
    wakeLock =
        powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK, getPackageName() + ":Android2Apple4MusicAudioRoute");
    wakeLock.setReferenceCounted(false);
    wakeLock.acquire();
  }

  private void _releaseWakeLock() {
    if (wakeLock != null) {
      if (wakeLock.isHeld()) {
        wakeLock.release();
      }
      wakeLock = null;
    }
  }

  private void _startVolumeSession() {
    AirPlayService airplay = AirPlayService.getInstance();
    remoteVolumeProvider =
        new VolumeProvider(
            VolumeProvider.VOLUME_CONTROL_ABSOLUTE, 100, airplay.getVolumePercent()) {
          @Override
          public void onAdjustVolume(int direction) {
            setCurrentVolume(AirPlayService.getInstance().adjustVolume(direction));
          }

          @Override
          public void onSetVolumeTo(int volume) {
            AirPlayService.getInstance().setVolumePercent(volume);
            setCurrentVolume(AirPlayService.getInstance().getVolumePercent());
          }
        };

    mediaSession = new MediaSession(this, getString(R.string.mirror_app_name));
    mediaSession.setCallback(
        new MediaSession.Callback() {
          @Override
          public void onStop() {
            stopSelf();
          }
        });
    mediaSession.setPlaybackToRemote(remoteVolumeProvider);
    mediaSession.setPlaybackState(
        new PlaybackState.Builder()
            .setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1f)
            .setActions(PlaybackState.ACTION_STOP)
            .build());
    mediaSession.setActive(true);
  }

  private void _releaseVolumeSession() {
    remoteVolumeProvider = null;
    if (mediaSession != null) {
      mediaSession.setActive(false);
      mediaSession.release();
      mediaSession = null;
    }
  }

  private void _setActiveProjection(MediaProjection projection) {
    _clearActiveProjection();
    activeProjection = projection;
    projectionCallback =
        new MediaProjection.Callback() {
          @Override
          public void onStop() {
            stopSelf();
          }
        };
    activeProjection.registerCallback(projectionCallback, new Handler(Looper.getMainLooper()));
  }

  private void _clearActiveProjection() {
    if (activeProjection != null && projectionCallback != null) {
      try {
        activeProjection.unregisterCallback(projectionCallback);
      } catch (Exception ignored) {
      }
    }
    activeProjection = null;
    projectionCallback = null;
  }
}
