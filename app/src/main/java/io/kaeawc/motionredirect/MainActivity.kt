package io.kaeawc.motionredirect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.TOP
import androidx.constraintlayout.widget.ConstraintSet.START
import androidx.constraintlayout.widget.ConstraintSet.END
import androidx.constraintlayout.widget.ConstraintSet.BOTTOM
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        top_left_button?.setOnClickListener { moveTo(R.id.top_left) }
        bottom_left_button?.setOnClickListener { moveTo(R.id.bottom_left) }
        bottom_right_button?.setOnClickListener { moveTo(R.id.bottom_right) }
        top_right_button?.setOnClickListener { moveTo(R.id.top_right) }
    }

    private fun moveTo(state: Int) {
        motion_root?.rebuildScene()
        val viewId = R.id.ball_view
        val newConstraints = motion_root?.getConstraintSet(R.id.placeholder)
        newConstraints?.setMargin(viewId, TOP, ball_view?.top ?: 0)
        newConstraints?.setMargin(viewId, START, ball_view?.left ?: 0)
        newConstraints?.clear(viewId, END)
        newConstraints?.clear(viewId, BOTTOM)
        motion_root?.updateState(R.id.placeholder, newConstraints)
        motion_root?.setTransition(R.id.placeholder, state)
        motion_root?.transitionToEnd()
    }

    private fun getTransientConstraints(): ConstraintSet {
        TODO()
    }
}
