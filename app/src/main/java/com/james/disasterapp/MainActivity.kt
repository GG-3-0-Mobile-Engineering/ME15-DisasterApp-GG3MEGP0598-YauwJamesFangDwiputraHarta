package com.james.disasterapp

import android.app.SearchManager
import android.database.Cursor
import android.database.MatrixCursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
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

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetBinding: BottomSheetBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var disasterAdapter: DisasterAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var isSearchEmpty: MutableLiveData<Boolean> = MutableLiveData(true)
    private var query = MutableLiveData("")
    private val markersList: MutableList<Marker> = mutableListOf()


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

        showBottomSheet()

        searchView()

    }


    private fun showBottomSheet() {

        mainViewModel.getDisaster().observe(this) {
            if (it != null) {
                when (it) {
                    is ResultCustom.Loading -> Toast.makeText(
                        this,
                        ("loading"),
                        Toast.LENGTH_LONG
                    ).show()
                    is ResultCustom.Success ->

                        if (it.data!!.isNotEmpty()) {
                            var listDisaster = ArrayList<Properties>()
                            it.data.forEach { disaster ->
                                listDisaster.add(
                                    Properties(
                                        disaster?.properties?.imageUrl,
                                        disaster?.properties?.disasterType
                                    )
                                )
//                                Log.d("kok ga muncul", "${disaster?.properties?.imageUrl}")
                            }
                            val bottomSheet = findViewById<ConstraintLayout>(R.id.bottom_sheet)
                            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                            bottomSheetBehavior.peekHeight = 100
                            bottomSheetBehavior.isFitToContents = false
                            bottomSheetBehavior.halfExpandedRatio = 0.3f
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                            recyclerView = findViewById(R.id.rv_item)
                            disasterAdapter = DisasterAdapter(listDisaster)
                            recyclerView.adapter = disasterAdapter
                        } else {
                            Toast.makeText(
                                this,
                                ("no data"),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    is ResultCustom.Error -> {
                        Toast.makeText(
                            this,
                            ("terjadi kesalahan"),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
        }
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
                    showBottomSheet()
                    isSearchEmpty.value = true
                }

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

                // Do whatever you want with selection text

                if (idArea != null) {
                    query.value = idArea
                    isSearchEmpty.value = false
                    Log.d("id area", "${idArea}")

                    mainViewModel.getSearchingDisaster(idArea!!).observe(this@MainActivity) {
                        if (it != null) {
                            when (it) {
                                is ResultCustom.Loading -> Toast.makeText(
                                    this@MainActivity,
                                    ("loading"),
                                    Toast.LENGTH_LONG
                                ).show()

                                is ResultCustom.Success ->

                                    if (it.data!!.isNotEmpty()) {
                                        var listDisasterArea = ArrayList<Properties>()
                                        it.data.forEach { disaster ->
                                            listDisasterArea.add(
                                                Properties(
                                                    disaster?.properties?.imageUrl,
                                                    disaster?.properties?.disasterType
                                                )
                                            )
                                            Log.d(
                                                "kok ga muncul",
                                                "${disaster?.properties?.imageUrl}"
                                            )
                                        }
                                        val bottomSheet =
                                            findViewById<ConstraintLayout>(R.id.bottom_sheet)
                                        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                                        bottomSheetBehavior.peekHeight = 100
                                        bottomSheetBehavior.isFitToContents = false
                                        bottomSheetBehavior.halfExpandedRatio = 0.3f
                                        bottomSheetBehavior.state =
                                            BottomSheetBehavior.STATE_HALF_EXPANDED
                                        recyclerView = findViewById(R.id.rv_item)
                                        disasterAdapter = DisasterAdapter(listDisasterArea)
                                        recyclerView.adapter = disasterAdapter


                                    } else {
                                        Toast.makeText(
                                            this@MainActivity,
                                            ("no data"),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                is ResultCustom.Error -> {
                                    Toast.makeText(
                                        this@MainActivity,
                                        ("${it.error}"),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            }
                        }
                    }
                }
                else {
                    mMap.clear()
                    Toast.makeText(
                        this@MainActivity,
                        ("Area tidak ditemukan"),
                        Toast.LENGTH_LONG
                    ).show()
                }




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

                // Do whatever you want with selection text
                query.value = intentData
                isSearchEmpty.value = false
                mainViewModel.getSearchingDisaster(query.value!!).observe(this@MainActivity) {
                    if (it != null) {
                        when (it) {
                            is ResultCustom.Loading -> Toast.makeText(
                                this@MainActivity,
                                ("loading"),
                                Toast.LENGTH_LONG
                            ).show()

                            is ResultCustom.Success ->

                                if (it.data!!.isNotEmpty()) {
                                    var listDisasterArea = ArrayList<Properties>()
                                    it.data.forEach { disaster ->
                                        listDisasterArea.add(
                                            Properties(
                                                disaster?.properties?.imageUrl,
                                                disaster?.properties?.disasterType
                                            )
                                        )
                                        Log.d("kok ga muncul", "${disaster?.properties?.imageUrl}")
                                    }
                                    val bottomSheet =
                                        findViewById<ConstraintLayout>(R.id.bottom_sheet)
                                    bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                                    bottomSheetBehavior.peekHeight = 100
                                    bottomSheetBehavior.isFitToContents = false
                                    bottomSheetBehavior.halfExpandedRatio = 0.3f
                                    bottomSheetBehavior.state =
                                        BottomSheetBehavior.STATE_HALF_EXPANDED
                                    recyclerView = findViewById(R.id.rv_item)
                                    disasterAdapter = DisasterAdapter(listDisasterArea)
                                    recyclerView.adapter = disasterAdapter


                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        ("no data"),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            is ResultCustom.Error -> {
                                Toast.makeText(
                                    this@MainActivity,
                                    ("${it.error}"),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        }
                    }
                }


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
            if (isSearchEmpty) {
                mMap.clear()
                mainViewModel.getDisaster().observe(this) {
                    if (it != null) {
                        when (it) {
                            is ResultCustom.Loading -> Toast.makeText(
                                this,
                                ("loading"),
                                Toast.LENGTH_LONG
                            ).show()

                            is ResultCustom.Success -> {
                                if (it.data!!.isNotEmpty()) {

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
                                        Log.d(
                                            "coba lagi yuk",
                                            "${disaster.properties?.disasterType}"
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
                                } else {
                                    Toast.makeText(this, "no data", Toast.LENGTH_LONG).show()

                                }

                            }
                            is ResultCustom.Error -> {
                                Toast.makeText(this, it.error, Toast.LENGTH_LONG).show()
                            }

                        }
                    }

                }
            } else {

                mMap.clear()
                GlobalScope.launch {
                    delay(2000L)
                }

                val newQuery: String? = query.value

                mainViewModel.getSearchingDisaster(newQuery!!).observe(this@MainActivity) {
                    if (it != null) {
                        when (it) {
                            is ResultCustom.Loading -> Toast.makeText(
                                this@MainActivity,
                                ("loading"),
                                Toast.LENGTH_LONG
                            ).show()

                            is ResultCustom.Success -> {
                                if (it.data!!.isNotEmpty()) {

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
                                        Log.d(
                                            "coba lagi yuk",
                                            "${disaster.properties?.disasterType}"
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
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "no data",
                                        Toast.LENGTH_LONG
                                    ).show()

                                }

                            }
                            is ResultCustom.Error -> {
                                Toast.makeText(this@MainActivity, it.error, Toast.LENGTH_LONG)
                                    .show()
                            }

                        }
                    }
                }
            }
        }


    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.map_options, menu)
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.normal_type -> {
//                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
//                true
//            }
//            R.id.satellite_type -> {
//                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
//                true
//            }
//            R.id.terrain_type -> {
//                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
//                true
//            }
//            R.id.hybrid_type -> {
//                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
//                true
//            }
//            else -> {
//                super.onOptionsItemSelected(item)
//            }
//        }
//    }
}