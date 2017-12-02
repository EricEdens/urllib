package org.urllib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.common.base.Function;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.urllib.internal.EncodeRules;
import org.urllib.internal.UrllibUrl;

public class TestEncodingRules {

  private static final Rule[] PATH = new Rule[0x80];
  private static final Rule[] QUERY = new Rule[0x80];
  private static final Rule[] FRAGMENT = new Rule[0x80];

  @BeforeClass
  public static void importRules() throws IOException {
    Scanner scanner = new Scanner(Paths.get("src/main/resources", "encode-set.tsv"));
    // Throw away header.
    scanner.nextLine();
    while (scanner.hasNextLine()) {
      Integer point = Integer.decode(scanner.next());
      String rep = scanner.next();
      PATH[point] = Rule.valueOf(scanner.next());
      QUERY[point] = Rule.valueOf(scanner.next());
      FRAGMENT[point] = Rule.valueOf(scanner.next());
    }
  }

  @Test public void pathObjectFromUrlBuilder() {
    Function<String, String> codepoint = new Function<String, String>() {
      @Override public String apply(String codepoint) {
        Url url = Urls.http("host.com").path("_" + codepoint).create();
        String path = url.path().toString();
        return path.substring(2, path.length());
      }
    };
    run(codepoint, PATH);
  }

  @Test public void pathFromUrlBuilder() {
    Function<String, String> codepoint = new Function<String, String>() {
      @Override public String apply(String codepoint) {
        Url url = Urls.http("host.com").path("_" + codepoint).create();
        return url.toString().replace("http://host.com/_", "");
      }
    };
    run(codepoint, PATH);
  }

  @Test public void queryObjectFromUrlBuilder() {
    Function<String, String> codepoint = new Function<String, String>() {
      @Override public String apply(String codepoint) {
        Url url = Urls.http("host.com").query("key", codepoint).create();
        return url.query().toString().replace("key=", "");
      }
    };
    run(codepoint, QUERY);
  }

  @Test public void queryFromUrlBuilder() {
    Function<String, String> codepoint = new Function<String, String>() {
      @Override public String apply(String codepoint) {
        Url url = Urls.http("host.com").query("key", codepoint).create();
        return url.toString().replace("http://host.com/?key=", "");
      }
    };
    run(codepoint, QUERY);
  }

  @Test public void fragmentFromBuilder() {
    Function<String, String> codepoint = new Function<String, String>() {
      @Override public String apply(String codepoint) {
        Url url = Urls.http("host.com").fragment(codepoint).create();
        return url.toString().replace("http://host.com/#", "");
      }
    };
    run(codepoint, FRAGMENT);
  }

  @Test public void unsafeShouldAlwaysBeEncoded() {
    for (int i = 0; i < EncodeRules.UNSAFE.length(); i++) {
      int codepoint = EncodeRules.UNSAFE.codePointAt(i);
      if (codepoint == '\\') {
        assertNotEquals(Rule.NONE, PATH[codepoint]);
      } else {
        assertEquals(Rule.PERC, PATH[codepoint]);
      }

      assertEquals(Rule.PERC, QUERY[codepoint]);
      assertEquals(Rule.PERC, FRAGMENT[codepoint]);
    }
  }

  @Test public void unreservedShouldNeverBeEncoded() {
    for (int i = 0; i < EncodeRules.UNRESERVED.length(); i++) {
      int codepoint = EncodeRules.UNRESERVED.codePointAt(i);
      assertEquals(Rule.NONE, PATH[codepoint]);
      assertEquals(Rule.NONE, QUERY[codepoint]);
      assertEquals(Rule.NONE, FRAGMENT[codepoint]);
    }
  }

  private void run(Function<String, String> codepoint, Rule[] path) {
    for (char c = 0; c < 0x80; c++) {
      String expected;
      switch (path[c]) {
        case NONE:
          expected = "" + c;
          break;
        case PERC:
          expected = String.format("%%%02X", (int) c);
          break;
        case SLSH:
          expected = "/";
          break;
        case PLUS:
          expected = "+";
          break;
        default:
          throw new AssertionError(path[c]);
      }
      String actual = codepoint.apply("" + c);
      assertEquals(expected, actual);
    }

  }

  private enum Rule {
    NONE, PERC, SLSH, PLUS
  }
}
