package com.shadow.findingfalcone

import com.google.gson.annotations.SerializedName

data class Planet(
    var name: String,
    var distance: Int,
    var planetSearching: Boolean,
    var vehicleName: String?
) {
    override fun equals(other: Any?): Boolean {
        return this.name == (other as Planet).name
    }
}

data class Vehicles(
    var name: String,
    @SerializedName("total_no")
    var totalNo: Int,
    @SerializedName("max_distance")
    var maxDist: Int,
    var speed: Int
) {
    override fun equals(other: Any?): Boolean {
        return this.name == (other as Vehicles).name
    }
}

data class RequestBody(
    var token: String?=null,
    var planet_names: ArrayList<String>?=null,
    var vehicle_names: ArrayList<String>?=null
)

data class ResponseBody(
    var planet_name: String,
    var status: String,
    var error: String
)
