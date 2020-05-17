# WifiHandler

Wifihandler handles communication with the robot backend, it does this
by exposing a interface that will automatically communicate with the
backend for you, wheter you need to receive or send data. The
Wifihandler is async so the user passes in a lambda function that will
be called with the relevant data when the call is finished. The
WifiHandler also serves as a way to know and be notified of when the
wifi is available or not.

## `getLastPosition(PositionGetListener positionListener) : void`

**Description** : fetches a position from the backend

### Use:

```java
WifiHandler.getLastPosition((id, x, y, rotation, sessionId) -> {
    // Do something with data
});
```

## `postPosition(final double x, final double y, final int rotation, final long millis, final PositionPostListener postListener) : void`

**Description** : post the mowers position and rotation to the backend

### Use:

```java
WifiHandler.getInstance().postPosition(2.1, 2.2, 180, 123456,  posId -> {
    // Do something with posId
});
```

## `postCollision(final double x, final double y, final int rotation, final long millis, final CollisionPostListener collisionListener) : void`

**Description** : post that a collision happend at position and
rotation.

### Use:

```java
WifiHandler.getInstance().postCollision(2.1, 2.2, 180, 123456,  () -> {
});
```

## `createSession(final String sessionName, final SessionCreateListener createListener) : void`

**Description** : Create a new session.
### Use:

```java
WifiHandler.getInstance().createSession("name", (noError, message) -> {
    if (noError) {
    } else {
    }
});
```
## `addCallback(final WifiCallback callback) : void`

**Description** : Register for notifications when wifiState has changed
### Use:

```java
WifiHandler.getInstance().addCallback(this);
```

## `removeCallback(final WifiCallback callback) : void`

**Description** : Deregister for notifications when wifiState has
changed
### Use:

```java
WifiHandler.getInstance().removeCallback(this);
```

