function updateMap(lineRef, latitude, longitude) {

    let colorMap = {};
    colorMap['117'] = 'blue';
    colorMap['125'] = 'red';
    colorMap['129'] = 'green';

    console.log('colorMap for line 125 = ' + colorMap[lineRef]);

    const centerProj = ol.proj.fromLonLat(["0.250479", "52.410062"])

    const features = [];
    features.push(new ol.Feature({
      geometry: new ol.geom.Point(ol.proj.fromLonLat([
        longitude, latitude
      ]))
    }));

    // create the source and layer for random features
    const vectorSource = new ol.source.Vector({
        features
    });
    const vectorLayer = new ol.layer.Vector({
        source: vectorSource,
        style: new ol.style.Style({
          image: new ol.style.Circle({
            radius: 3,
            fill: new ol.style.Fill({color: colorMap[lineRef]})
          })
        })
    });
    // create map and add layers
    const mapElement = document.getElementById("map");
    if(mapElement.hasChildNodes()) {
        map.setTarget("map")
        //let view = map.getView();
        //view.setCenter(ol.proj.fromLonLat([longitude, latitude]));
        map.getLayers().forEach(function(layer) {
            if(layer instanceof ol.layer.Vector) {
                console.log("forcing refresh of vector layer")
                layer.getSource().clear();
                layer.getSource().addFeatures(features);
            }
        });

    } else {
        map = new ol.Map({
            target: 'map',
            layers: [
              new ol.layer.Tile({
                source: new ol.source.OSM()
              }),
              vectorLayer
            ],
            view: new ol.View({
              center: centerProj,
              zoom: 13
            })
        });
    }
}
