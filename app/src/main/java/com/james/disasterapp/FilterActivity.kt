package com.james.disasterapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.james.disasterapp.AdminArea.suggestions
import com.james.disasterapp.databinding.ActivityFilterBinding

class FilterActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityFilterBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var mAdapter = ArrayAdapter(this, R.layout.textview, AdminArea.suggestions.map{
            it.first
        })

        binding.edtProvince.setAdapter(mAdapter)
        binding.edtProvince.addTextChangedListener { province ->
            val foundPair = AdminArea.suggestions.find { it.first.toString() == province.toString()  }
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(MainActivity.LOCATION, foundPair?.second.toString())
            startActivity(intent)

//            Toast.makeText(
//                this@FilterActivity,
//                ("${it}"),
//                Toast.LENGTH_LONG
//            ).show()
        }




    }
}