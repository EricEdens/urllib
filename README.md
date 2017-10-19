# Urllib

Urllib is a library that makes URL manipulation easy, fun, and safe!

- [x] Zero extra dependencies.
- [x] Supports Java 8+.
- [x] Compliant with [RFC 3986](https://tools.ietf.org/html/rfc3986).
- [x] Immutable and threadsafe.


## We're in preview!

Feel free to check out the code and give feedback! We're targeting a general release
once we reach 1.0. 


- [x] 0.1
  - Create a `Url` from scratch with builders.
  - Interop with `java.net.URI`
  - Support ASCII DNS hosts.
- [ ] 0.2
  - Expose component fields (scheme, host, path, etc..) via methods on the `Url` object.
- [ ] 0.3
  - Support IPv4 hosts.
- [ ] 0.4
  - Support IPv6 hosts.
- [ ] 0.5
  - Support IDN hosts.
- [ ] 0.6
  - Utility method to classify a potential URL. Is it junk? A protocol-relative URL? An absolute path?
- [ ] 0.7
  - Resolve a possibly-relative link against an existing `Url` 
- [ ] 0.8
  - Utility method to canonicalize previously-encoded URLs.
- [ ] 0.9
  - Create a `Url` by parsing.
- [ ] 1.0
  - Encode a `Url` to display to users (like in a web browser URL bar)
  
## License
[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)
