# MainActivity

MainActivity handles the underlying application context aswell as
switching between different screens (fragments) in the app. MainActivity
also has control over the various handlers in the app such as the
bluetoothHandler. Making sure that the bluetoothhandler is turned off
when the app is paused.

## `onCreate(Bundle savedInstanceState) : void`

**Description** : Called on startup to construct mainActivity, called
automatically by platform. Loads up the mainActivity view and
initializes the bottom navigation. Loads "home" fragment

### Use:

```java
none
```

## `onPause() : void`

**Description** : called automatically when application is paused, for
example when user switches to another app. Should be used to pause
threads and save data.

### Use:

```java
none
```

## `onResume() : void`

**Description** : called automatically when application starts after
being paused, should be used to initalize data and startup threads.

### Use:

```java
none
```

## `onDestroy() : void`

**Description** : called automatically when application is destroyed,
for example when user turns off the app. Should be used to save data.

### Use:

```java
none
```

## `checkLocationPermission() : boolean`

**Description** : used to check if the application has the location
permission needed to run bluetooth. this is run at startup in the
onCreate method. user is prompted to accept and results is passed to the
onRequestPermissionResult method

### Use:

```java
this.checkLocationPermission();
```

## `onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) : void`

**Description** : called automatically after user accepts or denys a
permission request, we can handle their choiche in this function

### Use:

```java
none
```

## `onNavigationItemSelected(@NonNull MenuItem item) : void`

**Description** : tied to the bottom navigationalbar, when user clicks
on one of the buttons this function is called. is used with the
openFragment function to smoothly transition between fragments

### Use:

```java
none
```

## `openFragment(Fragment fragment, String toFragment) : void`

**Description** : switches the underlying fragment of the mainactivity
view to the fragment passed in, paramter 1 is the fragment and
toFragment is the string name of that fragment, which is used in order
to create an animation. we need to know the name of the fragment in
order to pick the right animation (left or right) so it matches the
location of the buttons on the bottom navigationalbar

### Use:

```java
this.openFragment(HomeFragment.newInstance(), "Home");
```

