#!/bin/bash

if [ -z "$1" ]
then
	echo "Usage : $0 <controller-ip> <bridge-name>"
	exit 1
fi

ovs-vsctl set-manager tcp:$1:6640
ovs-vsctl add-br $2
ovs-vsctl set-controller $2 tcp:$1:6653
