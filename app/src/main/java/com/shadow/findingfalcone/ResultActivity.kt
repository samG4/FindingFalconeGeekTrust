package com.shadow.findingfalcone

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        intent?.let {
            if (it.getStringExtra("status") == "instruction") {
                webView.loadUrl("file:///android_asset/instructions.html")
                webView.visibility = View.VISIBLE
                tvStatus.visibility = View.GONE
                tvPlanet.visibility = View.GONE
                tvTime.visibility = View.GONE
            } else {
                webView.visibility = View.GONE
                tvStatus.visibility = View.VISIBLE
                tvPlanet.visibility = View.VISIBLE
                tvTime.visibility = View.VISIBLE
                if (it.getStringExtra("status") == "success") {
                    tvStatus.text = getString(R.string.status_message)
                } else {
                    tvStatus.text = getString(R.string.failure_status)
                }
                tvPlanet.text = (getString(R.string.planet_found_result) + it.getStringExtra("planetName"))
                tvTime.text = (getString(R.string.time_taken_result) + it.getStringExtra("timeTaken"))
            }

        }
        btnStartAgain.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("reset", true)
            startActivity(intent)
            finish()
        }
    }
}
