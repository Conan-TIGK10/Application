# SessionFragment

The SessionFragment handles the calls from user to the API regarding the creation of a new session. It provides a GUI interface that starts when the SessionFragment is called. It handles errors from the API and displays them if something went wrong.

## `onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState): void`

**Description** : Overrided function that adds functionality to the GUI. This adds a click listener which will use the input data and then call the createSession function from the WifiHandler. If it passes without any errors, the session will be created and the fragment will close, otherwise the error from the API call will be shown in the text message for the session errors. When there is an error, the fragment will not close and it will be usable again for new input.

## `closeFragment(): void`

**Description** : Function which will close the fragment and return to the previous fragment in the stack.

### Use:

```java
closeFragment();
```
