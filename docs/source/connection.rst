********************
Connection to a peer
********************

In order to send blocks to other people, you will need to find peers. This will be done with the help of the underlying communication protocol of ` App-To-App Communicator<https://github.com/Tribler/app-to-app-communicator>`_. This app creates a global connected network without the need for a central server. This is made hard by the NATs and Firewalls of ISPs from the cellular network providers, the home network providers and individual home routers. The App-to-App communication finds a way to get around the NATs by NAT puncturing, for more information about NAT and Firewall traversal see `NAT Traversal < https://www.tribler.org/NATtraversal/>`_.

In IPv8, this is done with help of `Dispersy <https://dispersy.readthedocs.io/en/devel/system_overview.html#overlay>`_.

There are multiple ways to connect to a peer: either via a local network, a global network or bluetooth (note that bluetooth is not working perfectly).  First the app tries to connect to the hardcoded bootstrap phone over the internet, when you have no active global internet connection you can change the bootstrap phone to a phone with a private IP address. In both ways when the bootstrap phone response to the introduction request eventual other nodes in the network are send via punctures to your phone. How a typical NAT puncture goes please see the following steps:
1.	When peer A starts App-to-app communicator, a connection request to peer B is made.
2.	Upon connection peer B chooses another connected peer, peer C, and sends the address of peer C to peer A as introduction response message.
3.	Peer B sends peer C a puncture request.
4.	Peer C sends a puncture message to peer A to punch a hole in its own NAT.

// picture of the NAT Puncture

After the different users have connection with you and are listed in the incoming peer list on the main activity, you can communicate with other peers by clicking on them. Now the app will go through the steps as explained in :ref:`creating-block-label`.
//Sending a transaction to another peer via bluetooth requires you to pair the devices via the //Android bluetooth manager. After they are paired, the app will list the devices your device is //paired with. To initiate a transaction, press on one of the devices.

Connection
============
The class `Communication` is responsible for handling the data that is received either to bluetooth or WiFi. This class is abstract so that both type of connections use the same logic when a message is received. The classes `BluetoothConnection` and `NetworkConnection`  both have as parent `Communication`. The most imporant function of these two classes is `sendMessage`, which sends a message to a peer. A network connection will create a ‘’’’’’’’’’’’’’new `ClientTask` TODO‘’’’’and a bluetooth connection will create a `ConnectThread`, which will both send the message to the peer.


Network
============
The connection is made by using the `ServerSocket class <https://developer.android.com/reference/java/net/ServerSocket.html>`_. The implementation in TrustChain Android is done by a client-server style model. Although, since every device is a server can't really be seen as an exact client-server model. All outgoing messages, like sending a crawl request or half block, is done via client tasks. The server handles the incoming messages. It checks whether it has received a half block or a crawl request and calls handles the response by calling either ``synchronizedReceivedHalfBlock`` or ``receivedCrawlRequest``.

If, from looking at the source code, it is not yet clear how the connection is made, please look into other `Android server/client tutorials <http://android-er.blogspot.nl/2014/02/android-sercerclient-example-server.html>`_ that can be found online.



The simplest way for connecting via IP, which does not have to deal with possible NAT puncturing or port forwarding is connecting to a local IP on the same WiFi network. A guaranteed method involves setting up a WiFi hotspot on one of the devices and letting the other peer connect to this hotspot. WiFi networks, which make use of IPv6 are not guaranteed to work.

.. figure:: ./images/connection_example.png
	:width: 300px

Example of connecting to a peer using on a device using a WiFi hotspot.


Bluetooth
==================================
Bluetooth works similar to WiFi: a server is created which is listening for messages, and a client send messages to server. However, with WiFi the messages are sent through two different ports and thus two different connections. This is not possible with Bluetoooth so the client will reuse the socket.

.. figure:: ./images/connection_example_bluetooth.jpeg
	:width: 300px




Links to code
=============
* LINK TO OWN CLASSES
* LINK TO OWN CLASSES
* `ConnectThread implementation (ConnectThread.java) <https://github.com/wkmeijer/CS4160-trustchain-android/blob/master/app/src/main/java/nl/tudelft/cs4160/trustchain_android/connection/bluetooth/ConnectThread.java>`_
* `AcceptThread implementation (AcceptThread.java) <https://github.com/wkmeijer/CS4160-trustchain-android/blob/master/app/src/main/java/nl/tudelft/cs4160/trustchain_android/connection/bluetooth/AcceptThread.java>`_
