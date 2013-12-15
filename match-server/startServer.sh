cd `dirname $0`
sleep 120 && rm -rf target/cargo/configurations/tomcat6x/webapps/*manager &
sleep 120 && rm -rf target/cargo/configurations/tomcat6x/webapps/cargocpc* &
mvn clean install cargo:start -Dcargo.maven.wait=true