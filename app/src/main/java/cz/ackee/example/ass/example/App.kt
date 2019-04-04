package cz.ackee.example.ass.example

import android.app.Application
import cz.ackee.ass.Ass
import cz.ackee.ass.BuildConfig
import cz.ackee.ass.withValue

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // TODO: replace with your own url and token
        Ass.initialize(this, "https://your.project.firebaseapp.com", "authtoken")
        Ass.setShakeSensitivity(Ass.Sensitivity.Light)

        if (BuildConfig.FLAVOR == "premium") {
            Ass.setGlobalParameters(
                "flavor" withValue "premium"
            )
        }
        Ass.addGlobalParameters("appOpenedCount" withValue 42)
    }
}
