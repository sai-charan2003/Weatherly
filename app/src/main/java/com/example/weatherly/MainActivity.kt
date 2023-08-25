package com.example.weatherly

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.LinearLayout
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView

import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide
import com.example.weatherly.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    data class HourlyForecast(
        val time:String,

        val tempCelsius: Double,
        val tempFahrenheit: Double,
        val conditionText: String,
        val conditionIcon: String
    )
    private val hourlyForecasts: MutableList<HourlyForecast> = mutableListOf()
       private var long:Double?=null
    private var lati:Double?=null


    var temperature:Int = 0
    private val launchSecondActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            val returnValue = data?.getStringExtra("RETURN_VALUE_KEY")
            Log.d("TAG", "$returnValue: ")


                Log.d("TAG", "true")
            if (returnValue != null) {

//

                getweatherdatacity(returnValue)
            }

            // Handle the returnValue from the second activity
        }
    }
    var location:String="0"
    var temperature_f:Int = 0
    var feelslike:Int = 0
    var feelslike_f:Int = 0
    var corf:Boolean=false







    override fun onCreate(savedInstanceState: Bundle?) {



        val sharedPref = getSharedPreferences("isChecked", MODE_PRIVATE)



        val editor =sharedPref.edit()
        editor.clear().apply()
        binding.search.setOnClickListener {
            val intent=Intent(this, search::class.java)

            launchSecondActivity.launch(intent)
        }

        binding.setting.setOnClickListener{
            val view: View=layoutInflater.inflate(R.layout.bottomsheet,null)
            val dialog=BottomSheetDialog(this)
            val change=view.findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.convert)

            change.setChecked(sharedPref.getBoolean("isChecked",false))



            change.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked){
                    editor.putBoolean("isChecked",true)
                    editor.putBoolean("corf",true)
                    editor.apply()
                    change.setChecked(isChecked)

                    changetof()

                }
                else{
                    editor.putBoolean("isChecked",false)
                    editor.putBoolean("corf",false)
                    editor.apply()
                    change.setChecked(isChecked)
                    changetoc()

                }



            }
            dialog.setContentView(view)
            dialog.show()

        }



        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)




        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.getting.visibility=View.VISIBLE
        binding.mainactivity1.visibility=View.GONE

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient!!.lastLocation.addOnSuccessListener { location->
                lati=location.latitude
                long=location.longitude
                getweatherdata(long!!, lati!!)
            }
        }
        else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            if(ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient!!.lastLocation.addOnSuccessListener { location ->
                    lati = location.latitude
                    long = location.longitude
                    getweatherdata(long!!, lati!!)
                }
            }


        }







    }
    private fun getweatherdata(long:Double,lat:Double) {
        hourlyForecasts.clear()
        

        binding.getting.visibility= View.VISIBLE






            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.weatherapi.com/v1/")

                .build().create(apiinterface::class.java)
            Log.d("TAG", "getweatherdata: $long")
            var response = retrofit.getWeatherData(loc = "$lat,$long",
                key = "ff200d03083c4e7891263736231801"
            )
            response.enqueue(object : Callback<weatherdata> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<weatherdata>, response: Response<weatherdata>) {
                    val responsebody = response.body()
                    Log.d("TAG", "onResponse: $responsebody")
                    if (responsebody != null) {
                        val forecastHour=responsebody.forecast.forecastday[0].hour
                        val currentEpochTime = System.currentTimeMillis() / 1000
                        Log.d("hour", "onResponse: $forecastHour")
                        for(i in 0..23){
                            val now=responsebody.forecast.forecastday[0].hour[i]
                            val current=responsebody.forecast.forecastday[0].hour[i].time_epoch
                            if(current>currentEpochTime){
                                hourlyForecasts.add(
                                    HourlyForecast(
                                        now.time,
                                        now.temp_c,
                                        now.temp_f,
                                        now.condition.text,
                                        now.condition.icon


                                    )
                                )
                            }

                        }
                        Log.d("TAG", "onResponse: $hourlyForecasts")
                        val containerLayout = findViewById<LinearLayout>(R.id.containerLayout)
                        for(i in 0 until hourlyForecasts.size){
                            val cardViewLayout = LayoutInflater.from(this@MainActivity).inflate(R.layout.card_layout, containerLayout, false) as MaterialCardView
                            val time = cardViewLayout.findViewById<TextView>(R.id.time)
                            val temp=cardViewLayout.findViewById<TextView>(R.id.temp)
                            val weathertype=cardViewLayout.findViewById<TextView>(R.id.weather_type)
                            val image=cardViewLayout.findViewById<ImageView>(R.id.icon)

                            var tempint=hourlyForecasts[i].tempCelsius.toInt().toString()


                            time.text=hourlyForecasts[i].time.substring(hourlyForecasts[0].time.length - 5)
                            temp.text= "$tempint°C"
                            val imagedu:String=hourlyForecasts[i].conditionIcon
                            Glide.with(this@MainActivity).load("https:$imagedu").into(image)
                            weathertype.text=hourlyForecasts[i].conditionText


                            binding.containerLayout.addView(cardViewLayout)


                        }

                        Log.d("TAG", "onResponse: $hourlyForecasts")
                        val lastFiveDigits = hourlyForecasts[0].time.substring(hourlyForecasts[0].time.length - 5)
                        Log.d("TAG", "onResponse: $lastFiveDigits")
                        Log.d("TAG", "onResponse: ")





                        temperature= responsebody.current.temp_c.toInt()
                        var city=responsebody.location.name
                        var type=responsebody.current.condition.text
                         feelslike= responsebody.current.feelslike_c.toInt()
                        var icon=responsebody.current.condition.icon
                        temperature_f= responsebody.current.temp_f.toInt()
                        feelslike_f=responsebody.current.feelslike_f.toInt()
                        binding.windspeed.text="${responsebody.current.wind_kph}Km/h"
                        binding.humudity.text="${responsebody.current.humidity}%"
                        binding.sunrise.text="${responsebody.forecast.forecastday[0].astro.sunrise}"
                        binding.sunset.text="${responsebody.forecast.forecastday[0].astro.sunset}"


                        binding.temp.text="$temperature°C"

                        binding.cityName.text="$city"
                        binding.feels.text="$feelslike°C"
                        binding.weatherType.text="$type"

                        Glide.with(this@MainActivity).load("https:$icon").into(binding.weatherimage)
                        binding.getting.visibility=View.INVISIBLE
                        binding.mainactivity1.visibility=View.VISIBLE
                    }

                }

                override fun onFailure(call: Call<weatherdata>, t: Throwable) {

                }


            })
    }
    private fun changetoc(){

            binding.temp.text="$temperature°C"
            binding.feels.text="$feelslike°C"

    }
    private fun changetof(){

        binding.temp.text="$temperature_f°F"
        binding.feels.text="$feelslike_f°F"

    }

