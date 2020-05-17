# MbotHandler

MbotHandler receives sensor data from the BluetoothHandler, and uses
that data to update the robots current position and heading, Mbothandler
also notifies listeners of these updates using callback functions. Also
in this class is a thread and thread implementation used to send fake
data to the mbotHandler when you cant test using the real robot.

## `bluetoothMessage(byte[] bytes) : void`

**Description** : This function will be called when BluetoothHandler
receives a bluetooth message, this function will check if the message is
complete, if the data received starts with a '/' and ends with '&' we
have received a complete message, and the message can be passed onto the
parseMessage function. since incomplete messages can be received we use
a byteArray to build up a message until we have a message that starts
with '/' and ends with '&'

### Use:

```java
none
```

## `parseMessage(String message) : void`

**Description** : Parses data received from the robot, data is received
in the format 'GYRO,DISTANCE,LIDAR,LIGHT,MILLIS'. The acceptable ranges
are defined in the function itself, since the data we receive is quite
sporadic we have to do alot of work to make sense of it. The function
also sends updates to the backend database at a regular interval of 500
milliseconds. And we notify all the listeners that want update about the
robots current state.

Gyro: The gyro or rotation data is not absolute, the starting value is
always zero on the robot side, so we have no idea of what absolute north
is. A workaround has been implemented where we collect the first gyro
data we receive and use that as absolute north (or 0).

Millis: We receive a millisecond counter from the robot, which should be
the milliseconds elapses since the robot started. But this counter could
overflow so a overflow protection implemenation has been intregrated. We
care about the difference in millis from the last update to the current
update because we can use that difference to interpolate the robots
position smoothly from one point to another.

Lidar: Lidar data is from a scale of 0-400. We use this data to infer
that the collision avoidance system has been activated, we dont ever
receive a update that the collision avoidance has been activated but on
the robots side the collision avoidance system is activated when the
sensor goes under 20.

Light: We use the light sensor to once again infer that the collision
avoidance system on the robot has been activated. we receive a number
from 0-3, if its 0 then no light detection has been activated. 1 = left
light sensor, 2 = right light sensor, 3 = both light sensors.

Distance: The distance data we receive is very tricky. Its an absolute
value signifying the distance the robot has travelled in one direction.
so if the robot is travelling in one direction the distance value keeps
climbing upwards. the distance value resets to 0 when the robot turns or
when it drives backwards, when driving backwards the distance data
climbs into the negative. We only care about the difference between two
updates so we can use that data to update our position by multiplying
the direction by the distance traveled and adding that to our position.
This entails alot of checking to see if the distance data has been
reset, or if its climbed in one direction.

### Use:

```java
none
```
