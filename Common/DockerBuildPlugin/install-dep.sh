#!/bin/bash

echo "deb http://ftp.fr.debian.org/debian jessie-backports main" >> /etc/apt/sources.list

echo 'JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/"' > /etc/environment
echo '_JAVA_OPTIONS="-Djava.net.preferIPv4Stack=true"' >> /etc/environment
echo 'MAVEN_OPTS="-Xmx1024m"' >> /etc/environment

apt-get update
apt install -y -t jessie-backports maven git vim openjdk-8-jdk openjdk-8-jre screen curl wget
