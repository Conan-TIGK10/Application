# BluetoothHandler

BluetoothHandler handles connecting to the robot, and keeping that connection alive. The class also exposes function to write data to the robot.
If you want to get bluetooth data from the robot sign upp for callbacks with the AddCallback function. BluetoothHandler works as a static singleton, exposing only one object to the rest of the application.

## `addCallback(final BluetoothCallback callback) : void`

**Description** : Register for notifications when bluetooth message has been received
### Use:

```java
MbotHandler.getInstance().addCallback(this);
```

## `removeCallback(final BluetoothCallback callback) : void`

**Description** : Deregister for notifications when bluetooth message has been received
### Use:

```java
MbotHandler.getInstance().removeCallback(this);
```

## `start() : void`

**Description** : Start bluetooth connection with robot
### Use:

```java
MbotHandler.getInstance().connect();
```
## `stop() : void`

**Description** : Stop bluetooth connection with robot
### Use:

```java
MbotHandler.getInstance().stop();
```

## `write(final byte[] bytes) : void`

**Description** : Write a message to the robot
### Use:

```java
String message = "Hello";
BluetoothHandler.getInstance().write(message.getBytes(StandardCharsets.US_ASCII));
```
