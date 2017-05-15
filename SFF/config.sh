#!/bin/bash

if [ -z "$1" ]
then
	echo "Usage : $0 <controller-ip>"
	exit 1
fi

ovs-vsctl set-manager tcp:$1:6640
