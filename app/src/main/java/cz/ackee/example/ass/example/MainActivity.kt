package cz.ackee.example.ass.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import cz.ackee.ass.Ass
import cz.ackee.ass.withValue
import cz.ackee.example.ass.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        Ass.setLocalParameters(this,
            "anonymous" withValue false,
            "accountName" withValue "pan.unicorn@ackee.cz",
            "userId" withValue 10
        )

        findViewById<Button>(R.id.btn_click).setOnClickListener {
            Ass.open(this)
        }
    }
}
