# WifiHandler

Wifihandler handles communication with the mower backend, it does this by exposing a interface that will automatically communicate with the backend for you, wheter you need to receive or send data.
The Wifihandler is async so the user passes in a lambda function that will be called with the relevant data when the call is finished.

## `getPosition(PositionGetListener positionListener) : void`

**Description** : fetches a position from the backend

### Use:

```java
WifiHandler.getPosition((x, y) -> {
    // Do something with x and y
});
```

## `postPosition(double x, double y) : void`

**Description** : post the mowers position to the backend

### Use:

```java
WifiHandler.postPosition(1.0, 2.0);
```
