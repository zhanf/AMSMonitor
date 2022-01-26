package com.shyii.ams

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

   private val byteArray = ByteArray(500 * 1024)

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.tvNext).setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java).apply {
                putExtra("extra", byteArray)
            })
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putByteArray("byteArray",ByteArray(50 * 1024))
        super.onSaveInstanceState(outState)
    }
}