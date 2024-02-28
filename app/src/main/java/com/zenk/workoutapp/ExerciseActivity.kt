package com.zenk.workoutapp

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import com.zenk.workoutapp.databinding.ActivityExerciseBinding

class ExerciseActivity : AppCompatActivity() {

    private var binding : ActivityExerciseBinding? = null

    private var restTimer : CountDownTimer? = null
    private var restProgress = 0

    private var exerciseTimer : CountDownTimer? = null
    private var exerciseProgress = 0

    private var exerciseList : ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarExercise)

        if(supportActionBar != null)
        {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        exerciseList = Constants.defaultExerciseList()

        binding?.toolbarExercise?.setNavigationOnClickListener{
            onBackPressed()
        }

        setUpRestView()
    }

    private fun setUpExerciseView()
    {
        binding?.flRestView?.visibility = View.INVISIBLE
        binding?.tvTitle?.visibility = View.INVISIBLE
        binding?.tvNext?.visibility = View.INVISIBLE
        binding?.tvUpcoming?.visibility = View.INVISIBLE
        binding?.tvExercise?.visibility = View.VISIBLE
        binding?.ivImage?.visibility = View.VISIBLE
        binding?.flExerciseView?.visibility = View.VISIBLE


        if(exerciseTimer != null)
        {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding?.tvExercise?.text = exerciseList!![currentExercisePosition].getName()

        setExerciseProgressBar()
    }
    private fun setUpRestView()
    {
        binding?.flRestView?.visibility = View.VISIBLE
        binding?.tvTitle?.visibility = View.VISIBLE
        binding?.tvUpcoming?.visibility = View.VISIBLE
        binding?.tvNext?.visibility = View.VISIBLE
        binding?.tvExercise?.visibility = View.INVISIBLE
        binding?.ivImage?.visibility = View.INVISIBLE
        binding?.flExerciseView?.visibility = View.INVISIBLE

        setUpcomingText()

        if(restTimer != null)
        {
            restTimer?.cancel()
            restProgress = 0
        }

        setRestProgressBar()
    }

    private fun setUpcomingText()
    {
        binding?.tvNext?.text = exerciseList!![currentExercisePosition + 1].getName()
    }
    private fun setRestProgressBar()
    {
        binding?.progressBar?.progress = restProgress

        restTimer = object : CountDownTimer(10000, 1000){
            override fun onTick(p0: Long) {
                restProgress++
                binding?.progressBar?.progress = 10 - restProgress
                binding?.tvTimer?.text = (10 - restProgress).toString()
            }

            override fun onFinish() {
                Toast.makeText(this@ExerciseActivity,
                    "Time to start exercising!",
                    Toast.LENGTH_SHORT
                ).show()

                currentExercisePosition++

                setUpExerciseView()

            }

        }.start()
    }

    private fun setExerciseProgressBar()
    {
        binding?.exerciseProgressBar?.progress = exerciseProgress

        exerciseTimer = object : CountDownTimer(30000, 1000){
            override fun onTick(p0: Long) {
                exerciseProgress++
                binding?.exerciseProgressBar?.progress = 30 - exerciseProgress
                binding?.tvTimerExercise?.text = (30 - exerciseProgress).toString()
            }

            override fun onFinish() {

                if(currentExercisePosition < exerciseList?.size!! - 1)
                {
                    setUpRestView()
                }
                else
                {
                    Toast.makeText(this@ExerciseActivity,
                    "Congratulations! You are done!",
                    Toast.LENGTH_SHORT).show()
                }

            }

        }.start()
    }


    override fun onDestroy() {
        super.onDestroy()

        if(restTimer != null)
        {
            restTimer?.cancel()
            restProgress = 0
        }

        if(exerciseTimer != null)
        {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        binding = null
    }
}