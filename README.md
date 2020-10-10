# Shop Floor Service Connector

This project aims to provide a user friendly framework to implement a message based, service oriented architecture in the shop floor.

The core requirements are
- user-friendliness with the support of common patterns like pub sub and client server
- peer to peer, with no need for central infrastructure 
- possible support of multiple languages 
- low hardware requirements to support embedded devices
- performance

The middleware is build up from two parts:
 - a "core" which can be run on industry PCs or servers and who is responsible for service registry and message routing
 - a lightweight "adapter" part which can be run anywhere and executes the services

Communication between them can be established via IPC or TCP.

As core libraries, ZMQ and Protobuf are used. The registry is implemented with Hazelcast.
