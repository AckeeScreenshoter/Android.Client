# Ass (Android screenshot system)
We often found our testers submitting screenshots with details about the issue, device specific
information and user id to our bug tracking system. We wanted to automate this activity in a way
that would be minimally intrusive for our applications and as helpful as possible for our testers.

During weekend hackathon session we created this library that allows testers to shake the device
to take screenshot, add description of the problem, and send all device/app specific information
to our servers.

## Setup
You have to initialize the library first:

```
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Ass.initialize(this, "https://your.project.firebaseapp.com", "authtoken")
    }
}
```

The `initialize` method requires 3 arguments:
 - `Application` reference (because it hooks into its `ActivityLifecycleCallbacks`),
 - `url` of the server where requests should be sent,
 - `authToken` that is sent as part of Authorization header.

That is enough to set up the library.

## Usage
If you initialized the library and shake the device while the application is in a resumed state,
a screenshot of currently resumed Activity is taken and sent via intent to `FeedbackActivity`.

`FeedbackActivity` automatically collects device specific information such as device name,
OS version and static application information such as app name, version and package. All these
details together with the screenshot are uploaded to a server when you hit send button.

### Extra parameters
If you want to send extra information you can define **global parameters** that are uploaded with
each request or **local parameters** that are scoped to a specific Activity and uploaded only if
shaken with that Activity active.

```
Ass.setGlobalParameters(
    "flavor" withValue "premium"
)

Ass.setLocalParameters(this,
    "anonymous" withValue false,
    "accountName" withValue "pan.unicorn@ackee.cz",
    "userId" withValue 10
)
```

You might want to set user's id and username as global parameters once he is logged in or just
scope it to profile screen if that makes more sense.

### Disabled activities
Sometimes you might not want to enable shake gesture for a specific Activity. For that you can
use `addDisabledActivities`:
```
Ass.addDisabledActivities(
    LoginActivity::class.java
)
```

### Shake sensitivity
Depending on the kind of application you develop, you might want to increase or reduce
sensitivity of the shake gesture. For that you can use `setShakeSensitivity`:
```
Ass.setShakeSensitivity(Ass.Sensitivity.Light)  // makes it easier to activate
Ass.setShakeSensitivity(Ass.Sensitivity.Medium) // default
Ass.setShakeSensitivity(Ass.Sensitivity.Hard)   // makes it harder to activate
```
