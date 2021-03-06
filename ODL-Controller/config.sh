#!/bin/bash

# Useful snippet to shut it down
# kill -9 $(screen -ls | grep odl | cut -d"." -f1 | xargs); screen -wipe

source /etc/environment
karaf_root="/root/distribution-karaf-0.6.0-Carbon"
mv -b $karaf_root/data/log/karaf.log $karaf_root/data/log/karaf.log.last
screen -dmS odl-controller $karaf_root/bin/karaf

while [ ! -f $karaf_root/data/log/karaf.log ] || ! grep -q "Karaf started in" $karaf_root/data/log/karaf.log; do
	sleep 1
done

# IT IS NOW STARTED

$karaf_root/bin/client "feature:install odl-groupbasedpolicy-ofoverlay odl-groupbasedpolicy-ui odl-restconf"
$karaf_root/bin/client "repo-add mvn:com.orange.cloudsec/cloud-sec-features/0.1.0-SNAPSHOT/xml/features"
$karaf_root/bin/client "feature:install odl-cloud-sec odl-cloud-sec-api"
