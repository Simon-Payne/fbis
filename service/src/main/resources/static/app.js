// set scheme to either ws or wss as required.
// Spring Boot auth.enabled property must be true if wss, false if ws.

const scheme = 'wss://'

// no need to change what's after this line
const host = window.location.host
var startDt = 0;
const endpoint = '/bus-location-feed'
console.log('brokerURL: ' + scheme + host + endpoint)
const stompClient = new StompJs.Client({
    brokerURL: scheme + host + '/bus-location-feed'
});
const dateTimeOptionsDisplay = {
  weekday: "long",
  hour: "numeric",
  minute: "numeric",
  timeZone: "Europe/London",
  hour12: true
}
const dateTimeOptionsLog = {
  year: "numeric",
    month: "numeric",
    day: "numeric",
    hour: "numeric",
    minute: "numeric",
    second: "numeric",
    hour12: false,
    timeZone: "Europe/London",
    timeZoneName: "short"
}
const dateFormatDisplay = new Intl.DateTimeFormat("en-GB", dateTimeOptionsDisplay)
const dateFormatLog = new Intl.DateTimeFormat("en-GB", dateTimeOptionsLog)

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
        var d = posData.recordedTime
        const recordedTime = new Date(d[0], d[1]-1, d[2], d[3], d[4])
        const recordedTimeDisplay = dateFormatDisplay.format(recordedTime)
        const msgDisplay = recordedTimeDisplay + " bus " + posData.lineRef + " position " + posData.latitude + ":" + posData.longitude
        const recordedTimeLog = dateFormatLog.format(recordedTime)
        const msgLog = "\"Bus pos: " + posData.lineRef + "\",\"" + recordedTimeLog + "\"," + posData.latitude + "," + posData.longitude
        console.log(msgLog);
        showBusPos(msgDisplay, posData);
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
        updateMap(posData.lineRef, window.latitude, window.longitude);
    });
}

function unsubscribe(lineRef, subscriptionId) {
    stompClient.unsubscribe(subscriptionId)
    console.log("unsubscribed from lineRef " + lineRef + " subscription id " + subscriptionId)
    subscriptionMap.delete(lineRef)
    startDt = 0;
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

