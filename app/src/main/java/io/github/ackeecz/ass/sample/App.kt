package io.github.ackeecz.ass.sample

import android.app.Application
import io.github.ackeecz.ass.Ass
import io.github.ackeecz.ass.withValue

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // TODO: replace with your own url and token
        Ass.initialize(this, "https://your.project.firebaseapp.com", "authtoken")
        Ass.setShakeSensitivity(Ass.Sensitivity.Light)

        Ass.setGlobalParameters(
            "flavor" withValue "yourFlavor"
        )
        Ass.addGlobalParameters("appOpenedCount" withValue 42)
    }
}
