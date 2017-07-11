FROM debian:jessie-slim

MAINTAINER Mathieu Rousse version:0.1
VOLUME /build

ADD ./install-dep.sh /tmp/install-dep.sh
RUN /bin/sh /tmp/install-dep.sh
ADD ./build.sh /tmp/build.sh

CMD ["/tmp/build.sh"]
