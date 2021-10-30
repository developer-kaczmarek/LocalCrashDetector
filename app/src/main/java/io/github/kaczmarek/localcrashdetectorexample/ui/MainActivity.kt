package io.github.kaczmarek.localcrashdetectorexample.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import io.github.kaczmarek.localcrashdetectorexample.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.btn_call_crash)
        btn.setOnClickListener {
            throw NullPointerException("Button Call NullPointerException. Sorry")
        }
    }
}