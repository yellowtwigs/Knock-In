package com.yellowtwigs.knockin.ui.activities.edit_contact

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import com.yellowtwigs.knockin.databinding.ActivityEditContactBinding

class EditContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditContactBinding
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}