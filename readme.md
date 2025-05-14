# Flatshire Bus Info Service (FBIS)

[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/Simon-Payne/fbis/badge)](https://scorecard.dev/viewer/?uri=github.com/Simon-Payne/fbis)

## Overview
This is a tool I developed in response to the practical need to know where my local village bus was to be found at any 
particular time. This happened because my car was at the dealership for some repairs (let's not go there!) and the best option for getting about 
was to use public transport.

## Bus Open Data Service
**FBIS** uses publicly available [Bus Open Data Service (BODS)](https://data.bus-data.dft.gov.uk/) datasets and web services to produce innovative public-facing
bus info applications. BODS data is supplied by the UK Department of Transport Bus Open Data Service and is freely available for download on condition that the user registers and obtains an API key.

The service uses [OpenStreetMap](https://www.openstreetmap.org/) and [OpenLayers](https://openlayers.org/), and given a pair of latitude-longitude coordinates, plots the current position of the bus.

## How to build the FBIS Service

### Requirements: -
 * Apache Maven 3.0.0+
 * Java SDK 17
 * Register with BODS for an API key

### Optionally: -
 * Register with NVD for an API key (saves time running the Maven build).

### Steps to fetch, build and run the project
1. Clone the source code to a local directory.
2. Configure the Maven build (see below). 
2. Open your favourite shell and CD to that directory.
3. Run command `mvn install` to build and test the code and copy the binaries to your local Maven repository.
4. Run command `mvn spring-boot:run` to start the server.
5. Open a browser at URL http://localhost:8080 to use the service.

### Video of service in operation
[Embed link to a video here.]

### Configuring the Maven build
Two pieces of data are required without which the project won't run.

 * Add a `server` to Maven settings.xml that holds the credentials for the NVD API databases download.
Alternatively, you can simply embed the API key as a property in the root pom.xml file but this is not recommended as it is possible to expose the secret by running the Maven build in debug.
 * Add the BODS API key to your server's environment or configure it directly in the `application.properties` file (not recommended).

### Operating profiles
The server can be run in default profile which contacts the BODS web services to fetch feed data.
A `demo` profile supplies hardcoded route data instead so is an option for offline use. It also updates the position of the bus more quickly than realtime use for is good for demonstration purposes,as the name implies.

## For the curious
*Flatshire* is a humorous name I often use to refer to Cambridgeshire as it is largely, um, flat.
The example data used for the demo version of the service is drawn from the buses operated by
the local bus company that serves our village.
