package org.urllib;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;

public class UrlsTest {

  @Test public void minimalEscapeTrimsWhitespace() {
    String expected = "http://host";
    assertEquals(expected, Urls.escape(" http://host "));
    assertEquals(expected, Urls.escape("   http://host "));
    assertEquals(expected, Urls.escape(" http://host\n"));
    assertEquals(expected, Urls.escape("\thttp://host\n"));
    assertEquals(expected, Urls.escape("\fhttp://host\n"));
    assertEquals(expected, Urls.escape("\fhttp://host\n\r"));
  }

  @Test public void createURI() {
    assertEquals("http://host/path/", Urls.createURI("http:\\\\host\\path\\").toString());
    assertEquals("http://host/path/?q=%5C#%5C", Urls.createURI("http:\\\\host\\path\\?q=\\#\\").toString());
    assertEquals("http://test.org/res?signature=a+b=&init=a%20a", Urls.createURI("http://test.org/res?signature=a+b=&init=a a").toString());
    assertEquals("http://host/path;/?q=;%7C", Urls.createURI("http://host/path;/?q=;|").toString());
    assertEquals("https://en.wikipedia.org/wiki/A*", Urls.createURI("https://en.wikipedia.org/wiki/A*").toString());
    assertEquals("https://en.wikipedia.org/wiki/C++", Urls.createURI("https://en.wikipedia.org/wiki/C++").toString());
    assertEquals("https://en.wikipedia.org/wiki/%E2%9D%84", Urls.createURI("https://en.wikipedia.org/wiki/❄").toString());
    assertEquals("http://host/%2e", Urls.createURI("http://host/%2e").toString());
    assertEquals("http://host/%25zz", Urls.createURI("http://host/%zz").toString());
    assertEquals("http://[fa::dd]", Urls.createURI("http://FA::0:dd").toString());
    assertEquals("http://host.com:90", Urls.createURI("http://user:pass@host.com:90").toString());
  }

  @Test public void createURIHost() {
    String expected = "http://xn--qei";
    String input = "http://❤";
    assertEquals(expected, Urls.createURI(input).toString());
    assertEquals("http://host.com:90", Urls.createURI("http://user:pass@host.com:90").toString());
    assertEquals("http://host.com", Urls.createURI("http://HOST.com.").toString());
    assertEquals("http://host.com", Urls.createURI("http://HOST.com:").toString());
    assertEquals("http://host.com/", Urls.createURI("http://HOST.com:/").toString());
    assertEquals("http://192.168.1.1", Urls.createURI("http://192.168.1.1").toString());
    assertEquals("http://192.com", Urls.createURI("http://192.com").toString());
    assertEquals("http://192.com", Urls.createURI("http://192%2ecom").toString());
    assertEquals("http://[fa::dd]", Urls.createURI("http://FA::0:dd").toString());
  }

  @Test public void createURIBackSlashes() {
    assertEquals("http://host/path/?q=%5C#%5C",
        Urls.createURI("http:\\\\host\\path\\?q=\\#\\").toString());
  }

  @Test public void createURIPort() {
    assertEquals("http://host:80", Urls.createURI("http://host:80").toString());
  }

  @Test public void createURIPath() {
    assertEquals("http://host/%2e", Urls.createURI("http://host/%2e").toString());
    assertEquals("http://host/%25zz", Urls.createURI("http://host/%zz").toString());
  }

  @Test public void minimalEscapeRemovesLineBreaks() {
    String expected = "http://host";
    assertEquals(expected, Urls.escape("http://\nhost"));
    assertEquals(expected, Urls.escape("http://\n\rhost"));
    assertEquals(expected, Urls.escape("http://\rhost"));
    assertEquals(expected, Urls.escape("http://\r     host"));
    assertEquals(expected, Urls.escape("http://\n\thost"));
  }

  @Test public void minimalEncodeToLowerCase() {
    assertEquals("http://host", Urls.escape("HTTP://host"));
  }

