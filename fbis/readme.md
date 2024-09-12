# Flatshire Bus Info Service (FBIS)

Leveraging publicly available [Bus Open Data Service (BODS)](https://data.bus-data.dft.gov.uk/) datasets and web services to produce innovative public-facing 
bus info applications.

## Steps to implement service
1. Write Spring Boot client to call BODS [Location data API](https://data.bus-data.dft.gov.uk/api/buslocation-openapi/)(requires sign-in) endpoints to retrieve current bus location coordinates for 
the 125 bus from Ely to Little Downham.
2. Using [OpenStreetMap](https://www.openstreetmap.org/) and [a suitable web map library](https://wiki.openstreetmap.org/wiki/Software_libraries#Web_maps), and given a pair of latitude-longitude coordinates, plot the position of the bus.
3. Join the two together to present a dynamic map-based view of the position of an arbitrary bus.
4. Configure a Raspberry Pi plus a reused tablet screen to display the bus location in my kitchen.


[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/Simon-Payne/fbis/badge)](https://scorecard.dev/viewer/?uri=github.com/Simon-Payne/fbis)