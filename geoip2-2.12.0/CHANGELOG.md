CHANGELOG
=========

2.12.0 (2018-04-11)
-------------------

* Rename `userId` to `accountId` in various places and support the future error
  codes `ACCOUNT_ID_REQUIRED` and `ACCOUNT_ID_UNKNOWN`.

2.11.0 (2018-01-19)
------------------

* The web service client now correctly handles a proxy of `Proxy.NO_PROXY`.
* The `isInEuropeanUnion()` method was added to
  `com.maxmind.geoip2.record.Country`. This returns `true` if the country
  is a member state of the European Union.

2.10.0 (2017-10-27)
-------------------

* The following new anonymizer methods were added to
 `com.maxmind.geoip2.record.Traits` for use with GeoIP2 Precision Insights:
 `isAnonymous()`, `isAnonymousVpn()`, `isHostingProvider()`, `isPublicProxy()`,
  and `isTorExitNode()`.

2.9.0 (2017-05-08)
------------------

* Added support for GeoLite2 ASN database.

2.8.1 (2017-02-22)
------------------

* Update `maxmind-db` dependency to fix `jackson-databind` version range
  issue. Closes GitHub #77.
* Update most other dependencies.

2.8.0 (2016-09-15)
------------------

* All changes included in 2.8.0-rc1.
* Updated documentation to clarify what the accuracy radius refers to.

2.8.0-rc1 (2016-06-20)
----------------------

* IMPORTANT: Java 7 is now required. If you need Java 6 support, please
  continue using 2.7.0 or earlier.
* This library no longer uses Google HTTP Client. It now directly uses
  Apache HttpClient. Closes #40, #66.
* `WebServiceClient` now implements `Closeable`. A pool of connections will be
  kept alive to be used across requests. To ensure all connections are closed
  when the object goes out of scope, call `close()` or use the
  try-with-resource statement as appropriate.
* Setting of a proxy for the `WebServiceClient` is now supported by the
  `proxy(Proxy)` builder method.
* Updated documentation to reflect that the accuracy radius is now included
  in City.
* Updated dependencies.

2.7.0 (2016-04-15)
------------------

* Added support for the GeoIP2 Enterprise database.

2.6.0 (2016-01-13)
------------------

* This release was updated to 1.2.0 of the MaxMind DB reader, which includes
  faster caching with fewer allocations.
* The IP addresses in the database models are now injected via Jackson rather
  than being added to the `JsonNode` before deserialization. Pull requests by
  Viktor Szathmáry. GitHub #56.

2.5.0 (2016-01-04)
------------------

* The database reader now supports pluggable caching of the decoded data. By
  default, no caching is performed. Please see the `README.md` file or the API
  docs for information on how to enable caching. Pull requests by Viktor
  Szathmáry. GitHub #55.

2.4.0 (2015-12-21)
------------------

* Jackson now uses the constructors on model classes when mapping JSON and
  database records to them rather than overriding the access modifiers on
  them. Pull request by Martijn van Groningen. GitHub #51 & #52.
* The format of the output of the `toString()` methods in the models has
  changed to better represent the values returned by the databases and web
  services. `toString()` should be only used for debugging and diagnostics.
  Do not try to parse it. If you want the contents of the model as a machine-
  readable string, use `toJson()`.
* This release depends on version 1.0.1 of the MaxMind DB reader, which
  includes several performance enhancements from by Viktor Szathmáry.

2.3.1 (2015-07-07)
------------------

* No code changes in this release
* Fix for version number in pom.xml example in README.md
* Slight documentation improvement referring to MaxMind-DB-Reader-java

2.3.0 (2015-06-29)
------------------

* Add support for the `average_income` and `population_density` fields.
* The `isAnonymousProxy()` and `isSatelliteProvider()` methods on
  `com.maxmind.geoip2.record.Traits` have been deprecated. Please use our
  [GeoIP2 Anonymous IP database](https://www.maxmind.com/en/geoip2-anonymous-
  ip-database) to determine whether an IP address is used by an anonymizing
  service.

2.2.0 (2015-04-24)
------------------

* A `DatabaseProvider` interface has been added to facilitate mocking of
  `DatabaseReader`. Pull request by Yonatan Most. GitHub #34.
