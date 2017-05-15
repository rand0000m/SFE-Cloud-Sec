#!/bin/bash

# Useful snippet to shut it down
# kill -9 $(screen -ls | grep odl | cut -d"." -f1 | xargs); screen -wipe

karaf_root="/root/distribution-karaf-0.5.2-Boron-SR2"
mv -b $karaf_root/data/log/karaf.log $karaf_root/data/log/karaf.log.last
screen -dmS odl-controller $karaf_root/bin/karaf

while [ ! -f $karaf_root/data/log/karaf.log ] || ! grep -q "Karaf started in" $karaf_root/data/log/karaf.log; do
	sleep 1
done

# IT IS NOW STARTED


