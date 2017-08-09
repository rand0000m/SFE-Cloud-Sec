#!/bin/bash

cd /build

# Compilation du plugin GroupBasedPolicy avec patch
useradd -m builduser
chmod a+rw /build
su builduser << EOSU
git config --global user.email "you@example.com"
git config --global user.name "Your Name"
cd /build
mkdir ~/.m2
wget "https://raw.githubusercontent.com/opendaylight/odlparent/master/settings.xml" -O ~/.m2/settings.xml
git clone https://github.com/opendaylight/groupbasedpolicy.git
cd groupbasedpolicy
git checkout release/carbon
git am /tmp/export-endpoint.patch
mvn install -DskipTests
cp -r ~/.m2/repository/org/opendaylight/groupbasedpolicy/groupbasedpolicy/0.5.0-Carbon/*.jar /build/
EOSU

