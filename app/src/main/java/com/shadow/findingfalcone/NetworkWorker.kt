package com.shadow.findingfalcone

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface FindFalconeAPI {
    @GET("planets")
    fun getPlanets(): Observable<ArrayList<Planet>>

    @GET("vehicles")
    fun getVehicles(): Observable<ArrayList<Vehicles>>

    @POST("token")
    @Headers("Accept:application/json")
    fun getToken(): Observable<RequestBody>

    @POST("find")
    @Headers("Accept:application/json", "Content-Type:application/json")
    fun findFalcone(@Body requestBody: RequestBody): Observable<ResponseBody>
}

private val baseUrl = "https://findfalcone.herokuapp.com/"

class FindFalconeWorker(val handler: FindFalconeHandler) {

    val retroInst = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build().create(FindFalconeAPI::class.java)
    var disposable = CompositeDisposable()
    fun getPlanets() {
        disposable.add(
            retroInst.getPlanets()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(handler::handlePlanetResponse, handler::errorResponse)
        )
    }

    fun getVehicles() {
        disposable.add(
            retroInst.getVehicles()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(handler::handleVehicleResponse, handler::errorResponse)
        )
    }

    fun getToken() {
        disposable.add(
            retroInst.getToken()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(handler::handleTokenResponse, handler::errorResponse)
        )
    }

    fun findFalcone(requestBody: RequestBody) {
        disposable.add(
            retroInst.findFalcone(requestBody)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(handler::handleResponse, handler::errorResponse)
        )
    }
}

interface FindFalconeHandler {
    fun handlePlanetResponse(result: ArrayList<Planet>)
    fun handleVehicleResponse(result: ArrayList<Vehicles>)
    fun handleTokenResponse(token: RequestBody)
    fun errorResponse(error: Throwable)
    fun handleResponse(responseBody: ResponseBody)
}