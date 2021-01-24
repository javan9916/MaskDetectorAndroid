package com.example.maskdetector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var mainNav: BottomNavigationView
    private lateinit var mainFrame: FrameLayout

    private lateinit var imageFragment: ImageFragment
    private lateinit var liveFragment: LiveFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainNav = findViewById(R.id.main_nav)
        mainFrame = findViewById(R.id.main_frame)

        imageFragment = ImageFragment()
        liveFragment = LiveFragment()

        setFragment(imageFragment)

        mainNav.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { view ->
            when (view.itemId) {
                R.id.nav_image -> {
                    setFragment(imageFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_live -> {
                    setFragment(liveFragment)
                    return@OnNavigationItemSelectedListener true
                }
                else -> {
                    return@OnNavigationItemSelectedListener false
                }
            }
        })
    }

    private fun setFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_frame, fragment)
        fragmentTransaction.commit()
    }
}