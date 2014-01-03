Il-2 Simple Server Daemon
-------------------------

Il-2 Simple Server Daemon is a very simple server controller for the Il-2 Sturmovik 1946 dedicated server. It aims to be an easily maintained tool with IL-2 DCG integration.

### Planned features:

- Cross-platform compatibility: Simple Server Daemon is based upon Java SE 7, the JavaFX 2.x framework and Clojure. It was initially written and tested on Arch Linux and latter rewritten on Windows 8. The original test server ran on CentOS 6.4 via WINE, and was play-tested from a Windows 7 client. Issues from all three major platforms will be investigated.
- Remote server console connection: As long as you have unblocked access to the server console port via TCP and your IP is whitelisted in confs.ini, you should be able to connect.
- Direct access to the remote console via the interface: Simple Server Daemon features a console area for direct command entry and console monitoring.
- Local usage for direct mission file selection and loading: Select your il2server.exe and .mis file and you're ready to go.
- Mission cycle loading: define a series of missions and a time interval, and Simple Server Daemon will run through the mission cycle, allowing missions to run for the interval defined.
- Remote mission loading using internal IL-2 mission path: If you are running Simple Server Daemon remotely, you can use the internal IL-2 mission directories directly, e.g. net/dogfight/DCG/dcgmission.mis
- IL-2 DCG integration: Seamless integration with DCG was the inspiration for this tool. On Windows, Simple Server Daemon will generate a new DCG mission when you press the Next button.
- In-game control via chat: enter macro commands into the Il-2 text chat and the controller will process them accordingly.
- Settings in a single, easily editable .ini file: If you'd like to move your config to a new location, it's as easy as copying a single file.
- Simple pilot management: Kick & Ban features are planned. Extremely detailed pilot management is not envisaged.
- Version-independent difficulty settings: User editable difficulty settings are planned to prevent new IL-2 patches from rendering this tool useless.

### Features which are not envisaged in the near future:

- Statistics

### Installation:

The program .jar file can be extracted to the location of your choice. You must ensure that an up-to-date version of the Java 7 runtime is installed. After extraction the program can be started by double-clicking the .jar file.

### License

Copyright © 2014 David Gregory

Distributed under the Eclipse Public License version 1.0.