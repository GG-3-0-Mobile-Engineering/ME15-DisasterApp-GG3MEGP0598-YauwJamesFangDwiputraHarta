package com.james.disasterapp

import android.app.SearchManager
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.james.disasterapp.databinding.ActivityMainBinding
import com.james.disasterapp.databinding.BottomSheetBinding
import com.james.disasterapp.model.Properties
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBinding: BottomSheetBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var disasterAdapter: DisasterAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var isSearchEmpty: MutableLiveData<Boolean> = MutableLiveData(true)
    private var isFilterEmpty: MutableLiveData<Boolean> = MutableLiveData(true)
    private var querySearch: MutableLiveData<String> = MutableLiveData("")
    private var queryFilter = MutableLiveData("")
    private val markersList: MutableList<Marker> = mutableListOf()


    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == FilterActivity.RESULT_CODE && result.data != null) {
            val selectedValue =
                result.data?.getStringExtra(FilterActivity.EXTRA_SELECTED_VALUE)
            queryFilter.value = selectedValue
            isFilterEmpty.value = false
            binding.searchView.setQuery("All", false)
            binding.tvFilter.text = "$selectedValue x"
            binding.tvFilter.visibility = View.VISIBLE
            listDisaster(true, false, queryFilter = selectedValue)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bottomSheetBinding = BottomSheetBinding.inflate(layoutInflater)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mainViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        )[MainViewModel::class.java]

        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        preferences.getString(
            getString(R.string.pref_key_dark),
            getString(R.string.pref_dark_auto)
        )?.apply {
            val mode = NightMode.valueOf(this.uppercase(Locale.US))
            AppCompatDelegate.setDefaultNightMode(mode.value)
        }

        binding.fabSetting.setOnClickListener {
            val intentToSetting = Intent(this, SettingActivity::class.java)
            intentToSetting.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intentToSetting)
            binding.searchView.setQuery(null, false)
        }


        binding.fabFilter.setOnClickListener {
            val moveForResultIntent = Intent(this@MainActivity, FilterActivity::class.java)
            resultLauncher.launch(moveForResultIntent)
        }

        if (isSearchEmpty.value == true && isFilterEmpty.value == true) {
            listDisaster(true, true)
        }

        searchView()

    }

    private fun searchView() {
        val from = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
        val to = intArrayOf(R.id.searchItemID)
        val cursorAdapter = SimpleCursorAdapter(
            this,
            R.layout.suggestion_item_layout, //layout item suggestion
            null,
            from,
            to,
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )

        binding.searchView.suggestionsAdapter = cursorAdapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    listDisaster(true, true)
                    isSearchEmpty.value = true
                }
                binding.tvFilter.visibility = View.GONE

                val cursor =
                    MatrixCursor(
                        arrayOf(
                            BaseColumns._ID,
                            SearchManager.SUGGEST_COLUMN_TEXT_1,
                            SearchManager.SUGGEST_COLUMN_TEXT_2
                        )
                    )
                newText?.let {

                    AdminArea.suggestions.forEachIndexed { index, suggestion ->
                        if (suggestion.first.contains(newText, true))
                            cursor.addRow(arrayOf(index, suggestion.first, suggestion.second))
                    }
                }

                cursorAdapter.changeCursor(cursor)
                return true
            }

            override fun onQueryTextSubmit(newQuery: String?): Boolean {
                val matchingPair =
                    AdminArea.suggestions.find { it.first.lowercase() == newQuery?.lowercase() }
                val idArea = matchingPair?.second


                querySearch.value = idArea?:""

                listDisaster(false, true, querySearch = querySearch.value)

                isSearchEmpty.value = false
                return true
            }
        })

        binding.searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionClick(position: Int): Boolean {
                val cursor = binding.searchView.suggestionsAdapter.getItem(position) as Cursor
                val selection =
                    cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)) //it works, ignore error
                val intentData =
                    cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2)) //it works, ignore error

                binding.searchView.setQuery(selection, false)

                querySearch.value = intentData
                listDisaster(false, true, querySearch = querySearch.value)

                isSearchEmpty.value = false

                return true

            }

            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(MainViewModel::class.java)

        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        isSearchEmpty.observe(this) { isSearchEmpty ->

                if (isSearchEmpty == true ) {
                    mMap.clear()
                    mainViewModel.getDisaster().observe(this) {
                        if (it != null) {
                            when (it) {
                                is ResultCustom.Loading -> binding.loadingMarker.visibility = View.VISIBLE

                                is ResultCustom.Success -> {
                                    if (it.data!!.isNotEmpty()) {
                                        binding.loadingMarker.visibility = View.GONE
                                        mMap.clear()

                                        val boundsBuilder = LatLngBounds.Builder()
                                        it.data.forEach { disaster ->
                                            val latLng =
                                                LatLng(
                                                    disaster!!.coordinates[1],
                                                    disaster.coordinates[0]
                                                )
                                            mMap.addMarker(
                                                MarkerOptions()
                                                    .position(latLng)
                                                    .title("${disaster.properties?.disasterType}")
                                                    .snippet("${disaster.properties?.text}")
                                            )
                                            boundsBuilder.include(latLng)
                                        }
                                        val bounds: LatLngBounds = boundsBuilder.build()
                                        mMap.animateCamera(
                                            CameraUpdateFactory.newLatLngBounds(
                                                bounds,
                                                resources.displayMetrics.widthPixels,
                                                resources.displayMetrics.heightPixels,
                                                300
                                            )
                                        )
                                    }

                                }
                                is ResultCustom.Error -> {
                                    binding.loadingMarker.visibility = View.GONE
                                    mMap.clear()
                                }

                            }
                        }

                    }
                } else if (isSearchEmpty == false) {

                    mMap.clear()
                    GlobalScope.launch {
                        delay(2000L)
                    }

                    val newQuery: String? = querySearch.value

                    mainViewModel.getSearchingDisaster(newQuery!!).observe(this@MainActivity) {
                        if (it != null) {
                            when (it) {
                                is ResultCustom.Loading -> binding.loadingMarker.visibility = View.VISIBLE

                                is ResultCustom.Success -> {
                                    binding.loadingMarker.visibility = View.GONE
                                    if (it.data!!.isNotEmpty()) {
                                        mMap.clear()

                                        val boundsBuilder = LatLngBounds.Builder()
                                        it.data.forEach { disaster ->
                                            val latLng =
                                                LatLng(
                                                    disaster!!.coordinates[1],
                                                    disaster.coordinates[0]
                                                )
                                            val marker = mMap.addMarker(
                                                MarkerOptions()
                                                    .position(latLng)
                                                    .title("${disaster.properties?.disasterType}")
                                                    .snippet("${disaster.properties?.text}")
                                            )
                                            boundsBuilder.include(latLng)
                                            markersList.add(marker!!)
                                        }
                                        val bounds: LatLngBounds = boundsBuilder.build()
                                        mMap.animateCamera(
                                            CameraUpdateFactory.newLatLngBounds(
                                                bounds,
                                                resources.displayMetrics.widthPixels,
                                                resources.displayMetrics.heightPixels,
                                                100
                                            )

                                        )
                                    }
                                }
                                is ResultCustom.Error -> {
                                    binding.loadingMarker.visibility = View.GONE
                                    mMap.clear()
                                }

                            }
                        }
                    }

                }
        }

        isFilterEmpty.observe(this){ isFilterEmpty ->
            if (isFilterEmpty == false){
                mMap.clear()
                val newQuery: String? = queryFilter.value

                mainViewModel.getFilterDisaster(newQuery!!).observe(this@MainActivity) {
                    if (it != null) {
                        when (it) {
                            is ResultCustom.Loading -> binding.loadingMarker.visibility = View.VISIBLE

                            is ResultCustom.Success -> {
                                if (it.data!!.isNotEmpty()) {
                                    binding.loadingMarker.visibility = View.GONE
                                    mMap.clear()

                                    val boundsBuilder = LatLngBounds.Builder()
                                    it.data.forEach { disaster ->
                                        val latLng =
                                            LatLng(
                                                disaster!!.coordinates[1],
                                                disaster.coordinates[0]
                                            )
                                        val marker = mMap.addMarker(
                                            MarkerOptions()
                                                .position(latLng)
                                                .title("${disaster.properties?.disasterType}")
                                                .snippet("${disaster.properties?.text}")
                                        )
                                        boundsBuilder.include(latLng)
                                        markersList.add(marker!!)
                                    }
                                    val bounds: LatLngBounds = boundsBuilder.build()
                                    mMap.animateCamera(
                                        CameraUpdateFactory.newLatLngBounds(
                                            bounds,
                                            resources.displayMetrics.widthPixels,
                                            resources.displayMetrics.heightPixels,
                                            100
                                        )

                                    )
                                }
                            }
                            is ResultCustom.Error -> {
                                binding.loadingMarker.visibility = View.GONE
                                mMap.clear()
                            }

                        }
                    }
                }
            }

        }
    }

    private fun listDisaster(
        isSearchEmpty: Boolean? = null,
        isFilterEmpty: Boolean? = null,
        querySearch: String? = null,
        queryFilter: String? = null
    ) {

        if (isSearchEmpty == true && isFilterEmpty == true) {
            mainViewModel.getDisaster().observe(this) {
                if (it != null) {
                    when (it) {
                        is ResultCustom.Loading -> {
                            binding.bottomSheet.loadingList.visibility = View.VISIBLE
                            binding.bottomSheet.rvItem.visibility = View.GONE
                            binding.bottomSheet.noData.visibility = View.GONE
                        }

                        is ResultCustom.Success ->
                            if (it.data!!.isNotEmpty()) {
                                binding.bottomSheet.loadingList.visibility = View.GONE
                                binding.bottomSheet.rvItem.visibility = View.VISIBLE
                                binding.bottomSheet.noData.visibility = View.GONE
                                var listDisaster = ArrayList<Properties>()
                                it.data.forEach { disaster ->
                                    listDisaster.add(
                                        Properties(
                                            disaster?.properties?.imageUrl,
                                            disaster?.properties?.disasterType,
                                            disaster?.properties?.text,
                                        )
                                    )
                                }
                                val bottomSheet =
                                    findViewById<ConstraintLayout>(R.id.bottom_sheet)
                                bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                                bottomSheetBehavior.peekHeight = 65
                                bottomSheetBehavior.isFitToContents = false
                                bottomSheetBehavior.halfExpandedRatio = 0.3f
                                bottomSheetBehavior.state =
                                    BottomSheetBehavior.STATE_HALF_EXPANDED
                                recyclerView = findViewById(R.id.rv_item)
                                disasterAdapter = DisasterAdapter(listDisaster)
                                recyclerView.adapter = disasterAdapter
                            }

                        is ResultCustom.Error -> {
                            binding.bottomSheet.loadingList.visibility = View.GONE
                            binding.bottomSheet.rvItem.visibility = View.GONE
                            binding.bottomSheet.noData.visibility = View.VISIBLE
                        }

                    }
                }
            }
        } else if (isSearchEmpty == false && isFilterEmpty == true) {
            mainViewModel.getSearchingDisaster(querySearch!!).observe(this@MainActivity) {
                if (it != null) {
                    when (it) {
                        is ResultCustom.Loading -> {
                            binding.bottomSheet.loadingList.visibility = View.VISIBLE
                            binding.bottomSheet.rvItem.visibility = View.GONE
                            binding.bottomSheet.noData.visibility = View.GONE
                        }

                        is ResultCustom.Success ->
                            if (it.data!!.isNotEmpty()) {
                                binding.bottomSheet.loadingList.visibility = View.GONE
                                binding.bottomSheet.rvItem.visibility = View.VISIBLE
                                binding.bottomSheet.noData.visibility = View.GONE
                                var listDisasterArea = ArrayList<Properties>()

                                it.data.forEach { disaster ->
                                    listDisasterArea.add(
                                        Properties(
                                            disaster?.properties?.imageUrl,
                                            disaster?.properties?.disasterType,
                                            disaster?.properties?.text,
                                        )
                                    )
                                }
                                val bottomSheet =
                                    findViewById<ConstraintLayout>(R.id.bottom_sheet)
                                bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                                bottomSheetBehavior.peekHeight = 65
                                bottomSheetBehavior.isFitToContents = false
                                bottomSheetBehavior.halfExpandedRatio = 0.3f
                                bottomSheetBehavior.state =
                                    BottomSheetBehavior.STATE_HALF_EXPANDED
                                recyclerView = findViewById(R.id.rv_item)
                                disasterAdapter = DisasterAdapter(listDisasterArea)
                                recyclerView.adapter = disasterAdapter
                            }

                        is ResultCustom.Error -> {
                            val bottomSheet =
                                findViewById<ConstraintLayout>(R.id.bottom_sheet)
                            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                            bottomSheetBehavior.peekHeight = 65
                            bottomSheetBehavior.isFitToContents = false
                            bottomSheetBehavior.halfExpandedRatio = 0.3f
                            bottomSheetBehavior.state =
                                BottomSheetBehavior.STATE_HALF_EXPANDED
                            binding.bottomSheet.loadingList.visibility = View.GONE
                            binding.bottomSheet.rvItem.visibility = View.GONE
                            binding.bottomSheet.noData.visibility = View.VISIBLE

                        }

                    }
                }
            }
        } else if (isSearchEmpty == true && isFilterEmpty == false) {
            mainViewModel.getFilterDisaster(queryFilter!!).observe(this@MainActivity) {
                if (it != null) {
                    when (it) {
                        is ResultCustom.Loading -> {
                            binding.bottomSheet.loadingList.visibility = View.VISIBLE
                            binding.bottomSheet.rvItem.visibility = View.GONE
                            binding.bottomSheet.noData.visibility = View.GONE
                        }

                        is ResultCustom.Success ->
                            if (it.data!!.isNotEmpty()) {
                                binding.bottomSheet.loadingList.visibility = View.GONE
                                binding.bottomSheet.rvItem.visibility = View.VISIBLE
                                binding.bottomSheet.noData.visibility = View.GONE
                                var listDisasterArea = ArrayList<Properties>()

                                it.data.forEach { disaster ->
                                    listDisasterArea.add(
                                        Properties(
                                            disaster?.properties?.imageUrl,
                                            disaster?.properties?.disasterType,
                                            disaster?.properties?.text,
                                        )
                                    )
                                }
                                val bottomSheet =
                                    findViewById<ConstraintLayout>(R.id.bottom_sheet)
                                bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                                bottomSheetBehavior.peekHeight = 65
                                bottomSheetBehavior.isFitToContents = false
                                bottomSheetBehavior.halfExpandedRatio = 0.3f
                                bottomSheetBehavior.state =
                                    BottomSheetBehavior.STATE_HALF_EXPANDED
                                recyclerView = findViewById(R.id.rv_item)
                                disasterAdapter = DisasterAdapter(listDisasterArea)
                                recyclerView.adapter = disasterAdapter


                            }

                        is ResultCustom.Error -> {
                            val bottomSheet =
                                findViewById<ConstraintLayout>(R.id.bottom_sheet)
                            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                            bottomSheetBehavior.peekHeight = 65
                            bottomSheetBehavior.isFitToContents = false
                            bottomSheetBehavior.halfExpandedRatio = 0.3f
                            bottomSheetBehavior.state =
                                BottomSheetBehavior.STATE_HALF_EXPANDED
                            binding.bottomSheet.loadingList.visibility = View.GONE
                            binding.bottomSheet.rvItem.visibility = View.GONE
                            binding.bottomSheet.noData.visibility = View.VISIBLE

                        }

                    }
                }
            }

        }
    }

    fun onTextViewClick(view: View) {
        if (view.id == R.id.tv_filter) {
            listDisaster(true, true)
            binding.tvFilter.visibility = View.GONE
            isFilterEmpty.value = true
            isSearchEmpty.value = true
        }
    }


}