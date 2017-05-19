#!/bin/bash

container_image="alagalah/odlpoc_ovs230"
name=""
container_id=""
switch=$(hostname)
container_ip=""
container_mac=""
container_broadcast=""
container_router_ip=""


usage(){
	printf "Container instantiation script for SFC :\n"
	printf "\t--container-image=alagalah/odlpoc_ovs230   : Docker image used for the container\n"
	printf "\t--name                                     : Name of the container\n"
	printf "\t--switch=$switch                           : OVS Switch to use\n"
	printf "\t--ip                                       : IP address and subnet of the container (format: 192.168.0.2/24)\n"
	printf "\t--broadcast                                : Broadcast address of the container\n"
	printf "\t--mac                                      : MAC address of the container\n"
	printf "\t--router-ip                                : IP address of the router of the container\n"
	printf "\t-h                                         : prints this message\n"
}

if [ $# -eq 0 ]
then
	usage
fi

launchContainer(){
	container_id=$(docker run -d --net=none --name=$name -h $name -t  -i --privileged=True $container_image /bin/bash)
}

linkContainer(){
	sh /vagrant/SFC/ovswork.sh $switch $container_id $container_ip $container_broadcast $container_router_ip $container_mac $name
}

OPTS=$( getopt -o h -l container-image:,name:,switch:,ip:,broadcast:,mac:,router-ip: -- "$@" )
if [ $? != 0 ]
then
	exit 1
fi

eval set -- "$OPTS"

while true ; do
	case "$1" in
		-h)
			usage;
			exit 0;;
		--container-image)
			container_name=$2
			shift 2;;
		--name)
			name=$2
			shift 2;;
		--switch)
			switch=$2
			shift 2;;
		--ip)
			container_ip=$2
			shift 2;;
		--broadcast)
			container_broadcast=$2
			shift 2;;
		--mac)
			container_mac=$2
			shift 2;;
		--router-ip)
			container_router_ip=$2
			shift 2;;
		--) shift; break;;
	esac
done

if [ -z "$name" ]
then
	echo "Container name is required"
	exit 1
fi

if [ -z "$container_ip" ]
then
	echo "Container IP address is required"
	exit 1
fi

if [ -z "$container_broadcast" ]
then
	echo "Container broadcast address is required"
	exit 1
fi

if [ -z "$container_mac" ]
then
	echo "Container MAC address is required"
	exit 1
fi

if [ -z "$container_router_ip" ]
then
	echo "Container Router IP address is required"
	exit 1
fi

launchContainer
linkContainer

exit 0
