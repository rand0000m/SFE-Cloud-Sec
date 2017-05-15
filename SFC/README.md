# Service Function Classifier preparation

Those scripts performs the installation and configuration of an SFC :

- `install.sh` : Installs OVS with the NSH patch as well as Docker Engine
- `config.sh` : Initializes the OVS with the main bridge, connects to the SDN Controller and adds tunnels for the containers
- `add-cont.sh` : Script to instanciate a docker and connect it to the SFC
- `ovswork.sh` : Script used by `add-cont.sh` to connect a docker to an OVS instance
