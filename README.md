# Binary Protocol

Poznan University of Technology
Jacek Eichler

Projet is written in Java. The purpose of it is to make communicator which encodes packets to binary form and sends them over TCP.
There is a raport attached ( pdf file), but unfournately it's in polish. Screenshot which shows how it works :

Commands:
- !accept (0001)
- !available (0010)
- !disconnect (0011)
- !exit (0100)
- !invite (0101)
- !reject (0110)

Operation codes:
- 0000 sending casual message
- 0001 second client has accepted the invitation
- 0010 checking other client availability
- 0011 disconnecting from other client
- 0100 disconnecting from a server
- 0101 inviting other client to chat
- 0110 rejecting invitation

Answer codes:
- 000 sending initializing packet
- 001 sending a message
- 010 server replays that client accepted invitation
- 011 server replays that second client is available
- 100 server replays that  second client has disconnected
- 101 server replays that second client has left the chat
- 110 server replays that you have been invited to chat
- 111 server replays that client has declined your invitation

![1234567](https://user-images.githubusercontent.com/39658861/56096943-9a920900-5eee-11e9-811b-c0cc877674db.png)


Initializing packet for client1. SessionId send 312(101111000). Operation field set to 0.

![inivte](https://user-images.githubusercontent.com/39658861/56097234-dda1ab80-5ef1-11e9-8054-8fd01e58c52d.png)

Invite send to second client. Operation field 5(0101), asnwer field 0 (000), message: !invite

