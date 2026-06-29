package io.github.jqssun.displaymirror.job;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import io.github.jqssun.displaymirror.State;

public class AirPlayAudioCapture {
  private static final String TAG = "AirPlayAudioCapture";
  private static final int SAMPLE_RATE = 44100;
  private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
  private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
  private static final int AIRPLAY_SAMPLES_PER_FRAME = 352;
  private static final int CHANNELS = 2;
  private static final int BYTES_PER_SAMPLE = 2;
  private static final int AIRPLAY_FRAME_BYTES =
      AIRPLAY_SAMPLES_PER_FRAME * CHANNELS * BYTES_PER_SAMPLE;

  private AudioRecord audioRecord;
  private Thread readThread;
  private volatile boolean running;

  public boolean start(Context context, MediaProjection projection, airplaylib.Session session) {
    if (running) return true;
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      State.log("AirPlay audio: Android version too low for playback capture");
      return false;
    }
    if (context == null || projection == null || session == null) {
      State.log("AirPlay audio: missing projection or session");
      return false;
    }

    try {
      int minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, ENCODING);
      if (minBuffer <= 0) {
        State.log("AirPlay audio: invalid minimum buffer size " + minBuffer);
        return false;
      }

      AudioFormat audioFormat =
          new AudioFormat.Builder()
              .setEncoding(ENCODING)
              .setSampleRate(SAMPLE_RATE)
              .setChannelMask(CHANNEL_CONFIG)
              .build();
      android.media.AudioPlaybackCaptureConfiguration config =
          new android.media.AudioPlaybackCaptureConfiguration.Builder(projection)
              .excludeUsage(AudioAttributes.USAGE_ALARM)
              .build();

      audioRecord =
          new AudioRecord.Builder()
              .setAudioPlaybackCaptureConfig(config)
              .setAudioFormat(audioFormat)
              .setBufferSizeInBytes(
                  AirPlayAudioQuality.captureBufferBytes(minBuffer, AIRPLAY_FRAME_BYTES))
              .build();
      audioRecord.startRecording();

      running = true;
      PcmFrameBuffer frameBuffer = new PcmFrameBuffer(AIRPLAY_FRAME_BYTES, session::sendAudioPCM);
      readThread = new Thread(() -> readLoop(frameBuffer), "AirPlayAudioCapture");
      readThread.start();
      State.log("AirPlay audio: capture started at 44.1 kHz stereo PCM");
      return true;
    } catch (Exception e) {
      Log.e(TAG, "start failed", e);
      State.log("AirPlay audio failed: " + e.getMessage());
      stop();
      return false;
    }
  }

  public void stop() {
    running = false;
    if (audioRecord != null) {
      try {
        audioRecord.stop();
      } catch (Exception ignored) {
      }
      try {
        audioRecord.release();
      } catch (Exception ignored) {
      }
      audioRecord = null;
    }
    if (readThread != null) {
      readThread.interrupt();
      readThread = null;
    }
  }

  private void readLoop(PcmFrameBuffer frameBuffer) {
    try {
      Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
    } catch (Exception e) {
      State.log("AirPlay audio: priority hint failed - " + e.getMessage());
    }
    byte[] buffer = new byte[AIRPLAY_FRAME_BYTES * 4];
    while (running && audioRecord != null) {
      int read = audioRecord.read(buffer, 0, buffer.length, AudioRecord.READ_BLOCKING);
      if (read > 0) {
        AirPlayAudioQuality.applyOutputHeadroom(buffer, read);
        frameBuffer.write(buffer, read);
      } else if (read < 0) {
        State.log("AirPlay audio read error: " + read);
        return;
      }
    }
  }
}
