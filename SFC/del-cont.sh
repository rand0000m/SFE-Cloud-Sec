#!/bin/bash

name=""
bridge=""
container_id=""

stopContainer(){
	docker stop $name
	docker rm $name
}

unlinkContainer(){
	while read dev mnt fstype options dump fsck
	do
	    [ "$fstype" != "cgroup" ] && continue
	    echo $options | grep -qw devices || continue
	    CGROUPMNT=$mnt
	done < /proc/mounts

	NSPID=$(head -n 1 $(find "$CGROUPMNT" -name "$container_id*" | head -n 1)/tasks)
	[ "$NSPID" ] || {
	    echo "Could not find a process inside container $GUEST_ID"
	    exit 1
	}

	# Step 2 : Delete the netns
	ip netns delete $NSPID

	# Step 3 : Delete interfaces from the bridge
	ovs-vsctl del-port $bridge vethl-$name
}

OPTS=$( getopt -o h -l name:,switch: -- "$@" )
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
		--switch)
			bridge=$2
			shift 2;;
		--name)
			name=$2
			shift 2;;
		--) shift; break;;
	esac
done

if [ -z "$bridge" ]
then
	echo "Bridge name is required"
	exit 1
fi

if [ -z "$name" ]
then
	echo "Container name is required"
	exit 1
fi

container_id=$(docker ps --no-trunc | grep $name | cut -f1 -d" ")
if [ -z $container_id ]
then
	echo "Container does not exist"
	exit 1
fi
unlinkContainer
stopContainer
