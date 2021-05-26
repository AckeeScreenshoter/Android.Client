package io.github.ackeecz.ass.sample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import io.github.ackeecz.ass.Ass
import io.github.ackeecz.ass.withValue

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
