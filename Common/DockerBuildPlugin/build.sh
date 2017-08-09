#!/bin/bash

cd /build
mkdir ~/.ssh
echo -e "Host git.rousse.me\n\tIdentityFile /tmp/deploy_key" > ~/.ssh/config
chmod 0600 /tmp/deploy_key
ssh-keyscan git.rousse.me >> ~/.ssh/known_hosts

# Compilation du plugin Cloud-Sec
git clone ssh://git@git.rousse.me/mrousse/SFE-Cloud-Sec.git
cd SFE-Cloud-Sec/ODL-Controller/cloud-sec
mkdir ~/.m2
wget -q -O - https://raw.githubusercontent.com/opendaylight/odlparent/master/settings.xml > ~/.m2/settings.xml
wget "https://nexus.opendaylight.org/content/repositories/public/org/opendaylight/groupbasedpolicy/l2-l3-domain-extension/0.5.0-Carbon/l2-l3-domain-extension-0.5.0-Carbon-javadoc.jar"
mvn install:install-file -DgroupId=org.opendaylight.groupbasedpolicy -DartifactId=l2-l3-domain-extension -Dversion=0.5.0-Carbon -Dpackaging=bundle -Dfile=l2-l3-domain-extension-0.5.0-Carbon-javadoc.jar
mvn clean install -DskipTests
cp -r ~/.m2/repository/com/orange/cloudsec /build/cloudsec
