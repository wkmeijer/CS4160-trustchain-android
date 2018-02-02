************************
Connection to a peer
************************

In order to send blocks to other people, you will need to find peers. This will be done with the help of the underlying communication protocol of the App-To-App Communicator <https://github.com/Tribler/app-to-app-communicator>. Since the same protocol as the App-To-App Communicator is maintained, the Trustchain Android is also backwards compatible with the communicator app. This app creates a global connected network without the need for a central server. This is difficult because of the network address translations (NATs) and Firewalls of internet service providers (ISPs) from the cellular network providers, the home network providers and individual home routers. The App-to-App communication finds a way to get around the NATs by NAT puncturing, for more information about NAT and Firewall traversal see NAT Traversal < https://www.tribler.org/NATtraversal/>.

While NAT puncturing is only needed with the current internet protocol IP4, IP6 has the promise to connect directly to each peer without the need for NATs. If one considers the internet protocol IP8 suggested by the TU Delft, one handles peer discovery with the help of Dispersy <https://dispersy.readthedocs.io/en/devel/system_overview.html#overlay>.

There are multiple ways to connect to a peer: either via a local network or a global network. First the app tries to connect to the hardcoded bootstrap phone over the internet, when you have no active global internet connection you can change the bootstrap phone address to a phone with a private IP address (which only works currently when you are both connected to the same private network). In both ways when the bootstrap phone responses to the introduction request, eventually all other nodes in the network are send via punctures to your phone. The following steps explain how a typical NAT puncture works:

1.	When peer A starts the App-to-app communicator, a connection request to peer B is sent.
2.	Upon connection peer B chooses another connected peer, peer C, and sends the address of peer C to peer A as an introduction response message.
3.	Peer B sends peer C a puncture request.
4.	Peer C sends a puncture message to peer A to punch a hole in its own NAT.


.. figure:: ./images/intro_puncture_req.png
   :width: 300px
   :alt: Fig. 1. How a connection is setup between peers.

After the different users have a connection with you and are listed in the incoming peer list on the main activity. Now the app will go through the steps as explained in :ref:`creating-block-label`.

Connection
============
When the Trustchain Android app has started, on the background the app opens an User Datagram Protocol (UDP) datagram port (per default this is 1873) on the local phone and a listening thread in order for the phone to be able to receive datagram packets from other phones. After that, the client tries to connect to the bootstrap server, which is denoted by “server” in Figure 2. Figure 2 also shows what a user sees after selecting a username. It shows a peer’s local IP, WAN address, name and connection type. Furthermore the right column shows peers that are in the network but where you haven’t made a successful connection with. The left column on the other hand shows peers that are ready to connect since you have made a successful connection with them. The server needs to be publicly accessible in order to function as a bootstrap of the peer to peer network so it will almost immediately shift to the left side and with a green dot in front of it. After a connection has been made with the bootstrap, several puncture requests will be made in order to establish a direct UDP connection with the other peers. These puncture requests are like the ones stated in Fig. 1, executed with the help of the bootstrap phone. After the punctures are received and therefore a hole has been punched in the NAT of each router adjacent to the phones, the respective peers will also go to the list “Active Peers” with also a green dot in front of them. Now you can press on the respective peers and add these peers to your inbox, more on this in :ref:`inbox`. For an explanation for the different colors of dots and bars please have a look at Fig 2. When a user closes the app, which means that he/she is inactive, the user will be removed after 25 seconds in order to avoid that the peerlist is showing inactive users.

.. figure:: ./images/overview_connection_explanation.png
   :width: 300px
   :alt: Fig. 2. Getting the explanation of the colors.


Network and Messages
====================
The class Network is responsible for handling the data that is received either through a local or global internet connection. Although it should be noted that a listen thread for UDP datagrams should already be initialized on the local phone’s port in order to receive datagrams from other users over the internet. It handles all the different messages a peer is able to send and receive, these are all subclasses of the Message class. The program is able to send and receive the following messages:

BlockMessage - This is a message containing a half block or a full block. The distinction between a half block or a full block will be made by the receiver, the difference is that a half block has an empty value for the linked sequence number while this is not the case for a full block.
CrawlRequest - A crawl request is sent to another peer after you click on this peer in your inbox. After receival of this request a peer sends its own whole chain to the other party.
PunctureRequest - A puncture request is sent to another party already familiar with a new peer, not yet in the active peers list of the program. After receival a puncture will be send to the new peer.
Puncture - A puncture is actually nothing more than an empty message. It has the sole purpose to punch a hole in the NAT of the receiver and from that point one can send Introduction Messages to each other.
IntroductionRequest - An introduction request is sent to the party after their respective NAT has been punctured in order to retrieve more information about the other peer like network provider and WAN address.
IntroductionResponse - When the introduction request has been received the information will be provided to the other peer.

Network Class
==============
The UDP socket is made by using the DatagramChannel class <https://docs.oracle.com/javase/7/docs/api/java/nio/channels/DatagramChannel.html>. The implementation in TrustChain Android is done by a peer-to-peer model, each peer implements a listener thread at their socket which always listens for datagram packets from other users. Sending of the messages also makes use of the same UDP socket.

If, from looking at the source code, it is not yet clear how the connection is made, please look into other Android Developer Documentation <https://developer.android.com/reference/java/nio/channels/DatagramChannel.html> that can be found online.


UML Diagram of the code covering connection
===========================================
The color scheme used in this UML diagram is the following: Red objects are objects that are not changed compared to the App-To-App communicator. Blue blocks are altered in order to handle block messages and green blocks are completely new with this Android Trustchain app compared to the communicator.


.. figure:: ./images/uml_diagram_connection.png
   :width: 300px
   :alt: Fig. 3. UML diagram.
