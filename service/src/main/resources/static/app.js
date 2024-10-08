const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/bus-location-feed'
});

const subscriptionMap = new Map();

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
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
    $("#buspos117").html("");
    $("#buspos125").html("");
    $("#buspos129").html("");
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    subscriptionMap.forEach((key) => {
      subscriptionMap.delete(key);
    });
    console.log("Disconnected");
}

function subscribe(lineRef) {
    let subscription = stompClient.subscribe("/topic/buspos/" + lineRef + "/", (response) => {
        const posData = JSON.parse(response.body)
        const msg = "Bus " + posData.lineRef + " is at position " + posData.latitude + ":" + posData.longitude
        console.log(msg);
        showBusPos(msg, posData);
    });
    console.log("subscribed to lineRef " + lineRef + " subscription id " + subscription.id)
    subscriptionMap.set(lineRef, subscription.id)
    $("#subscribe" + lineRef).css("background-color","yellow")
}

function showBusPos(message, posData) {
    $("#buspos" + posData.lineRef).html("<tr><td>" + message + "</td></tr>");
    window.latitude = posData.latitude;
    window.longitude = posData.longitude;
    $.getScript("/map.js", function() {
        console.log("Updating map latitude " + window.latitude + ", longitude " + window.longitude);
        updateMap(posData.lineRef, window.latitude, window.longitude);
    });
}

function unsubscribe(lineRef, subscriptionId) {
    stompClient.unsubscribe(subscriptionId)
    console.log("unsubscribed from lineRef " + lineRef + " subscription id " + subscriptionId)
    subscriptionMap.delete(lineRef)
    $("#subscribe" + lineRef).css("background-color","white")
}

function toggleSubscription(lineRef) {
    let subscriptionId = subscriptionMap.get(lineRef)
    if(subscriptionId !== undefined) {
        unsubscribe(lineRef, subscriptionId)
    } else {
        subscribe(lineRef);
    }
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => connect());
    $( "#disconnect" ).click(() => disconnect());
    $( "#subscribe117" ).click(() => toggleSubscription(117));
    $( "#subscribe125" ).click(() => toggleSubscription(125));
    $( "#subscribe129" ).click(() => toggleSubscription(129));
});

