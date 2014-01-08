cd `dirname $0`
sleep 120 && rm -rf target/cargo/configurations/tomcat7x/webapps/*manager &
sleep 120 && rm -rf target/cargo/configurations/tomcat7x/webapps/cargocpc* &
mvn clean install cargo:start -Dcargo.maven.wait=true
