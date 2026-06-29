package io.github.jqssun.displaymirror.job;

final class AirPlayAudioQuality {
  private static final int CAPTURE_BUFFER_MIN_FRAMES = 32;
  private static final int CAPTURE_BUFFER_MIN_MULTIPLIER = 4;
  private static final int OUTPUT_HEADROOM_NUMERATOR = 9;
  private static final int OUTPUT_HEADROOM_DENOMINATOR = 10;

  private AirPlayAudioQuality() {}

  static int captureBufferBytes(int minBufferBytes, int frameBytes) {
    if (minBufferBytes <= 0 || frameBytes <= 0) {
      return 0;
    }
    long androidMinimum = (long) minBufferBytes * CAPTURE_BUFFER_MIN_MULTIPLIER;
    long airPlayMinimum = (long) frameBytes * CAPTURE_BUFFER_MIN_FRAMES;
    long chosen = Math.max(androidMinimum, airPlayMinimum);
    return chosen > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) chosen;
  }

  static void applyOutputHeadroom(byte[] pcm, int size) {
    if (pcm == null || size <= 1) {
      return;
    }
    int limit = Math.min(size, pcm.length) & ~1;
    for (int i = 0; i < limit; i += 2) {
      int sample = (short) ((pcm[i] & 0xff) | (pcm[i + 1] << 8));
      int scaled =
          (sample * OUTPUT_HEADROOM_NUMERATOR
                  + (sample >= 0
                      ? OUTPUT_HEADROOM_DENOMINATOR / 2
                      : -OUTPUT_HEADROOM_DENOMINATOR / 2))
              / OUTPUT_HEADROOM_DENOMINATOR;
      pcm[i] = (byte) scaled;
      pcm[i + 1] = (byte) (scaled >> 8);
    }
  }
}