  @Test public void minimalEncodeHostname() {
    assertEquals("http://xn--qei", Urls.escape("http://❤"));
    assertEquals("http://host.com:9000", Urls.escape("http://user:password@host.com:9000"));
    assertEquals("http://host.com", Urls.escape("http://HOST.com."));
    assertEquals("http://192.168.1.1", Urls.escape("http://192.168.1.1"));
    assertEquals("http://192.com", Urls.escape("http://192.com"));
    assertEquals("http://192.com", Urls.escape("http://192%2ecom"));
    assertEquals("http://[fa::dd]", Urls.escape("http://FA::0:dd"));
  }

  @Test public void minimalEncodeChecksAuthority() {
    try {
      Urls.escape("http://\\\\]/path");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage(), containsString("Invalid hostname:"));
    }
  }

  @Test public void minimalEncodeFixColonSlashSlash() {
    String expected = "http://host";
    assertEquals(expected, Urls.escape("http://////host"));
    assertEquals(expected, Urls.escape("http:/host"));
    assertEquals(expected, Urls.escape("http:\\host"));
    assertEquals(expected, Urls.escape("http:\\\\host"));
  }

  @Test public void minimalEscape_retainPlusInPath() {
    assertEquals("http://wikipedia.org/c++", Urls.escape("http://wikipedia.org/c++"));
  }

  @Test public void minimalEscape_retainPlusInQiuery() {
    assertEquals("http://wikipedia.org/?q=c++", Urls.escape("http://wikipedia.org/?q=c++"));
  }

  @Test public void minimalEncodePath() {
    verifyEscaping("/%00", "/\u0000");
    verifyEscaping("/%01", "/\u0001");
    verifyEscaping("/%02", "/\u0002");
    verifyEscaping("/%03", "/\u0003");
    verifyEscaping("/%04", "/\u0004");
    verifyEscaping("/%05", "/\u0005");
    verifyEscaping("/%06", "/\u0006");
    verifyEscaping("/%07", "/\u0007");
    verifyEscaping("/%08", "/\u0008");
    verifyEscaping("/", "/\u0009");
    verifyEscaping("/", "/\n");
    verifyEscaping("/%0B", "/\u000b");
    verifyEscaping("/", "/\u000c");
    verifyEscaping("/", "/\r");
    verifyEscaping("/%0E", "/\u000e");
    verifyEscaping("/%0F", "/\u000f");
    verifyEscaping("/%10", "/\u0010");
    verifyEscaping("/%11", "/\u0011");
    verifyEscaping("/%12", "/\u0012");
    verifyEscaping("/%13", "/\u0013");
    verifyEscaping("/%14", "/\u0014");
    verifyEscaping("/%15", "/\u0015");
    verifyEscaping("/%16", "/\u0016");
    verifyEscaping("/%17", "/\u0017");
    verifyEscaping("/%18", "/\u0018");
    verifyEscaping("/%19", "/\u0019");
    verifyEscaping("/%1A", "/\u001a");
    verifyEscaping("/%1B", "/\u001b");
    verifyEscaping("/%1C", "/\u001c");
    verifyEscaping("/%1D", "/\u001d");
    verifyEscaping("/%1E", "/\u001e");
    verifyEscaping("/%1F", "/\u001f");
    verifyEscaping("/", "/\u0020");
    verifyEscaping("/!", "/!");
    verifyEscaping("/%22", "/\"");
    verifyEscaping("/#", "/#");
    verifyEscaping("/$", "/$");
    verifyEscaping("/%25", "/%");
    verifyEscaping("/&", "/&");
    verifyEscaping("/'", "/'");
    verifyEscaping("/(", "/(");
    verifyEscaping("/)", "/)");
    verifyEscaping("/*", "/*");
    verifyEscaping("/+", "/+");
    verifyEscaping("/,", "/,");
    verifyEscaping("/-", "/-");
    verifyEscaping("/.", "/.");
    verifyEscaping("/", "/");
    verifyEscaping("/0", "/0");
    verifyEscaping("/1", "/1");
    verifyEscaping("/2", "/2");
    verifyEscaping("/3", "/3");
    verifyEscaping("/4", "/4");
    verifyEscaping("/5", "/5");
    verifyEscaping("/6", "/6");
    verifyEscaping("/7", "/7");
    verifyEscaping("/8", "/8");
    verifyEscaping("/9", "/9");
    verifyEscaping("/:", "/:");
    verifyEscaping("/;", "/;");
    verifyEscaping("/%3C", "/<");
    verifyEscaping("/=", "/=");
    verifyEscaping("/%3E", "/>");
    verifyEscaping("/?", "/?");
    verifyEscaping("/@", "/@");
    verifyEscaping("/A", "/A");
    verifyEscaping("/B", "/B");
    verifyEscaping("/C", "/C");
    verifyEscaping("/D", "/D");
    verifyEscaping("/E", "/E");
    verifyEscaping("/F", "/F");
    verifyEscaping("/G", "/G");
    verifyEscaping("/H", "/H");
    verifyEscaping("/I", "/I");
    verifyEscaping("/J", "/J");
    verifyEscaping("/K", "/K");
    verifyEscaping("/L", "/L");
    verifyEscaping("/M", "/M");
    verifyEscaping("/N", "/N");
    verifyEscaping("/O", "/O");
    verifyEscaping("/P", "/P");
    verifyEscaping("/Q", "/Q");
    verifyEscaping("/R", "/R");
    verifyEscaping("/S", "/S");
    verifyEscaping("/T", "/T");
    verifyEscaping("/U", "/U");
    verifyEscaping("/V", "/V");
    verifyEscaping("/W", "/W");
    verifyEscaping("/X", "/X");
    verifyEscaping("/Y", "/Y");
    verifyEscaping("/Z", "/Z");
    verifyEscaping("/%5B", "/[");
    verifyEscaping("/", "\\");
    verifyEscaping("/%5D", "/]");
    verifyEscaping("/%5E", "/^");
    verifyEscaping("/_", "/_");
    verifyEscaping("/%60", "/`");
    verifyEscaping("/a", "/a");
    verifyEscaping("/b", "/b");
    verifyEscaping("/c", "/c");
    verifyEscaping("/d", "/d");
    verifyEscaping("/e", "/e");
    verifyEscaping("/f", "/f");
    verifyEscaping("/g", "/g");
    verifyEscaping("/h", "/h");
    verifyEscaping("/i", "/i");
    verifyEscaping("/j", "/j");
    verifyEscaping("/k", "/k");
    verifyEscaping("/l", "/l");
    verifyEscaping("/m", "/m");
    verifyEscaping("/n", "/n");
    verifyEscaping("/o", "/o");
    verifyEscaping("/p", "/p");
    verifyEscaping("/q", "/q");
    verifyEscaping("/r", "/r");
    verifyEscaping("/s", "/s");
    verifyEscaping("/t", "/t");
    verifyEscaping("/u", "/u");
    verifyEscaping("/v", "/v");
    verifyEscaping("/w", "/w");
    verifyEscaping("/x", "/x");
    verifyEscaping("/y", "/y");
    verifyEscaping("/z", "/z");
    verifyEscaping("/%7B", "/{");
    verifyEscaping("/%7C", "/|");
    verifyEscaping("/%7D", "/}");
    verifyEscaping("/~", "/~");
    verifyEscaping("/%7F", "/\u007f");
    verifyEscaping("/%C2%80", "/\u0080");
  }

  @Test public void minimalEscapeQuery() {
    verifyEscaping("?%00=%00&%00=%00", "?\u0000=\u0000&\u0000=\u0000");
    verifyEscaping("?%01=%01&%01=%01", "?\u0001=\u0001&\u0001=\u0001");
    verifyEscaping("?%02=%02&%02=%02", "?\u0002=\u0002&\u0002=\u0002");
    verifyEscaping("?%03=%03&%03=%03", "?\u0003=\u0003&\u0003=\u0003");
    verifyEscaping("?%04=%04&%04=%04", "?\u0004=\u0004&\u0004=\u0004");
    verifyEscaping("?%05=%05&%05=%05", "?\u0005=\u0005&\u0005=\u0005");
    verifyEscaping("?%06=%06&%06=%06", "?\u0006=\u0006&\u0006=\u0006");
    verifyEscaping("?%07=%07&%07=%07", "?\u0007=\u0007&\u0007=\u0007");
    verifyEscaping("?%08=%08&%08=%08", "?\u0008=\u0008&\u0008=\u0008");
    verifyEscaping("?%09=%09&%09=", "?\u0009=\u0009&\u0009=\u0009");
    verifyEscaping("?=&=", "?\n=\n&\n=\n");
    verifyEscaping("?%0B=%0B&%0B=%0B", "?\u000b=\u000b&\u000b=\u000b");
    verifyEscaping("?%0C=%0C&%0C=", "?\u000c=\u000c&\u000c=\u000c");
    verifyEscaping("?=&=", "?\r=\r&\r=\r");
    verifyEscaping("?%0E=%0E&%0E=%0E", "?\u000e=\u000e&\u000e=\u000e");
    verifyEscaping("?%0F=%0F&%0F=%0F", "?\u000f=\u000f&\u000f=\u000f");
    verifyEscaping("?%10=%10&%10=%10", "?\u0010=\u0010&\u0010=\u0010");
    verifyEscaping("?%11=%11&%11=%11", "?\u0011=\u0011&\u0011=\u0011");
    verifyEscaping("?%12=%12&%12=%12", "?\u0012=\u0012&\u0012=\u0012");
    verifyEscaping("?%13=%13&%13=%13", "?\u0013=\u0013&\u0013=\u0013");
    verifyEscaping("?%14=%14&%14=%14", "?\u0014=\u0014&\u0014=\u0014");
    verifyEscaping("?%15=%15&%15=%15", "?\u0015=\u0015&\u0015=\u0015");
    verifyEscaping("?%16=%16&%16=%16", "?\u0016=\u0016&\u0016=\u0016");
    verifyEscaping("?%17=%17&%17=%17", "?\u0017=\u0017&\u0017=\u0017");
    verifyEscaping("?%18=%18&%18=%18", "?\u0018=\u0018&\u0018=\u0018");
    verifyEscaping("?%19=%19&%19=%19", "?\u0019=\u0019&\u0019=\u0019");
    verifyEscaping("?%1A=%1A&%1A=%1A", "?\u001a=\u001a&\u001a=\u001a");
    verifyEscaping("?%1B=%1B&%1B=%1B", "?\u001b=\u001b&\u001b=\u001b");
    verifyEscaping("?%1C=%1C&%1C=%1C", "?\u001c=\u001c&\u001c=\u001c");
    verifyEscaping("?%1D=%1D&%1D=%1D", "?\u001d=\u001d&\u001d=\u001d");
    verifyEscaping("?%1E=%1E&%1E=%1E", "?\u001e=\u001e&\u001e=\u001e");
    verifyEscaping("?%1F=%1F&%1F=%1F", "?\u001f=\u001f&\u001f=\u001f");
    verifyEscaping("?%20=%20&%20=", "?\u0020=\u0020&\u0020=\u0020");
    verifyEscaping("?!=!&!=!", "?!=!&!=!");
    verifyEscaping("?%22=%22&%22=%22", "?\"=\"&\"=\"");
    verifyEscaping("?#=%23&%23=%23", "?#=#&#=#");
    verifyEscaping("?$=$&$=$", "?$=$&$=$");
    verifyEscaping("?%25=%25&%25=%25", "?%=%&%=%");
    verifyEscaping("?&=&&&=&", "?&=&&&=&");
    verifyEscaping("?'='&'='", "?'='&'='");
    verifyEscaping("?(=(&(=(", "?(=(&(=(");
    verifyEscaping("?)=)&)=)", "?)=)&)=)");
    verifyEscaping("?*=*&*=*", "?*=*&*=*");
    verifyEscaping("?+=+&+=+", "?+=+&+=+");
    verifyEscaping("?,=,&,=,", "?,=,&,=,");
    verifyEscaping("?-=-&-=-", "?-=-&-=-");
    verifyEscaping("?.=.&.=.", "?.=.&.=.");
    verifyEscaping("?/=/&/=/", "?/=/&/=/");
    verifyEscaping("?0=0&0=0", "?0=0&0=0");
    verifyEscaping("?1=1&1=1", "?1=1&1=1");
    verifyEscaping("?2=2&2=2", "?2=2&2=2");
    verifyEscaping("?3=3&3=3", "?3=3&3=3");
    verifyEscaping("?4=4&4=4", "?4=4&4=4");
    verifyEscaping("?5=5&5=5", "?5=5&5=5");
    verifyEscaping("?6=6&6=6", "?6=6&6=6");
    verifyEscaping("?7=7&7=7", "?7=7&7=7");
    verifyEscaping("?8=8&8=8", "?8=8&8=8");
    verifyEscaping("?9=9&9=9", "?9=9&9=9");
    verifyEscaping("?:=:&:=:", "?:=:&:=:");
    verifyEscaping("?;=;&;=;", "?;=;&;=;");
    verifyEscaping("?%3C=%3C&%3C=%3C", "?<=<&<=<");
    verifyEscaping("?===&===", "?===&===");
    verifyEscaping("?%3E=%3E&%3E=%3E", "?>=>&>=>");
    verifyEscaping("??=?&?=?", "??=?&?=?");
    verifyEscaping("?@=@&@=@", "?@=@&@=@");
    verifyEscaping("?A=A&A=A", "?A=A&A=A");
    verifyEscaping("?B=B&B=B", "?B=B&B=B");
    verifyEscaping("?C=C&C=C", "?C=C&C=C");
    verifyEscaping("?D=D&D=D", "?D=D&D=D");
    verifyEscaping("?E=E&E=E", "?E=E&E=E");
    verifyEscaping("?F=F&F=F", "?F=F&F=F");
    verifyEscaping("?G=G&G=G", "?G=G&G=G");
    verifyEscaping("?H=H&H=H", "?H=H&H=H");
    verifyEscaping("?I=I&I=I", "?I=I&I=I");
    verifyEscaping("?J=J&J=J", "?J=J&J=J");
    verifyEscaping("?K=K&K=K", "?K=K&K=K");
    verifyEscaping("?L=L&L=L", "?L=L&L=L");
    verifyEscaping("?M=M&M=M", "?M=M&M=M");
    verifyEscaping("?N=N&N=N", "?N=N&N=N");
    verifyEscaping("?O=O&O=O", "?O=O&O=O");
    verifyEscaping("?P=P&P=P", "?P=P&P=P");
    verifyEscaping("?Q=Q&Q=Q", "?Q=Q&Q=Q");
    verifyEscaping("?R=R&R=R", "?R=R&R=R");
    verifyEscaping("?S=S&S=S", "?S=S&S=S");
    verifyEscaping("?T=T&T=T", "?T=T&T=T");
    verifyEscaping("?U=U&U=U", "?U=U&U=U");
    verifyEscaping("?V=V&V=V", "?V=V&V=V");
    verifyEscaping("?W=W&W=W", "?W=W&W=W");
    verifyEscaping("?X=X&X=X", "?X=X&X=X");
    verifyEscaping("?Y=Y&Y=Y", "?Y=Y&Y=Y");
    verifyEscaping("?Z=Z&Z=Z", "?Z=Z&Z=Z");
    verifyEscaping("?%5B=%5B&%5B=%5B", "?[=[&[=[");
    verifyEscaping("?%5C=%5C&%5C=%5C", "?\\=\\&\\=\\");
    verifyEscaping("?%5D=%5D&%5D=%5D", "?]=]&]=]");
    verifyEscaping("?%5E=%5E&%5E=%5E", "?^=^&^=^");
    verifyEscaping("?_=_&_=_", "?_=_&_=_");
    verifyEscaping("?%60=%60&%60=%60", "?`=`&`=`");
    verifyEscaping("?a=a&a=a", "?a=a&a=a");
    verifyEscaping("?b=b&b=b", "?b=b&b=b");
    verifyEscaping("?c=c&c=c", "?c=c&c=c");
    verifyEscaping("?d=d&d=d", "?d=d&d=d");
    verifyEscaping("?e=e&e=e", "?e=e&e=e");
    verifyEscaping("?f=f&f=f", "?f=f&f=f");
    verifyEscaping("?g=g&g=g", "?g=g&g=g");
    verifyEscaping("?h=h&h=h", "?h=h&h=h");
    verifyEscaping("?i=i&i=i", "?i=i&i=i");
    verifyEscaping("?j=j&j=j", "?j=j&j=j");
    verifyEscaping("?k=k&k=k", "?k=k&k=k");
    verifyEscaping("?l=l&l=l", "?l=l&l=l");
    verifyEscaping("?m=m&m=m", "?m=m&m=m");
    verifyEscaping("?n=n&n=n", "?n=n&n=n");
    verifyEscaping("?o=o&o=o", "?o=o&o=o");
    verifyEscaping("?p=p&p=p", "?p=p&p=p");
    verifyEscaping("?q=q&q=q", "?q=q&q=q");
    verifyEscaping("?r=r&r=r", "?r=r&r=r");
    verifyEscaping("?s=s&s=s", "?s=s&s=s");
    verifyEscaping("?t=t&t=t", "?t=t&t=t");
    verifyEscaping("?u=u&u=u", "?u=u&u=u");
    verifyEscaping("?v=v&v=v", "?v=v&v=v");
    verifyEscaping("?w=w&w=w", "?w=w&w=w");
    verifyEscaping("?x=x&x=x", "?x=x&x=x");
    verifyEscaping("?y=y&y=y", "?y=y&y=y");
    verifyEscaping("?z=z&z=z", "?z=z&z=z");
    verifyEscaping("?%7B=%7B&%7B=%7B", "?{={&{={");
    verifyEscaping("?%7C=%7C&%7C=%7C", "?|=|&|=|");
    verifyEscaping("?%7D=%7D&%7D=%7D", "?}=}&}=}");
    verifyEscaping("?~=~&~=~", "?~=~&~=~");
    verifyEscaping("?%7F=%7F&%7F=%7F", "?\u007f=\u007f&\u007f=\u007f");
    verifyEscaping("?%C2%80=%C2%80&%C2%80=%C2%80", "?\u0080=\u0080&\u0080=\u0080");

    verifyEscaping("?%2e", "?%2e");
    verifyEscaping("?%25zz", "?%zz");
    verifyEscaping("?+==", "?+==");
  }

  @Test public void minimalEscapeFragment() {
    verifyEscaping("#%00", "#\u0000");
    verifyEscaping("#%01", "#\u0001");
    verifyEscaping("#%02", "#\u0002");
    verifyEscaping("#%03", "#\u0003");
    verifyEscaping("#%04", "#\u0004");
    verifyEscaping("#%05", "#\u0005");
    verifyEscaping("#%06", "#\u0006");
    verifyEscaping("#%07", "#\u0007");
    verifyEscaping("#%08", "#\u0008");
    verifyEscaping("#", "#\u0009");
    verifyEscaping("#", "#\n");
    verifyEscaping("#%0B", "#\u000b");
    verifyEscaping("#", "#\u000c");
    verifyEscaping("#", "#\r");
    verifyEscaping("#%0E", "#\u000e");
    verifyEscaping("#%0F", "#\u000f");
    verifyEscaping("#%10", "#\u0010");
    verifyEscaping("#%11", "#\u0011");
    verifyEscaping("#%12", "#\u0012");
    verifyEscaping("#%13", "#\u0013");
    verifyEscaping("#%14", "#\u0014");
    verifyEscaping("#%15", "#\u0015");
    verifyEscaping("#%16", "#\u0016");
    verifyEscaping("#%17", "#\u0017");
    verifyEscaping("#%18", "#\u0018");
    verifyEscaping("#%19", "#\u0019");
    verifyEscaping("#%1A", "#\u001a");
    verifyEscaping("#%1B", "#\u001b");
    verifyEscaping("#%1C", "#\u001c");
    verifyEscaping("#%1D", "#\u001d");
    verifyEscaping("#%1E", "#\u001e");
    verifyEscaping("#%1F", "#\u001f");
    verifyEscaping("#", "#\u0020");
    verifyEscaping("#!", "#!");
    verifyEscaping("#%22", "#\"");
    verifyEscaping("#%23", "##");
    verifyEscaping("#$", "#$");
    verifyEscaping("#%25", "#%");
    verifyEscaping("#&", "#&");
    verifyEscaping("#'", "#'");
    verifyEscaping("#(", "#(");
    verifyEscaping("#)", "#)");
    verifyEscaping("#*", "#*");
    verifyEscaping("#+", "#+");
    verifyEscaping("#,", "#,");
    verifyEscaping("#-", "#-");
    verifyEscaping("#.", "#.");
    verifyEscaping("#/", "#/");
    verifyEscaping("#0", "#0");
    verifyEscaping("#1", "#1");
    verifyEscaping("#2", "#2");
    verifyEscaping("#3", "#3");
    verifyEscaping("#4", "#4");
    verifyEscaping("#5", "#5");
    verifyEscaping("#6", "#6");
    verifyEscaping("#7", "#7");
    verifyEscaping("#8", "#8");
    verifyEscaping("#9", "#9");
    verifyEscaping("#:", "#:");
    verifyEscaping("#;", "#;");
    verifyEscaping("#%3C", "#<");
    verifyEscaping("#=", "#=");
    verifyEscaping("#%3E", "#>");
    verifyEscaping("#?", "#?");
    verifyEscaping("#@", "#@");
    verifyEscaping("#A", "#A");
    verifyEscaping("#B", "#B");
    verifyEscaping("#C", "#C");
    verifyEscaping("#D", "#D");
    verifyEscaping("#E", "#E");
    verifyEscaping("#F", "#F");
    verifyEscaping("#G", "#G");
    verifyEscaping("#H", "#H");
    verifyEscaping("#I", "#I");
    verifyEscaping("#J", "#J");
    verifyEscaping("#K", "#K");
    verifyEscaping("#L", "#L");
    verifyEscaping("#M", "#M");
    verifyEscaping("#N", "#N");
    verifyEscaping("#O", "#O");
    verifyEscaping("#P", "#P");
    verifyEscaping("#Q", "#Q");
    verifyEscaping("#R", "#R");
    verifyEscaping("#S", "#S");
    verifyEscaping("#T", "#T");
    verifyEscaping("#U", "#U");
    verifyEscaping("#V", "#V");
    verifyEscaping("#W", "#W");
    verifyEscaping("#X", "#X");
    verifyEscaping("#Y", "#Y");
    verifyEscaping("#Z", "#Z");
    verifyEscaping("#%5B", "#[");
    verifyEscaping("#%5C", "#\\");
    verifyEscaping("#%5D", "#]");
    verifyEscaping("#%5E", "#^");
    verifyEscaping("#_", "#_");
    verifyEscaping("#%60", "#`");
    verifyEscaping("#a", "#a");
    verifyEscaping("#b", "#b");
    verifyEscaping("#c", "#c");
    verifyEscaping("#d", "#d");
    verifyEscaping("#e", "#e");
    verifyEscaping("#f", "#f");
    verifyEscaping("#g", "#g");
    verifyEscaping("#h", "#h");
    verifyEscaping("#i", "#i");
    verifyEscaping("#j", "#j");
    verifyEscaping("#k", "#k");
    verifyEscaping("#l", "#l");
    verifyEscaping("#m", "#m");
    verifyEscaping("#n", "#n");
    verifyEscaping("#o", "#o");
    verifyEscaping("#p", "#p");
    verifyEscaping("#q", "#q");
    verifyEscaping("#r", "#r");
    verifyEscaping("#s", "#s");
    verifyEscaping("#t", "#t");
    verifyEscaping("#u", "#u");
    verifyEscaping("#v", "#v");
    verifyEscaping("#w", "#w");
    verifyEscaping("#x", "#x");
    verifyEscaping("#y", "#y");
    verifyEscaping("#z", "#z");
    verifyEscaping("#%7B", "#{");
    verifyEscaping("#%7C", "#|");
    verifyEscaping("#%7D", "#}");
    verifyEscaping("#~", "#~");
    verifyEscaping("#%7F", "#\u007f");
    verifyEscaping("#%C2%80", "#\u0080");
    verifyEscaping("#%C2%81", "#\u0081");
    verifyEscaping("#%C2%82", "#\u0082");
    verifyEscaping("#%C2%83", "#\u0083");
    verifyEscaping("#%C2%84", "#\u0084");

    verifyEscaping("#%2e", "#%2e");
    verifyEscaping("#%25zz", "#%zz");
  }

  private void verifyEscaping(String expected, String input) {
    expected = "http://host" + expected;
    input = "http://host" + input;
    try {
      new URI(expected);
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
    assertEquals(expected, Urls.escape(input));
  }
}