package com.shadow.findingfalcone

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatSpinner
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.ship_select_view.*


class MainActivity : AppCompatActivity(), FindFalconeHandler, ChoiceAdapter.PlanetChoiceHandler {
    companion object {
        val TAG: String by lazy {
            this::class.java.name
        }
    }

    private var planetList = ArrayList<Planet>()
    private var vehicleList = ArrayList<Vehicles>()
    private lateinit var planetAdapter: ChoiceAdapter
    private lateinit var currentplanet: Planet
    private lateinit var dialog: Dialog
    private val hashMap = HashMap<String, Vehicles>()

    private val btnReset: Button by lazy {
        dialog.btnCancel
    }

    private val btnSet: Button by lazy {
        dialog.btnBegin
    }

    private val spnShip: AppCompatSpinner by lazy {
        dialog.spnShip
    }
    private lateinit var worker: FindFalconeWorker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        worker = FindFalconeWorker(this)
        localIntent = intent
        if(intent.getBooleanExtra("reset", false)){
            reset()
        }
        dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        with(dialog) {
            this.requestWindowFeature(Window.FEATURE_NO_TITLE)
            this.setContentView(R.layout.ship_select_view)
            this.window?.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        worker.let {
            it.getPlanets()
            it.getVehicles()
        }

        btnReset.setOnClickListener {
            dialog.dismiss()
        }

        btnSet.setOnClickListener {
            val itemSelected = spnShip.selectedItem
            var speed = 0
            val planetName = currentplanet.name
            val timeTaken: Int = tvTimeTaken.text.toString().toInt()
            var oldTime = 0
            var newTime = 0
            vehicleList.filter {
                it.name == itemSelected
            }?.map { ship ->
                if (hashMap.containsKey(planetName)) {
                    if (hashMap[planetName]!!.name != ship.name) {
                        vehicleList.first { it == hashMap[planetName] }.apply {
                            this?.let {
                                it.totalNo++
                            }
                        }
                        val oldSpeed = hashMap[planetName]?.speed ?: 0
                        if (oldSpeed > 0) {
                            oldTime = currentplanet.distance / oldSpeed
                        }
                        ship.totalNo--
                        speed = ship.speed
                    }
                } else {
                    ship.totalNo--
                    speed = ship.speed
                }
                hashMap[planetName] = ship
            }.also {
                if (speed != 0) {
                    newTime = (currentplanet.distance / speed)
                    val itemIndex = planetList.indexOf(currentplanet)
                    currentplanet.planetSearching = true
                    currentplanet.vehicleName = itemSelected as String
                    planetAdapter.planetList[itemIndex] = currentplanet
                    if (hashMap.size == 4) {
                        btnFindFalcone.visibility = View.VISIBLE
                        planetAdapter.isSearchQuotaFull = true
                    } else {
                        btnFindFalcone.visibility = View.GONE
                    }
                    planetAdapter.notifyDataSetChanged()
                }
            }
            tvTimeTaken.text = (timeTaken + newTime - oldTime).toString()
            dialog.dismiss()
        }

        btnFindFalcone.setOnClickListener {
            findFalcone()
        }

        spnShip.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        dialog.setOnDismissListener {

        }
    }

    override fun handleVehicleResponse(result: ArrayList<Vehicles>) {
        vehicleList = result
    }

    override fun errorResponse(error: Throwable) {
        Log.d(TAG, error.message)
    }

    override fun handlePlanetResponse(result: ArrayList<Planet>) {
        planetList = result
        planetAdapter = ChoiceAdapter(this, planetList, this)
        lvChoices.adapter = planetAdapter
    }

    override fun selectedPlanet(planet: Planet) {
        currentplanet = planet
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, updateVehicleChoices())
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnShip.adapter = arrayAdapter
        dialog.show()
    }

    override fun deselectedPlanet(planet: Planet) {
        val chosenVehicle = hashMap[planet.name]
        chosenVehicle!!.totalNo++
        val i: Int = vehicleList.indexOf(chosenVehicle)
        if (i != -1) {
            vehicleList?.set(i, chosenVehicle)
        }
        val timeTaken = (planet.distance / chosenVehicle.speed)
        tvTimeTaken.text = (tvTimeTaken.text.toString().toInt() - timeTaken).toString()
        hashMap.remove(planet.name)
        btnFindFalcone.visibility = View.GONE
    }

    private fun updateVehicleChoices(): ArrayList<String> {
        val vehicleChoices = ArrayList<String>()

        vehicleChoices.add(getString(R.string.select_space_ship))
        vehicleList.forEach { ship ->
            if (currentplanet.distance <= ship.maxDist) {
                if (ship.totalNo > 0) {
                    vehicleChoices.add(ship.name)
                }
            }
        }
        return vehicleChoices
    }

    override fun findFalcone() {
        Log.d(TAG, "findFalcone")
        worker.getToken()
    }

    private var token = ""
    override fun handleTokenResponse(body: RequestBody) {
        this.token = body.token ?: ""
        Log.d(TAG, "token $token")
        val requestBody = RequestBody()
        requestBody.token = token
        Log.d(TAG, token)
        requestBody.planet_names = ArrayList<String>().apply {
            this.addAll(hashMap.keys)
        }
        requestBody.vehicle_names = ArrayList<String>().apply {
            hashMap.keys.forEach {
                hashMap[it]?.let {
                    this.add(it.name)
                }
            }
        }
        worker.findFalcone(requestBody)

    }
    private lateinit var localIntent: Intent
    override fun handleResponse(responseBody: ResponseBody) {
        localIntent =  Intent(this, ResultActivity::class.java)
        localIntent.putExtra("status", responseBody.status)
        localIntent.putExtra("planetName", responseBody.planet_name)
        localIntent.putExtra("timeTaken", tvTimeTaken.text.toString())
        startActivity(localIntent)
    }

    override fun sendNoMoreResAlert() {
        val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        } else {
            AlertDialog.Builder(this)
        }
        builder.setTitle("Resources Exhausted")
            .setMessage("Uncheck anyother item to search this planet")
            .setPositiveButton(android.R.string.yes) { dialog, which ->
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun reset() {
        localIntent.putExtra("reset", false)
        planetList.clear()
        vehicleList.clear()
        hashMap.clear()
        worker.disposable.dispose()
        currentplanet = Planet("", 0, false, "")
        tvTimeTaken.text = "0"
        finish()
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.reset -> {
                reset()
            }
            /*R.id.instruction -> {
                localIntent.putExtra("status","instruction")
                startActivity(localIntent)
            }*/
        }
        return true
    }
}
