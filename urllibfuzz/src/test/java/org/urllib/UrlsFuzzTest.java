package org.urllib;

import java.util.Random;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;

public class UrlsFuzzTest {

  private static final Random RAND = new Random();
  private static final int RUN_LEN = 10000;
  private static final RandomStringGenerator UNFILTERED =
      new RandomStringGenerator.Builder().build();
  private static final RandomStringGenerator ASCII =
      new RandomStringGenerator.Builder().withinRange(0x00, 0x7F).build();
  private static final RandomStringGenerator ASCII_PRINTABLE =
      new RandomStringGenerator.Builder().withinRange(0x20, 0x7E).build();

  @Test public void minimalEscape() {
    for (int i = 0; i < RUN_LEN; i++) {
      int length = RAND.nextInt(20);
      Urls.minimalEscape("http://host.com/" + UNFILTERED.generate(length));
      Urls.minimalEscape("http://host.com/" + ASCII.generate(length));
      Urls.minimalEscape("http://host.com/" + ASCII_PRINTABLE.generate(length));
    }
  }

  @Test public void createURI() {
    for (int i = 0; i < RUN_LEN; i++) {
      int length = RAND.nextInt(20);
      Urls.createURI("http://host.com/" + UNFILTERED.generate(length));
      Urls.createURI("http://host.com/" + ASCII.generate(length));
      Urls.createURI("http://host.com/" + ASCII_PRINTABLE.generate(length));
    }
  }
}
