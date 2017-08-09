#!/bin/bash

bridge=$(hostname)
datapath_id=""
controller="127.0.0.1"
bridge_padding=$(printf '%0.1s' " "{1..16})
bridge_padded_len=16
bridge_padded=$(printf "%s%s" $bridge "${bridge_padding:${#bridge}}")

usage(){
	printf "Configuration script for SFC :\n"
	printf "\t--controller=127.0.0.1   : IP address of the ODL controller\n"
	printf "\t--bridge=$bridge_padded: bridge name\n"
	printf "\t--datapath-id            : ID of the datapath of the switch (optionnal)\n"
	printf "\t-h                       : prints this message\n"
}

if [ $# -eq 0 ]
then
	usage
fi

addBridge(){
	ovs-vsctl add-br $bridge
}

setDPID(){
	dpid=$(printf %016d $datapath_id);
	ovs-vsctl set bridge $bridge other-config:datapath-id=$dpid
	echo $dpid
}

setOfVersion(){
	ovs-vsctl set bridge $bridge protocols=OpenFlow13,OpenFlow12,OpenFlow10
}

setController(){
	ovs-vsctl set-controller $bridge tcp:$controller:6653
}

setManager(){
	ovs-vsctl set-manager tcp:$controller:6640
}

addGPETunnel(){
	ovs-vsctl add-port $bridge $bridge-vxlangpe-0 -- set interface $bridge-vxlangpe-0 \
		type=vxlan \
		options:exts=gpe \
		options:remote_ip=flow \
		options:dst_port=4790 \
		options:nshc1=flow \
		options:nshc2=flow \
		options:nshc3=flow \
		options:nshc4=flow \
		options:nsp=flow \
		options:nsi=flow \
		options:key=flow
}

addTunnel(){
	ovs-vsctl add-port $bridge $bridge-vxlan-0 -- set interface $bridge-vxlan-0 \
		type=vxlan \
		options:remote_ip=flow \
		options:key=flow
}

OPTS=$( getopt -o h -l controller:,bridge:,datapath-id: -- "$@" )
if [ $? != 0 ]
then
	exit 1
fi

eval set -- "$OPTS"

while true ; do
	case "$1" in
		-h)
			usage
			exit 0;;
		--bridge)
			bridge=$2
			shift 2;;
		--datapath-id)
			datapath_id=$2
			shift 2;;
		--controller) 
			controller=$2
			shift 2;;
		--) shift; break;;
	esac
done

addBridge

if [ ! -z "$datapath_id" ]
then
	setDPID
fi

setOfVersion
setController
setManager
addGPETunnel
addTunnel

exit 0
