# INF4410-A15

## TP 1

### Server environment
Ports must be opened. With iptables use
```bash
iptables -A INPUT -p tcp --dport 14001 -j ACCEPT
```

Default port for rmiregistry is 1099
Each object served must be exposed (through *bind* or *rebind*) on a defined port and the port opened if
the server is behind a firewall. Failing to do so will expose the object on a random port.

In order to start rmiregistry in IPv4 TCP, use this environment variable
```bash
export _JAVA_OPTIONS="-Djava.net.preferIPv4Stack=true"
```

Server hostname can be defined using the following
```bash
export _JAVA_OPTIONS="-Djava.rmi.server.hostname=132.207.4.36
```
This can also be added to the server bash script

### Building
Build with **ant**, which uses the *build.xml* file. 

### Windows particularities
To launch the processes under windows, use the scripts available as some extra JVM configuration is required. 

Before launching the server, run *start_rmiretistry.cmd* to avoid *ClassNotFound* exception.

When building in Windows using **ant**, the *jar* files might not work. Using the scripts bypasses these issues. Launching from an IDE also works, as long as the run configurations are properly configured.  
