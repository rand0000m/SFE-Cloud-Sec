# How to test

To test the deployed infrastructure, a ping and an HTTP connection are necessary (to test whether or not it goes through the SFC).

## Ping test

```
ssh root@192.168.33.10
cloud1# docker attach h35-2
h35-2# ping 10.0.36.4
**SHOULD WORK**
# Detach from docker using <Ctrl + P> <Ctrl + Q>
cloud1# exit
```

## HTTP test

```
ssh root@192.168.33.15
cloud6# docker attach h36-4
h36-4# python -m SimpleHTTPServer 80
<Ctrl-P><Ctrl-Q>
cloud6# exit
ssh root@192.168.33.10
cloud1# docker attach h35-2
h35-2# curl http://10.0.36.4
**SHOUD RETURN THE FOLDERS IN / OF h36-4
# Detach from docker using <Ctrl + P> <Ctrl + Q>
cloud1# exit
```

To ensure the trafic goes through the SFC, you may use the following commands and look at the number of packets going through the SFs :

```
ssh root@192.168.33.12 ovs-ofctl dump-flows sw3
ssh root@192.168.33.14 ovs-ofctl dump-flows sw5
```
