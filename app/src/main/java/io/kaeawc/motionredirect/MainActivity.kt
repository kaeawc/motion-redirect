package io.kaeawc.motionredirect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.constraintlayout.motion.widget.MotionScene.Transition.AUTO_ANIMATE_TO_END
import androidx.constraintlayout.motion.widget.MotionScene.Transition.AUTO_NONE
import androidx.constraintlayout.widget.ConstraintSet.TOP
import androidx.constraintlayout.widget.ConstraintSet.START
import androidx.constraintlayout.widget.ConstraintSet.END
import androidx.constraintlayout.widget.ConstraintSet.BOTTOM
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.lang.reflect.Field

class MainActivity : AppCompatActivity() {

    private var currentDestination: Int = -1
    private val availableWidth: Int by lazy { motion_root.measuredWidth }
    private val screenHeight: Int by lazy { motion_root.measuredHeight }
    private val availableHeight: Int by lazy { screenHeight - R.dimen.button_height.dp() }

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

    private fun moveTo(destinationState: Int) {
        motion_root?.rebuildScene()
        val viewId = R.id.ball_view
        val startConstraints = motion_root?.getConstraintSet(R.id.placeholder_start)
        val currentTop = ball_view?.top ?: 0
        val currentLeft = ball_view?.left ?: 0
        startConstraints?.setMargin(viewId, TOP, currentTop)
        startConstraints?.setMargin(viewId, START, currentLeft)
        startConstraints?.clear(viewId, END)
        startConstraints?.clear(viewId, BOTTOM)
        motion_root?.updateState(R.id.placeholder_start, startConstraints)

        val transitions = motion_root?.definedTransitions?.filterNotNull() ?: emptyList()
        val activeTransition = transitions.firstOrNull { it.endConstraintSetId == destinationState } ?: return
        setAutoTransition(activeTransition, AUTO_ANIMATE_TO_END)
        transitions.filterNot { it.endConstraintSetId == destinationState }
            .forEach { setAutoTransition(it, AUTO_NONE) }

        val (nextLeft, nextTop) = getLeftAndTop(destinationState) ?: return
        val (prevLeft, prevTop) = getLeftAndTop(currentDestination) ?: nextLeft to nextTop
        val betweenTop = ((prevTop - currentTop) / 2) + currentTop
        val betweenLeft = ((prevLeft - currentLeft) / 2) + currentLeft

        val betweenDuration = when (Math.abs(prevTop - currentTop) + Math.abs(prevLeft - currentLeft)) {
            in 0 until 100 -> 10
            in 100 until 250 -> 100
            else -> 250
        }

        transitions.firstOrNull { it.endConstraintSetId == R.id.placeholder_end }?.duration = betweenDuration

        val endConstraints = motion_root?.getConstraintSet(R.id.placeholder_end)
        endConstraints?.setMargin(viewId, TOP, betweenTop)
        endConstraints?.setMargin(viewId, START, betweenLeft)
        endConstraints?.clear(viewId, END)
        endConstraints?.clear(viewId, BOTTOM)
        motion_root?.updateState(R.id.placeholder_end, endConstraints)
        motion_root?.setTransition(R.id.placeholder_start, R.id.placeholder_end)
        motion_root?.progress = 0f
        motion_root?.transitionToEnd()

        currentDestination = destinationState
    }

    private fun setAutoTransition(transition: MotionScene.Transition, value: Int) {
        try {
            val field: Field = MotionScene.Transition::class.java.getDeclaredField("mAutoTransition")
            field.isAccessible = true
            field.setInt(transition, value)
            field.isAccessible = false
        } catch (ex: NoSuchFieldException) {
            Timber.e(ex, "Could not set autoTransition flag")
        }
    }

    private fun Int.dp(): Int {
        return resources.getDimensionPixelSize(this)
    }

    private fun getLeftAndTop(state: Int): Pair<Int, Int>? {
        return when (state) {
            R.id.top_left -> R.dimen.margin.dp() to R.dimen.margin.dp()
            R.id.bottom_left -> R.dimen.margin.dp() to availableHeight - R.dimen.ball_and_margin.dp()
            R.id.bottom_right -> availableWidth - R.dimen.ball_and_margin.dp() to availableHeight - R.dimen.ball_and_margin.dp()
            R.id.top_right -> availableWidth - R.dimen.ball_and_margin.dp() to R.dimen.margin.dp()
            else -> null
        }
    }
}
