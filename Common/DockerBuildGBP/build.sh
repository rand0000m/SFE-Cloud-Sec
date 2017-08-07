#!/bin/bash

cd /build
#mkdir ~/.ssh
#echo -e "Host git.rousse.me\n\tIdentityFile /tmp/deploy_key" > ~/.ssh/config
#chmod 0600 /tmp/deploy_key
#ssh-keyscan git.rousse.me >> ~/.ssh/known_hosts
#
## Compilation du plugin Cloud-Sec
#git clone ssh://git@git.rousse.me/mrousse/SFE-Cloud-Sec.git
#cd SFE-Cloud-Sec/ODL-Controller/cloud-sec
#mkdir ~/.m2
#wget -q -O - https://raw.githubusercontent.com/opendaylight/odlparent/master/settings.xml > ~/.m2/settings.xml
#wget "https://nexus.opendaylight.org/content/repositories/public/org/opendaylight/groupbasedpolicy/l2-l3-domain-extension/0.5.0-Carbon/l2-l3-domain-extension-0.5.0-Carbon-javadoc.jar"
#mvn install:install-file -DgroupId=org.opendaylight.groupbasedpolicy -DartifactId=l2-l3-domain-extension -Dversion=0.5.0-Carbon -Dpackaging=bundle -Dfile=l2-l3-domain-extension-0.5.0-Carbon-javadoc.jar
#mvn clean install -DskipTests
#cp -r ~/.m2/repository/com/orange/cloudsec /build/cloudsec

# Compilation du plugin GroupBasedPolicy avec patch
useradd -m builduser
chmod a+rw /build
su builduser << EOSU
cd /build
mkdir ~/.m2
wget "https://raw.githubusercontent.com/opendaylight/odlparent/master/settings.xml" -O ~/.m2/settings.xml
git clone https://github.com/opendaylight/groupbasedpolicy.git
cd groupbasedpolicy
echo "From 0d08c150cac12d2ac997f026a1da36765b21cb1f Mon Sep 17 00:00:00 2001
From: Mathieu Rousse <mathieu@rousse.me>
Date: Thu, 29 Jun 2017 11:03:49 +0200
Subject: [PATCH] Add endpoint export

---
 groupbasedpolicy/pom.xml | 1 +
 1 file changed, 1 insertion(+)

diff --git a/groupbasedpolicy/pom.xml b/groupbasedpolicy/pom.xml
index fa5f7c7..0e21766 100755
--- a/groupbasedpolicy/pom.xml
+++ b/groupbasedpolicy/pom.xml
@@ -94,6 +94,7 @@
               org.opendaylight.groupbasedpolicy.api.*,
               org.opendaylight.groupbasedpolicy.dto,
               org.opendaylight.groupbasedpolicy.util,
+	      org.opendaylight.groupbasedpolicy.endpoint,
             </Export-Package>
             <Embed-Dependency>java-ipv6</Embed-Dependency>
           </instructions>
-- 
2.1.4
" > export_endpoint.patch
git checkout release/carbon
git am < export_endpoint.patch
mvn install -DskipTests
cp -r ~/.m2/repository/org/opendaylight/groupbasedpolicy/groupbasedpolicy/0.5.0-Carbon/*.jar /build/
EOSU

