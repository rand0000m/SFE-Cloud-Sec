#!/bin/bash

if [ -z "$1" ]
then
	echo "Usage : $0 <switch>"
	exit 1
fi

SWITCH=$1

sudo ovs-vsctl add-br $SWITCH
sudo ovs-vsctl add-port $SWITCH $SWITCH-vxlangpe-0 -- set interface $SWITCH-vxlangpe-0 type=vxlan options:exts=gpe options:remote_ip=flow options:dst_port=6633 options:nshc1=flow options:nshc2=flow options:nshc3=flow options:nshc4=flow options:nsp=flow options:nsi=flow options:key=flow
