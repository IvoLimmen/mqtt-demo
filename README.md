# mqtt-demo
Small demo using Vert.x and MQTT

# Requirements

 * Java 8
 * Maven 3.5.0
 * MQTT server

# MQTT Server

 Software has been tested using mosquitto 1.4.8 under Linux Mint 18.1. Theoretically any MQTT 3.1.1 server running 
 locally should suffice for this demo.

# Building from scratch

 1. Checkout
 2. mvn clean install

# Running the demo

 The root of the project contains startup scripts. Do the following:

 1. Start the MQTT server
 2. Start ./start-domain in a shell
 3. Start ./start-alert-service
 4. Start ./start-device
 5. Optionally start more devices...

# What does it do?

The domain service receives all messages on a topic. And send messages to the alert-service if a threshold is reached.

Each device will post a temperature every 5 seconds. Each device has a unique ID.

The alert-service will notify (only by logging now) if something is posted on alert topic.

When you start a device it will spin up 50 instances.

Current configuration file will use a local mqtt server and has a max of 1000 simultaneous messages (max in flight).
