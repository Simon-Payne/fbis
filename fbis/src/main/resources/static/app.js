const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/bus-location-feed'
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
    /*stompClient.subscribe('/topic/buspos', (buspos) => {
        const posData = JSON.parse(buspos.body)
        const msg = "Bus " + posData.lineRef + " is at position " + posData.latitude + ":" + posData.longitude
        console.log(msg);
        showBusPos(msg, posData);
    });*/
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#buspos").html("");
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function subscribeToBusUpdates(lineRef) {
    stompClient.subscribe("/topic/buspos/" + lineRef + "/", (buspos) => {
        const posData = JSON.parse(buspos.body)
        const msg = "Bus " + posData.lineRef + " is at position " + posData.latitude + ":" + posData.longitude
        console.log(msg);
        showBusPos(msg, posData);
    });
}

function showBusPos(message, posData) {
    $("#buspos").html("<tr><td>" + message + "</td></tr>");
    window.latitude = posData.latitude;
    window.longitude = posData.longitude;
    $.getScript("/map.js", function() {
        console.log("Updating map latitude " + window.latitude + ", longitude " + window.longitude);
        updateMap(posData.lineRef, window.latitude, window.longitude);
    });
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => connect());
    $( "#disconnect" ).click(() => disconnect());
    $( "#subscribe117" ).click(() => subscribeToBusUpdates(117));
    $( "#subscribe125" ).click(() => subscribeToBusUpdates(125));
    $( "#subscribe129" ).click(() => subscribeToBusUpdates(129));
});

