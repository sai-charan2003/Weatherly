package com.example.weatherly

import android.app.Activity
import android.app.DownloadManager.Query
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log

import androidx.appcompat.app.AppCompatActivity

import com.example.weatherly.databinding.ActivitySearchBinding
import android.widget.SearchView



class search : AppCompatActivity() {


    private val binding: ActivitySearchBinding by lazy {
        ActivitySearchBinding.inflate(layoutInflater)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        var  text: String? =null



        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportActionBar?.hide()


        binding.searchview
            .getEditText()
            .setOnEditorActionListener { v, actionId, event ->

                binding.searchBar.setText(binding.searchview.getText())
                text= binding.searchview.getText().toString()
                binding.searchview.hide()
                Log.d("TAG", "onCreate: $text")
                val returnValue = text

                val resultIntent = Intent()
                resultIntent.putExtra("RETURN_VALUE_KEY", returnValue)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
                false
            }

//            binding.search.setOnClickListener {
//
//
//                val returnValue = text
//
//                val resultIntent = Intent()
//                resultIntent.putExtra("RETURN_VALUE_KEY", returnValue)
//                setResult(Activity.RESULT_OK, resultIntent)
//                finish() // Close the second activity
//            }










    }



}
