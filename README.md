# urllib
Urllib is a library that makes URL manipulation easy, fun, and safe!

- [x] Zero extra dependencies.
- [x] Supports Java 7+, Android 14+.
- [x] Compliant with [RFC 3986](https://tools.ietf.org/html/rfc3986).
- [x] Immutable and threadsafe.

```java
  System.out.println(
      Url.http("maps.google.com")
         .path("maps")
         .query("q", "Búðardalur")
         .create());

  >> http://maps.google.com/maps?q=B%C3%BA%C3%B0ardalur

  System.out.println(
      Url.parse("https://www.wolframalpha.com/input/?i=%E2%88%9A-1")
         .query()
         .params());

  >> {i=√-1}
```

## We're in preview!

Feel free to check out the code and give feedback! We're targeting a general release
once we reach 1.0. 


- [x] 0.1
  - Create a `Url` from scratch with builders.
  - Interop with `java.net.URI`
  - Support ASCII DNS hosts.
- [x] 0.2
  - Expose component fields (scheme, host, path, etc..) via methods on the `Url` object.
- [x] 0.3
  - Support IPv4 hosts.
- [x] 0.4
  - Support IPv6 hosts.
- [x] 0.5
  - Support IDN hosts.
- [x] 0.6
  - Create a `Url` by parsing.
- [x] 0.7
  - Utility method to create a `java.net.URI` from a previously-encoded `URL`.
- [ ] 0.8
  - Resolve a possibly-relative link against an existing `Url` 
- [ ] 0.9
  - Utility method to classify a potential URL. Is it junk? A protocol-relative URL? An absolute path?
- [ ] 1.0
  - Encode a `Url` to display to users (like in a web browser URL bar)
  
## License
[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)
