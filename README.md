Il-2 Simple Server Daemon
-------------------------

Il-2 Simple Server Daemon is a very simple server controller for the Il-2 Sturmovik 1946 dedicated server. It aims to be an easy-to-use tool with IL-2 DCG integration.

### Requirements:

[Java JRE 7][1] must be installed.

### Implemented:

- Cross-platform compatibility: Simple Server Daemon is based upon Java SE 7, the JavaFX 2.x framework and Clojure. As such, it is capable of running on most desktop operating systems as long as the JavaFX runtime is available.
- Remote server console connection: As long as you have unblocked access to the server console port via TCP and your IP is whitelisted in confs.ini, you should be able to connect.
- Direct access to the remote console via the interface: Simple Server Daemon features a console area for direct command entry and console monitoring.
- Local usage for direct mission file selection and loading: Select your il2server.exe and .mis file directly.
- Remote mission loading using internal IL-2 mission path: If you are running Simple Server Daemon remotely, you can use the internal IL-2 mission directories directly, e.g. Net/dogfight/DCG/dcgmission.mis
- Mission cycle loading: define a series of missions and time intervals, and Simple Server Daemon will run through the mission cycle, allowing missions to run for the interval defined.
- Settings in a single, easily editable .ini file: If you'd like to move your config to a new location, it's as easy as copying a single file.
- Version-independent difficulty settings: Difficulty settings are parsed into the program directly from the server to prevent new IL-2 patches from rendering this tool useless.

### Planned:

- IL-2 DCG integration: Seamless integration with DCG was the inspiration for this tool. On Windows, Simple Server Daemon will generate a new DCG mission when you press the Next button.
- In-game control via chat: enter macro commands into the Il-2 text chat and the controller will process them accordingly.
- Simple pilot management: Kick & Ban features are planned. Extremely detailed pilot management is not envisaged.

### Not planned:

- Statistics

### Installation:

The program .jar file and lib directory can be extracted to the location of your choice. You must ensure that an up-to-date version of the Java 7 runtime is installed. After extraction the program can be started by double-clicking the .jar file.

### License

Copyright © 2014 David Gregory

Distributed under the Eclipse Public License version 1.0.

[1]: http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html