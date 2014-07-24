WPS processes provided by 

## usage

**compilation**

1. `mvn clean install`


**deployment** //see also [aws/build.xml](aws/build.xml)

2. deploy regular 52n wps server 3.3.0-stable (or richwps server)
3. copy `mpa/target/lkn-mpa-0.0.1-SNAPSHOT-full.jar` to  `/var/lib/tomcat7/webapps/wps/WEB-INF/lib`
4. copy `aws/wps_config.xml` to  `/var/lib/tomcat7/webapps/wps/config/`
5. `chown tomcat7:tomcat7 /var/lib/tomcat7/webapps/wps`
6. `service tomcat7 restart`


**heads up:** if 52n wps server 3.3.0-stable is used, the wps_config needs to be altered.

## testing

[@see mpa/testing README.md](aws/testing/README.md)

## contents

* `lkn-mpa::0.0.1-SNAPSHOT` //-full