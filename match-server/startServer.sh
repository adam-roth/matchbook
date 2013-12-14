cd `dirname $0`
#mvn cargo:stop
mvn clean install cargo:start -Dcargo.maven.wait=true

