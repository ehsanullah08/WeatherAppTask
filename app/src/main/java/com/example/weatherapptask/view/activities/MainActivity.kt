package com.example.weatherapptask.view.activities


import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.weatherapptask.R
import com.example.weatherapptask.databinding.ActivityMainBinding
import com.example.weatherapptask.viewModels.WeatherInfoViewModel
import com.example.weatherapptask.viewModels.WeatherInfoViewModelFactory
import com.google.android.gms.location.LocationServices
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class MainActivity : AppCompatActivity(), KodeinAware {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override val kodein by kodein()
    private val factory: WeatherInfoViewModelFactory by instance()

    private lateinit var viewModel: WeatherInfoViewModel
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializations()
        checkLocationPermission()
    }

    private fun initializations() {
        viewModel = ViewModelProvider(this, factory)[WeatherInfoViewModel::class.java]

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        setLiveDataListeners()
        setupSearchView()

        binding.swipeRefreshLayout.setOnRefreshListener {
            getCurrentLocation()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission has been granted, you can access the location
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can access the location
                // Do whatever you need with the location here

                getCurrentLocation()
            } else {
                // Permission denied, handle accordingly
                binding.outputGroup.visibility = View.GONE
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.tvErrorMessage.text =
                    getString(R.string.please_give_location_permission_to_move_forward)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener(
                this
            ) { location ->
                if (location != null) {
                    val latitude: Double = location.latitude
                    val longitude: Double = location.longitude

                    viewModel.getWeatherInfo(latitude, longitude) // fetch weather info
                } else {
                    binding.outputGroup.visibility = View.GONE
                    binding.tvErrorMessage.visibility = View.VISIBLE
                    binding.tvErrorMessage.text =
                        getString(R.string.failed_to_get_location)
                }
            }
            .addOnFailureListener(this) {
                binding.outputGroup.visibility = View.GONE
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.tvErrorMessage.text =
                    getString(R.string.failed_to_get_location)
            }
    }

    private fun setupSearchView() {
        var searchQuery = ""

        /* Enable focus upon clicking anywhere on searchView */
        binding.layoutInput.searchView.setOnClickListener {
            binding.layoutInput.searchView.isIconified = false
            binding.layoutInput.searchView.requestFocus()
        }

        binding.layoutInput.ivSearchIcon.setOnClickListener {
            binding.layoutInput.searchView.visibility = View.VISIBLE
            binding.layoutInput.ivDoSearch.visibility = View.VISIBLE
            binding.layoutInput.ivSearchIcon.visibility = View.INVISIBLE
            binding.layoutInput.searchView.requestFocus()
        }

        binding.layoutInput.ivDoSearch.setOnClickListener {
            binding.layoutInput.searchView.visibility = View.INVISIBLE
            binding.layoutInput.ivDoSearch.visibility = View.INVISIBLE
            binding.layoutInput.ivSearchIcon.visibility = View.VISIBLE
            binding.layoutInput.searchView.clearFocus()
            binding.layoutInput.searchView.setQuery("", false)

            if (searchQuery.isNotBlank())
                viewModel.getWeatherInfo(searchQuery) // fetch weather info
        }

        binding.layoutInput.searchView.setOnCloseListener {
            binding.layoutInput.searchView.visibility = View.INVISIBLE
            binding.layoutInput.ivDoSearch.visibility = View.INVISIBLE
            binding.layoutInput.ivSearchIcon.visibility = View.VISIBLE
            true
        }

        binding.layoutInput.searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                /* Perform search when user clicks the search button in soft keyboard*/
                binding.layoutInput.ivDoSearch.performClick()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()) {
                    searchQuery = newText
                } else {
                    binding.layoutInput.searchView.visibility = View.INVISIBLE
                    binding.layoutInput.ivDoSearch.visibility = View.INVISIBLE
                    binding.layoutInput.ivSearchIcon.visibility = View.VISIBLE
                }
                return true
            }
        })
    }

    private fun setLiveDataListeners() {

        /**
         * ProgressBar visibility will be handled by this LiveData. ViewModel decides when Activity
         * should show ProgressBar and when hide.
         */
        viewModel.progressBarLiveData.observe(this) { isShowLoader ->
            if (isShowLoader)
                binding.progressBar.visibility = View.VISIBLE
            else
                binding.progressBar.visibility = View.GONE
        }

        /**
         * This method will be triggered when ViewModel successfully receive WeatherData from our
         * data source.
         */
        viewModel.weatherInfoLiveData.observe(this) { weatherData ->
            binding.layoutWeatherBasic.weatherInfo = weatherData
            binding.layoutWeatherAdditional.weatherInfo = weatherData
            binding.layoutSunsetSunrise.weatherInfo = weatherData

            binding.outputGroup.visibility = View.VISIBLE
            binding.tvErrorMessage.visibility = View.GONE

            binding.swipeRefreshLayout.isRefreshing = false
        }

        /**
         * If ViewModel faces any error during Weather Info fetching API call by Model, then PUSH the
         * error message into `weatherInfoFailureLiveData`.
         */
        viewModel.weatherInfoFailureLiveData.observe(this) { errorMessage ->
            binding.outputGroup.visibility = View.GONE
            binding.tvErrorMessage.visibility = View.VISIBLE
            binding.tvErrorMessage.text = errorMessage
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}