package com.example.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sample.ui.main.MainFragment

class MainActivity : AppCompatActivity(R.layout.main_activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState ?: run {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment())
                .commitNow()
        }
    }
}