* A `getLeastSpecificSubdivision()` method has been added to the
  `CityResponse` and `InsightsResponse` model classes. This returns the
  least specific subdivision for the location, e.g., England for Oxford,
  GB. Pull request by Daniel Kaneider. GitHub #35.
* The `InsightsResponse` and `Location` classes are no longer declared final.
* `AbstractResponse` is now declared `abstract`.

2.1.0 (2014-11-06)
------------------

* Added support for the GeoIP2 Anonymous IP database. The `DatabaseReader`
  class now has an `anonymousIp()` method which returns an
  `AnonymousIpResponse` object.

2.0.0 (2014-09-29)
------------------

* First production release.

0.10.0 (2014-09-23)
-------------------

* The deprecated `cityIspOrg()` and `omni()` methods have been removed from
  `DatabaseReader` and `WebServiceClient`.
* The lookup methods on `DatabaseReader` now throw an
  `UnsupportedOperationException` if the incorrect method is used for the
  database.
* `DatabaseReader` now provides the metadata for the database through the
  `getDatabase()` method.
* All of our dependencies were updated to the latest available version.

0.9.0 (2014-09-02)
------------------

* The `timeout` setter on `WebServiceClient.Builder` was renamed to
  `connectTimeout` and a `readTimeout` setter was added. The former timeout
  sets the timeout to establish a connection and the latter sets the timeout
  for reading from an established connection.

0.8.1 (2014-08-27)
------------------

* Updated to depend on the latest version of `com.maxmind.db` and
  `com.fasterxml.jackson.core`.

0.8.0 (2014-07-22)
------------------

* The web service client API has been updated for the v2.1 release of the web
  service. In particular, the `cityIspOrg` and `omni` methods on
  `WebServiceClient` have been deprecated. The `city` method now provides all
  of the data formerly provided by `cityIspOrg`, and the `omni` method has
  been replaced by the `insights` method.
* Support was added for the GeoIP2 Connection Type, Domain, and ISP databases.

0.7.2 (2014-06-02)
------------------

* Updated to version 0.3.3 of `maxmind-db`, which fixes a potential resource
  leak when used with a thread pool.
* Updated Google HTTP Client dependency.
* The Maven build was updated to include a zip file with all dependencies.

0.7.1 (2014-04-02)
------------------

* Added `toJson` method to response objects.
* Fixed a potential issue when using the `WebServiceClient` in multi-threaded
  applications.
* Updated documentation.

0.7.0 (2013-11-05)
------------------

* Renamed `getSubdivisionsList` to `getSubdivisions` on `AbstractNamedRecord`.
* An `InputStream` constructor was added to the `DatabaseReader.Builder`
  class. This reads the stream into memory as if it was using the
  `FileMode.MEMORY` mode. Patch by Matthew Daniel.
* The source code is now attached during packaging. Patch by Matthew Daniel.

0.6.0 (2013-10-23)
------------------

* IMPORTANT API CHANGE: The `DatabaseReader` class now uses a builder to
  construct the object. The class constructor on `DatabaseReader` is no longer
  public.
* Renamed the `languages` method on the `WebServiceClient.Builder` to
  `locales`.

0.5.0 (2013-10-17)
------------------

* Reorganized the response and record classes. The response classes end
  with `Response`. The record classes no longer end in `Record`.

0.4.1 (2013-08-16)
------------------

* Set the user-agent header to include API information.
* Updated documentation.
* Removed unused dependency from Maven POM.

0.4.0 (2013-07-08)
------------------

* Removed class hierarchy among web-service endpoint models.
* Refactored database-reader API to more closely match the web-service API.
  Created a Java interface for the two classes.

0.3.0 (2013-06-27)
------------------

* Reorganized the classes. `Client` was renamed `WebServiceClient` and moved
  to `com.maxmind.geoip2`. Record classes now have a suffix of "Record".
  The product classes (e.g., Omni) were renamed to their product name with
  no "Lookup" suffix.
* Additional specific exceptions were added to replace the general
  `WebServiceException`.
* A `DatabaseReader` class was added to the distribution. This reads GeoIP2
  databases and returns similar product object to `WebServiceClient`.

0.2.0 (2013-06-13)
------------------

* Replaced the public constructor on `Client` with a `Builder` class.

0.1.1 (2013-06-10)
------------------

* First official beta release.
* Documentation updates and corrections.
* Changed license to Apache License, Version 2.0.

0.1.0 (2013-05-21)
------------------

* Initial release
