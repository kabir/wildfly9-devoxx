#!/bin/bash

if [ "x$JBOSS_HOME" == "x" ]; then
	echo "JBOSS_HOME is not set, please set it to the root of your Wildfly installation"
	exit
fi


#IMPORTANT: The ALPN version changes depending on the version of the JVM you are using
#If you see class not found or similar SSL errors please look up the correct version
# at http://eclipse.org/jetty/documentation/current/alpn-chapter.html
ALPN_VERSION=8.1.3.v20150130

### When trying this yourself, remove these lines and remove 
#Copy keystore and truststore
cp http2/server.* $JBOSS_HOME/standalone/configuration/
#Copy the ALPN jar 
cp http2/alpn-boot-$ALPN_VERSION.jar $JBOSS_HOME/bin/alpn-boot-$ALPN_VERSION.jar

cd $JBOSS_HOME

### When trying this yourself, remove the lines above
#download our fake certificate for testing
#DO NOT USE THIS IN PRODUCTION
#Get a real cert instead
#curl https://raw.githubusercontent.com/undertow-io/undertow/master/core/src/test/resources/server.keystore >standalone/configuration/server.keystore
#curl https://raw.githubusercontent.com/undertow-io/undertow/master/core/src/test/resources/server.truststore >standalone/configuration/server.truststore
#Download the ALPN jar we are interested in
#curl http://central.maven.org/maven2/org/mortbay/jetty/alpn/alpn-boot/$ALPN_VERSION/alpn-boot-$ALPN_VERSION.jar >bin/alpn-boot-$ALPN_VERSION.jar


#Add ALPN to the boot class path
echo 'JAVA_OPTS="$JAVA_OPTS' " -Xbootclasspath/p:$JBOSS_HOME/bin/alpn-boot-$ALPN_VERSION.jar" '"' >>bin/standalone.conf

#Start Wildfly in the background
./bin/standalone.sh &
#wait for Wildfly to start
sleep 8

#Create a https security realm containing the SSL keystore
./bin/jboss-cli.sh -c "--command=/core-service=management/security-realm=https:add()"
./bin/jboss-cli.sh -c "--command=/core-service=management/security-realm=https/authentication=truststore:add(keystore-path=server.truststore, keystore-password=password, keystore-relative-to=jboss.server.config.dir)"
./bin/jboss-cli.sh -c "--command=/core-service=management/security-realm=https/server-identity=ssl:add(keystore-path=server.keystore, keystore-password=password, keystore-relative-to=jboss.server.config.dir)"

#Reference the security realm from the https connector
./bin/jboss-cli.sh -c "--command=/subsystem=undertow/server=default-server/https-listener=https:add(socket-binding=https, security-realm=https, enable-http2=true)"

#shut down Wildfly
kill `jps | grep jboss-modules.jar | cut -f1 -d ' ' `