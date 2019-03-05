package com.shadow.findingfalcone

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.planet_item.view.*

class ChoiceAdapter(
    private val ctx: Context,
    val planetList: ArrayList<Planet>,
    private val planetChoiceHandler: PlanetChoiceHandler
) : BaseAdapter() {

    private val imgResourceList = arrayListOf<Int>(
        R.drawable.p1,
        R.drawable.p2,
        R.drawable.p3,
        R.drawable.p4,
        R.drawable.p5,
        R.drawable.p6
    )

    var isSearchQuotaFull = false
    private val inflater: LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItem(p0: Int): Any = planetList[p0]

    override fun getItemId(p0: Int): Long = p0.toLong()

    override fun getCount(): Int = planetList.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView: View
        val holder: PlanetViewHolder
        if (convertView == null) {
            listItemView = inflater.inflate(R.layout.planet_item, parent, false)
            holder = PlanetViewHolder(listItemView)
            listItemView.tag = holder
        } else {
            listItemView = convertView
            holder = convertView.tag as PlanetViewHolder
        }

        val currentPlanet = planetList[position]
        val imgPlanet = holder.imgPlanet
        val tvPlanetName = holder.tvPlanetName
        val tvDistance = holder.tvDistance
        val btnShip = holder.btnShip
        val imgSelected = listItemView.imgCheck

        imgPlanet.setImageResource(imgResourceList[position])
        tvPlanetName.text = currentPlanet.name
        tvDistance.text = (currentPlanet.distance.toString() + ctx.getString(R.string.miles))
        imgSelected.visibility = if (currentPlanet.planetSearching) View.VISIBLE else View.INVISIBLE

        btnShip.text =
                if (!currentPlanet.vehicleName.isNullOrEmpty()) currentPlanet.vehicleName else ctx.getString(R.string.select_space_ship)
        listItemView.setOnClickListener {
            if(!isSearchQuotaFull){
                planetChoiceHandler.selectedPlanet(currentPlanet)
            }else{
                planetChoiceHandler.sendNoMoreResAlert()
            }
        }

        imgSelected.setOnClickListener {
            imgSelected.visibility = View.GONE
            planetChoiceHandler.deselectedPlanet(currentPlanet)
            currentPlanet.planetSearching = false
            isSearchQuotaFull = false
            currentPlanet.vehicleName=""
            notifyDataSetChanged()
        }

        return listItemView
    }

    private class PlanetViewHolder(val view: View) {
        val imgPlanet: ImageView = view.imgPlanet
        val tvPlanetName: TextView = view.tvPlanetName
        val tvDistance: TextView = view.tvDistance
        val btnShip: Button = view.btnSpaceShip
    }

    interface PlanetChoiceHandler {
        fun selectedPlanet(planet: Planet)
        fun deselectedPlanet(planet: Planet)
        fun findFalcone()
        fun sendNoMoreResAlert()
    }
}