private fun getweatherdatacity(city:String) {
    var i:Int=0
    hourlyForecasts.clear()
    binding.containerLayout.removeAllViews()
    Log.d("TAG", "getweatherdatacity: ${hourlyForecasts.isEmpty()}")
  binding.getting.visibility= View.VISIBLE
    binding.mainactivity1.visibility=View.GONE
    val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.weatherapi.com/v1/")
        .build().create(apiinterface::class.java)
    Log.d("TAG", "getweatherdata: $long")
    var response = retrofit.getWeatherData(loc = "$city", key = "ff200d03083c4e7891263736231801")
    response.enqueue(object : Callback<weatherdata> {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onResponse(call: Call<weatherdata>, response: Response<weatherdata>) {
            val responsebody = response.body()
            Log.d("TAG", "onResponse: $responsebody")
            if (responsebody != null) {

                val forecastHour=responsebody.forecast.forecastday[0].hour
                val currentEpochTime = System.currentTimeMillis() / 1000
                Log.d("hour", "onResponse: $forecastHour")
                for(i in 0..23){
                    val now=responsebody.forecast.forecastday[0].hour[i]
                    val current=responsebody.forecast.forecastday[0].hour[i].time_epoch
                    if(current>currentEpochTime){

                        hourlyForecasts.add(
                            HourlyForecast(
                                now.time,
                                now.temp_c,
                                now.temp_f,
                                now.condition.text,
                                now.condition.icon
                            )
                        )
                    }

                }
                Log.d("TAG", "onResponse: $hourlyForecasts")
                val containerLayout = findViewById<LinearLayout>(R.id.containerLayout)
                for(i in 0 until hourlyForecasts.size){
                    val cardViewLayout = LayoutInflater.from(this@MainActivity).inflate(R.layout.card_layout, containerLayout, false) as MaterialCardView
                    val time = cardViewLayout.findViewById<TextView>(R.id.time)
                    val temp=cardViewLayout.findViewById<TextView>(R.id.temp)
                    val weathertype=cardViewLayout.findViewById<TextView>(R.id.weather_type)
                    val image=cardViewLayout.findViewById<ImageView>(R.id.icon)
                    var tempint=hourlyForecasts[i].tempCelsius.toInt().toString()

                    time.text=hourlyForecasts[i].time.substring(hourlyForecasts[0].time.length - 5)
                    temp.text= "$tempint°C"
                    val imagedu:String=hourlyForecasts[i].conditionIcon
                    Glide.with(this@MainActivity).load("https:$imagedu").into(image)
                    weathertype.text=hourlyForecasts[i].conditionText


                    binding.containerLayout.addView(cardViewLayout)

                }

                Log.d("TAG", "onResponse: $hourlyForecasts")
                val lastFiveDigits = hourlyForecasts[0].time.substring(hourlyForecasts[0].time.length - 5)
                Log.d("TAG", "onResponse: $lastFiveDigits")
                Log.d("TAG", "onResponse: ")





                temperature= responsebody.current.temp_c.toInt()
                var city=responsebody.location.name
                var type=responsebody.current.condition.text
                feelslike= responsebody.current.feelslike_c.toInt()
                var icon=responsebody.current.condition.icon
                temperature_f= responsebody.current.temp_f.toInt()
                feelslike_f=responsebody.current.feelslike_f.toInt()
                binding.windspeed.text="${responsebody.current.wind_kph}Km/h"
                binding.humudity.text="${responsebody.current.humidity}%"
                binding.sunrise.text="${responsebody.forecast.forecastday[0].astro.sunrise}"
                binding.sunset.text="${responsebody.forecast.forecastday[0].astro.sunset}"


                binding.temp.text="$temperature°C"

                binding.cityName.text="$city"
                binding.feels.text="$feelslike°C"
                binding.weatherType.text="$type"

                Glide.with(this@MainActivity).load("https:$icon").into(binding.weatherimage)
                binding.getting.visibility=View.INVISIBLE
                binding.mainactivity1.visibility=View.VISIBLE
            }

        }

        override fun onFailure(call: Call<weatherdata>, t: Throwable) {

        }


    })





}


}