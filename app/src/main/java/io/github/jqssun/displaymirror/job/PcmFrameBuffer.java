package io.github.jqssun.displaymirror.job;

import java.util.Arrays;

public class PcmFrameBuffer {
  public interface FrameSink {
    void onFrame(byte[] frame);
  }

  private final byte[] pending;
  private final FrameSink sink;
  private int pendingSize;

  public PcmFrameBuffer(int frameBytes, FrameSink sink) {
    this.pending = new byte[frameBytes];
    this.sink = sink;
  }

  public void write(byte[] data, int size) {
    int offset = 0;
    while (offset < size) {
      int copy = Math.min(size - offset, pending.length - pendingSize);
      System.arraycopy(data, offset, pending, pendingSize, copy);
      offset += copy;
      pendingSize += copy;

      if (pendingSize == pending.length) {
        sink.onFrame(Arrays.copyOf(pending, pending.length));
        pendingSize = 0;
      }
    }
  }
}
