package com.example.onesky

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.onesky.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private final var FINE_PERMISSION_CODE = 1;
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: String
    private lateinit var cityName: String


    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()


        searchCity()
        fetchWeatherData("Jaipur");

        //Log.d("TAG", "onCreate: $cityName")


    }

    private fun getLiveCityName(lat: Double, long: Double){
        val geocoder = Geocoder(this, Locale.getDefault())
        val address = geocoder.getFromLocation(lat, long, 1)

        cityName = address?.get(0)?.adminArea.toString()
        return

    }

    private fun getLastLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_PERMISSION_CODE)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            if(location != null) {
                currentLocation = location.toString()
                var latitude = location.latitude.toString()
                Log.d("TAG1", "onCreate: $latitude")
                getLiveCityName(location.latitude, location.longitude)
            }
        }
        return
    }

    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })


    }

    private fun fetchWeatherData(cityname: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityname, "29b84d4e8e4d3eeff9259d02dbda840c", "metric")
        response.enqueue(object : Callback<WeatherApp>{
            @SuppressLint("SetTextI18n")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful){
                    val temperature = responseBody?.main?.temp.toString()
                    val humidity = responseBody?.main?.humidity.toString()
                    val windSpeed = responseBody?.wind?.speed.toString()
                    val seaLevel = responseBody?.main?.sea_level.toString()
                    val weather = responseBody?.weather?.firstOrNull()?.main?: "unknown"


                    val timePoint = LocalDateTime.now()
                    val month = timePoint.month.toString()
                    val date= timePoint.dayOfMonth.toString()
                    val day = timePoint.dayOfWeek.toString()

                    binding.temp.text = "$temperature °c"
                    binding.humidity.text = "$humidity%"
                    binding.feelsLike.text = weather
                    binding.windSpeed.text = "$windSpeed km/h"
                    binding.seaLevel.text = "$seaLevel hPa"
                    binding.city.text = cityname
                    binding.dayDate.text = "$day, $date $month"
                    binding.weather.text = weather

                    changesWithWeather(weather)

                }
            }

            override fun onFailure(call: Call<WeatherApp>, response: Throwable) {
                TODO("Not yet implemented")
            }

        })



    }

    private fun changesWithWeather(weather: String) {
        when (weather){
            "Clear Sky", "Sunny", "Clear" ->{
                binding.root.setBackgroundResource(R.drawable.sunny_bck_final)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
                binding.humidity.setTextColor(getColor(R.color.sunny))

                //setting colours acc to weather
                binding.windSpeed.setTextColor(getColor(R.color.sunny))
                binding.seaLevel.setTextColor(getColor(R.color.sunny))
                binding.feelsLike.setTextColor(getColor(R.color.sunny))

                //setting img acc to weather
                binding.imageHumidity.setImageResource(R.drawable.humidity_sunny)
                binding.imageWind.setImageResource(R.drawable.wind_sunny)
                binding.imageSea.setImageResource(R.drawable.sea_sunny)
                binding.imageFeels.setImageResource(R.drawable.feels_sunny)


            }
//            "Haze" ->{
//                binding.root.setBackgroundResource(R.drawable.cloudy_bck)
//                binding.lottieAnimationView.setAnimation(R.raw.cloud)
//            }
            "Clouds", "Partly Clouds", "Overcast", "Mist", "Foggy", "Haze" ->{
                binding.root.setBackgroundResource(R.drawable.cloudy_bck)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)

                //setting colours acc to weather
                binding.humidity.setTextColor(getColor(R.color.cloudy))
                binding.windSpeed.setTextColor(getColor(R.color.cloudy))
                binding.seaLevel.setTextColor(getColor(R.color.cloudy))
                binding.feelsLike.setTextColor(getColor(R.color.cloudy))

                //setting img acc to weather
                binding.imageHumidity.setImageResource(R.drawable.humidity_cloud)
                binding.imageWind.setImageResource(R.drawable.wind_cloud)
                binding.imageSea.setImageResource(R.drawable.sea_cloud)
                binding.imageFeels.setImageResource(R.drawable.feels_cloud)

            }
            "Rain", "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" ->{
                binding.root.setBackgroundResource(R.drawable.rainy_bck)
                binding.lottieAnimationView.setAnimation(R.raw.rain)

                //setting colours acc to weather
                binding.humidity.setTextColor(getColor(R.color.rainy))
                binding.windSpeed.setTextColor(getColor(R.color.rainy))
                binding.seaLevel.setTextColor(getColor(R.color.rainy))
                binding.feelsLike.setTextColor(getColor(R.color.rainy))

                //setting img acc to weather
                binding.imageHumidity.setImageResource(R.drawable.humidity_rain)
                binding.imageWind.setImageResource(R.drawable.wind_rain)
                binding.imageSea.setImageResource(R.drawable.sea_rain)
                binding.imageFeels.setImageResource(R.drawable.feels_rain)

            }
            "Light Snow", "Heavy Snow", "Moderate Snow", "Blizzard" ->{
                binding.root.setBackgroundResource(R.drawable.snowy_bkc)
                binding.lottieAnimationView.setAnimation(R.raw.snow)

                //setting colours acc to weather
                binding.humidity.setTextColor(getColor(R.color.snowy))
                binding.windSpeed.setTextColor(getColor(R.color.snowy))
                binding.seaLevel.setTextColor(getColor(R.color.snowy))
                binding.feelsLike.setTextColor(getColor(R.color.snowy))

                //setting img acc to weather
                binding.imageHumidity.setImageResource(R.drawable.humidity_snow)
                binding.imageWind.setImageResource(R.drawable.wind_snow)
                binding.imageSea.setImageResource(R.drawable.sea_snow)
                binding.imageFeels.setImageResource(R.drawable.feels_snow)


            }
        }

    }
}

