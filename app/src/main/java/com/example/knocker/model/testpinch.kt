//package com.example.knocker.model
//
//import android.os.Bundle
//import android.support.v7.app.AppCompatActivity
//import android.view.MotionEvent
//import android.view.ScaleGestureDetector
//import androidx.appcompat.app.AppCompatActivity
//import com.example.knocker.R
//
//class testpinch : AppCompatActivity() {
//    var scaleGestureDetectore: ScaleGestureDetector? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.pinch)
//        scaleGestureDetectore = ScaleGestureDetector(this,
//                MyOnScaleGestureListener())
//    }
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        scaleGestureDetectore?.onTouchEvent(event)
//        return true
//    }
////
//    inner class MyOnScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
//        override fun onScale(detector: ScaleGestureDetector): Boolean {
//            val scaleFactor = detector.scaleFactor
//            if (scaleFactor > 1) {
//                println("Zooming Out" + scaleFactor)
//            } else {
//                println("Zooming In" + scaleFactor)
//            }
//            return true
//        }
//
//        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
//            println("begin")
//            return true
//        }
//
//        override fun onScaleEnd(detector: ScaleGestureDetector) {
//            println("end")
//        }
//    }
//}