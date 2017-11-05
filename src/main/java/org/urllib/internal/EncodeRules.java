package org.urllib.internal;

public class EncodeRules {

  public static final String UNRESERVED =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.-_~";

  // ; Removed from RFC 3986 since some servers interpret semicolon as a path parameter.
  public static final String PATH     = "!$&'()*+,:=@" + UNRESERVED;

  // ; Removed from RFC 3986 since some servers split the query on that.
  // + Removed from RFC 3986 since most servers change that to a space.
  public static final String QUERY    = "!$'()*,/:?@" + UNRESERVED;
  public static final String FRAGMENT = "!$&'()*+,/:;=?@" + UNRESERVED;

  // RFC 3986 doesn't discuss 'unsafe' characters. The text below is from RFC 1738.
  // Although their unsafe category has evolved, the description is useful.
  //
  //   Characters can be unsafe for a number of reasons.  The space
  //   character is unsafe because significant spaces may disappear and
  //   insignificant spaces may be introduced when URLs are transcribed or
  //   typeset or subjected to the treatment of word-processing programs.
  //   The characters "<" and ">" are unsafe because they are used as the
  //   delimiters around URLs in free text; the quote mark (""") is used to
  //   delimit URLs in some systems.  The character "#" is unsafe and should
  //   always be encoded because it is used in World Wide Web and in other
  //   systems to delimit a URL from a fragment/anchor identifier that might
  //   follow it.  The character "%" is unsafe because it is used for
  //   encodings of other characters.  Other characters are unsafe because
  //   gateways and other transport agents are known to sometimes modify
  //   such characters. These characters are "{", "}", "|", "\", "^", "~",
  //   "[", "]", and "`".
  //
  // The unsafe category here includes characters that are not explicitly defined
  // in RFC 3986. They cannot represent data in a URL, and should be encoded prior
  // to writing to the network.
  public static final String UNSAFE = "\"%<>\\^`{|}";

}
