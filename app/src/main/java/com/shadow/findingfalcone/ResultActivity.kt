package com.shadow.findingfalcone

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        intent?.let {
            if(it.getStringExtra("status")=="success"){
                tvStatus.text = getString(R.string.status_message)
            }
            else{
                tvStatus.text = getString(R.string.failure_status)
            }

            tvPlanet.text = (getString(R.string.planet_found_result) + it.getStringExtra("planetName"))
            tvTime.text = (getString(R.string.time_taken_result) + it.getStringExtra("timeTaken"))
        }
        btnStartAgain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("startAgain",true)
            startActivity(intent)
            finish()
        }
    }
}
