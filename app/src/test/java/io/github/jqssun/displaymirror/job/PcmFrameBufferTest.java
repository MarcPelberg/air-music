package io.github.jqssun.displaymirror.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PcmFrameBufferTest {
  public static void main(String[] args) {
    List<byte[]> frames = new ArrayList<>();
    PcmFrameBuffer buffer = new PcmFrameBuffer(4, frames::add);

    buffer.write(new byte[] {1, 2, 3}, 3);
    assertEquals(0, frames.size(), "partial writes should not emit a frame");

    byte[] second = new byte[] {4, 5, 6, 7, 8, 9};
    buffer.write(second, second.length);
    second[0] = 99;

    assertEquals(2, frames.size(), "writes should emit every complete frame");
    assertArrayEquals(new byte[] {1, 2, 3, 4}, frames.get(0), "first frame");
    assertArrayEquals(new byte[] {5, 6, 7, 8}, frames.get(1), "second frame");

    buffer.write(new byte[] {10, 11, 12}, 3);
    assertEquals(3, frames.size(), "remainder should carry into the next write");
    assertArrayEquals(new byte[] {9, 10, 11, 12}, frames.get(2), "third frame");
  }

  private static void assertEquals(int expected, int actual, String message) {
    if (expected != actual) {
      throw new AssertionError(message + ": got " + actual + ", want " + expected);
    }
  }

  private static void assertArrayEquals(byte[] expected, byte[] actual, String message) {
    if (!Arrays.equals(expected, actual)) {
      throw new AssertionError(
          message + ": got " + Arrays.toString(actual) + ", want " + Arrays.toString(expected));
    }
  }
}
