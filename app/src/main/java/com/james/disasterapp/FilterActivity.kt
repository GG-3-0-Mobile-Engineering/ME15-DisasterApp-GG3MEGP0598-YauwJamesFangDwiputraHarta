package com.james.disasterapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.view.ContentInfoCompat.Flags
import androidx.core.widget.addTextChangedListener
import com.james.disasterapp.AdminArea.suggestions
import com.james.disasterapp.databinding.ActivityFilterBinding

class FilterActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityFilterBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.SettingActivity)
        setContentView(binding.root)

        var mAdapter = ArrayAdapter(this, R.layout.textview, AdminArea.typeDisaster)

        binding.edtProvince.setAdapter(mAdapter)
        binding.edtProvince.addTextChangedListener { typeDisaster ->
            val foundPair = AdminArea.suggestions.find { it.first.toString() == typeDisaster.toString()  }
//            val intent = Intent(this, MainActivity::class.java)
//            intent.putExtra(MainActivity.LOCATION, foundPair?.second.toString())
//            startActivity(intent)
//            finish()

            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_SELECTED_VALUE, typeDisaster.toString())
            setResult(RESULT_CODE, resultIntent)
            finish()


//            Toast.makeText(
//                this@FilterActivity,
//                ("${it}"),
//                Toast.LENGTH_LONG
//            ).show()
        }

        val actionBar = supportActionBar
        actionBar!!.title = "Filter Activity"
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    companion object {
        const val EXTRA_SELECTED_VALUE = "extra_selected_value"
        const val RESULT_CODE = 110
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}