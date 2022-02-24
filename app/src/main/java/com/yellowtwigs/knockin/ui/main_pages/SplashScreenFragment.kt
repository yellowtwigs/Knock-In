package com.yellowtwigs.knockin.ui.main_pages

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.yellowtwigs.knockin.R
import com.yellowtwigs.knockin.databinding.FragmentSplashScreenBinding
import com.yellowtwigs.knockin.ui.BaseFragment
import com.yellowtwigs.knockin.utils.AppTheme.checkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : BaseFragment() {

    private var binding: FragmentSplashScreenBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkTheme(cxt)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkIfFromSplashScreen()
        setupAnimation()
    }

    private fun checkIfFromSplashScreen() {
        val sharedFromSplashScreen =
            cxt.getSharedPreferences("fromSplashScreen", Context.MODE_PRIVATE)
        sharedFromSplashScreen.getBoolean("fromSplashScreen", false)
        val edit = sharedFromSplashScreen.edit()
        edit.putBoolean("fromSplashScreen", true)
        edit.apply()
    }

    private fun setupAnimation() {
        val slideDown = AnimationUtils.loadAnimation(cxt, R.anim.slide_to_down)
        val reappear = AnimationUtils.loadAnimation(cxt, R.anim.reappear)

        binding?.apply {
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                appNameCenter.startAnimation(slideDown)

                delay(1450)
                appNameCenter.visibility = View.INVISIBLE
                appNameDown.visibility = View.VISIBLE

                appIcon.startAnimation(reappear)
                appIcon.visibility = View.VISIBLE
                yellowtwigs.startAnimation(reappear)
                yellowtwigs.visibility = View.VISIBLE
                subtitle.startAnimation(reappear)
                subtitle.visibility = View.VISIBLE

                delay(3000)
                val navDir =
                    SplashScreenFragmentDirections.actionSplashScreenFragmentToMainFragment()
                navController.navigate(navDir)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}