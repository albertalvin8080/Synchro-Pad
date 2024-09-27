# Description
Synchro Pad is a TCP text editor made with Java Swing and design patterns. Its instances use TCP java sockets to communicate with the server, which is responsible for synchronizing the behavior of all instances.

## Debug mode
```shell
mvn clean package -Pdebug
```

## Production mode
```shell
mvn clean package -Pprod
```