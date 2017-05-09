# Cloud Sec

Projet d'agent de sécurité pour OpenDayLight

Commande de compilation : 

```
time mvn -Dmaven.site.skip=true -Dpmd.skip=true -Dpcd.skip=true -Dcheckstyle.skip=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.insecure=true -DskipTests -Dmaven.javadoc.skip=true clean install -T 2C -q
```

# Install and test instructions

Opendaylight is supposed to be installed at `/path/to/opendaylight`.

## Compile and launch in OpenDayLight

```
mvn clean install -DskipTests -T 2C
mkdir -p /path/to/opendaylight/system/com/orange -p
cp ~/.m2/repository/com/orange/cloudsec /path/to/opendaylight/system/com/orange -r
cd /path/to/opendaylight
bin/karaf
```

## Launch virtual machines

Two servers are used to host the virtual machines : `servera` and `serverb`. The commands below are to be executed on both servers.

Make sure to replace the IP addresses to match your network.

In our test network, we are using the following addresses : 

- 172.16.0.113 : ODL Controller
- 172.24.110.10 : Server A
- 172.24.110.11 : Server B
- 172.24.110.12 : VM 1
- 172.24.110.13 : VM 2
- 172.24.110.14 : VM 3
- 172.24.110.15 : VM 4
- 172.24.110.16 : VM 5
- 172.24.110.17 : VM 6

```
cd ~
git clone https://github.com/opendaylight/groupbasedpolicy
cd demos/gbpsfc-env
echo 'export NUM_NODES=3
export ODL="172.16.0.113"
export SUBNET="172.24.110."' > env.sh
source ./env.sh
```

The Vagrant File needs to be updated to fit our network :

- Define the subnet as starting from 172.24.110.12
- Set the hostname as `gbpsfcX` with X being the correct number (1-3 for server B and 4-6 for server A)
- Use a bridge to connect to the network (and not a private network)

To do so, we must change on both server A and B the file like so :

```
   # ip configuration
   ip_base = (ENV['SUBNET'] || "172.24.110.")
   ips = num_nodes.times.collect { |n| ip_base + "#{n+15}" }

   (...)

   num_nodes.times do |n|
     config.vm.define "gbpsfc#{n+4}", autostart: true do |compute|
       vm_ip = ips[n]
       vm_ip_sflow = ips_sflow[n]
       vm_index = n+4
       compute.vm.box = "tomas-c/gbpsfc-trusty64"
       compute.vm.box_version = "1.0.0"
       compute.vm.hostname = "gbpsfc#{vm_index}"
       compute.vm.network "public_network", ip: "#{vm_ip}", netmask: "255.255.0.0", bridge: "enp6s0"

       # default router
       compute.vm.provision "shell",
         run: "always",
         inline: "route add default gw 172.24.254.254"
```

PS : Make sure to change `vm_index = n+1`, `#{n+1}` and `#{n+12}` for server B.

Then you may create the virtual machines with : 

```
vagrant up
```

The last fix corrects the ODL IP environment variable and networking informations : (execute on servers A and B) 

```
for i in 1 2 3 4 5 6; do vagrant ssh gbpsfc$i -c "echo 'export ODL=172.16.0.113' >> ~/.profile"; done
for i in 1 2 3 4 5 6; do vagrant ssh gbpsfc$i -c "sudo ip r del default via 172.24.254.254; sudo ip r add 0.0.0.0/1 via 172.24.254.254; sudo ip r add 128.0.0.0/1 via 172.24.254.254"; done
```

## Update and copy the config files

Update with the correct ip addresses :

```
sff1="172.24.110.13"
sff2="172.24.110.15"
sff1ip=$(python -c "import binascii; import socket; print '0x' + binascii.hexlify(socket.inet_aton('$sff1')).upper()");
sff2ip=$(python -c "import binascii; import socket; print '0x' + binascii.hexlify(socket.inet_aton('$sff2')).upper()");
sed -i "s/0xC0A83247/$sff1ip/" sf-flows.sh
sed -i "s/0xC0A83249/$sff2ip/" sf-flows.sh
sed -i "s/0xC0A83247/$sff1ip/" sf-config.sh
sed -i "s/0xC0A83249/$sff2ip/" sf-config.sh
```

> Beware that some ip addresses are hardcoded in the module (should be corrected in a future release).

Copy of the config files to the virtual machines :

```
for i in 1 2 3 4 5 6; do vagrant ssh gbpsfc$i -c "cp /vagrant/demo-symmetric-chain/{get-nsps.py,infrastructure_config.py,sf-config.sh} /vagrant"; done
```

## Configuration of ODL and VM

On servers A and B :

```
for i in 1 2 3 4 5 6; do vagrant ssh gbpsfc$i -c "sudo -E /vagrant/infrastructure_launch.py"; done
```

To configure ODL : 

```
doc/demo-sf.sh
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/operations/cloud-sec:create-function-path" -X POST -d '{"input":{"unused":"test"}}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/operations/cloud-sec:create-tunnel" -X POST -d '{"input":{"unused":"test"}}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/operations/cloud-sec:create-tenant" -X POST -d '{"input":{"unused":"test"}}'
curl -s -H "Content-Type: application/json" -u admin:admin "http://127.0.0.1:8181/restconf/operations/cloud-sec:create-endpoints" -X POST -d '{"input":{"unused":"test"}}'
```

On servers A and B :

```
for i in 1 2 3 4 5 6; do vagrant ssh gbpsfc$i -c "sudo -E /vagrant/infrastructure_launch.py"; done
```

## Test

The demo should be operationnal, to test, we will perform some network activity :

```
# On server B
vagrant ssh gbpsfc1
sudo docker attach h35-2
# Type enter twice to see prompt
ping 10.0.36.4
# Ping should work
while true; do curl 10.0.36.4; sleep 5; done
[Ctrl-P-Q to detach container without destroying it]

# On server A
vagrant ssh gbpsfc6
sudo docker attach h36-4
# Type enter twice
python -m SimpleHTTPServer 80
# You should see requests from 10.0.35.2
[Ctrl-P-Q to detach container without destroying it]
```

You may now use `vagrant ssh gbpsfcX` to look at the traffic between each nodes (eg. using `ovs-dpctl dump-flows` or `ovs-ofctl dump-flows swX`).

## Ending the demonstration

The following command cleans the configuration on the nodes (should be ran on server A and B) :

```
for i in 1 2 3 4 5 6; do vagrant ssh gbpsfc$i -c "sudo ovs-vsctl del-br sw$i; sudo ovs-vsctl del-manager; sudo /vagrant/vmclean.sh; sudo /vagrant/sflow/stop_sflow-rt.sh  >/dev/null 2>&1"; done
```
