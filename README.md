# Sherlock

Sherlock is service discovery implemented to be AP (from CAP). To do this, it uses Akka with Distributed Data (CRDT).

Start with:
```
# First seed node with JMX enabled to acces cluster management through the client
sbt run -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -DAKKA_PORT=2552 -DPORT=9090
# Second seed node
sbt run -DAKKA_PORT=2553 -DPORT=9091
# Next random node
sbt run -DAKKA_PORT=0 -DPORT=0
```