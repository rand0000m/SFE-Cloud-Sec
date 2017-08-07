#!/bin/bash

if [ -z $1 ]
then
	mv /vagrant/builds/distribution-karaf-0.6.0-Carbon.tar.gz .
	tar -xf distribution-karaf-0.6.0-Carbon.tar.gz
else
	wget "https://nexus.opendaylight.org/content/repositories/opendaylight.release/org/opendaylight/integration/distribution-karaf/0.6.0-Carbon/distribution-karaf-0.6.0-Carbon.zip"
	unzip distribution-karaf-0.6.0-Carbon.zip
fi
mkdir -p /root/distribution-karaf-0.6.0-Carbon/system/com/orange

echo "deb http://ftp.fr.debian.org/debian jessie-backports main" >> /etc/apt/sources.list

echo 'JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/"' > /etc/environment
echo '_JAVA_OPTIONS="-Djava.net.preferIPv4Stack=true"' >> /etc/environment
echo 'MAVEN_OPTS="-Xmx1024m"' >> /etc/environment

apt update
apt install -y -t jessie-backports maven git vim openjdk-8-jdk openjdk-8-jre screen curl

if [ -z $1 ]
then
	mv /vagrant/builds/groupbased* /root/distribution-karaf-0.6.0-Carbon/system/org/opendaylight/groupbasedpolicy/groupbasedpolicy/0.5.0-Carbon/
	mv /vagrant/builds/cloudsec /root/distribution-karaf-0.6.0-Carbon/system/com/orange
else
	git clone https://git.rousse.me/mrousse/SFE-Cloud-Sec.git
	cd SFE-Cloud-Sec/ODL-Controller/cloud-sec
	mkdir ~/.m2
	wget -q -O - https://raw.githubusercontent.com/opendaylight/odlparent/master/settings.xml > ~/.m2/settings.xml
	wget "https://nexus.opendaylight.org/content/repositories/public/org/opendaylight/groupbasedpolicy/l2-l3-domain-extension/0.5.0-Carbon/l2-l3-domain-extension-0.5.0-Carbon-javadoc.jar"
	mvn install:install-file -DgroupId=org.opendaylight.groupbasedpolicy -DartifactId=l2-l3-domain-extension -Dversion=0.5.0-Carbon -Dpackaging=bundle -Dfile=l2-l3-domain-extension-0.5.0-Carbon-javadoc.jar
	mvn clean install -DskipTests
	cp -r ~/.m2/repository/com/orange/cloudsec /root/distribution-karaf-0.6.0-Carbon/system/com/orange
fi
