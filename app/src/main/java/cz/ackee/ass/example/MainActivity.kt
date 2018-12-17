package cz.ackee.ass.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cz.ackee.ass.Ass
import cz.ackee.ass.withValue
import cz.ackee.ass.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        Ass.setLocalParameters(this,
            "anonymous" withValue false,
            "accountName" withValue "pan.unicorn@ackee.cz",
            "userId" withValue 10
        )
    }
}
