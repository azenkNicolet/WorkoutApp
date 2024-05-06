package com.zenk.workoutapp

import android.app.Activity
import android.app.Dialog
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.zenk.workoutapp.databinding.ActivityExerciseBinding
import com.zenk.workoutapp.databinding.DialogCustomBackConfirmationBinding
import java.util.Locale

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var tts : TextToSpeech? = null
    private var player : MediaPlayer? = null

    private var binding : ActivityExerciseBinding? = null

    private var restTimer : CountDownTimer? = null
    private var restProgress = 0

    private var exerciseTimer : CountDownTimer? = null
    private var exerciseProgress = 0

    private var exerciseList : ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    private var exerciseAdapter : ExerciseStatusAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        tts = TextToSpeech(this, this)

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
            customDialogForBackButton()
        }

        setUpRestView()
        setUpExerciseStatusRecyclerView()
    }


    override fun onBackPressed() {
        customDialogForBackButton()
        //super.onBackPressed()
    }
    private fun customDialogForBackButton()
    {
        val customDialog = Dialog(this)
        val dialogBinding = DialogCustomBackConfirmationBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(false)

        dialogBinding.btnYes.setOnClickListener{
            this@ExerciseActivity.finish()
            customDialog.dismiss()
        }

        dialogBinding.btnNo.setOnClickListener{
            customDialog.dismiss()
        }

        customDialog.show()
    }
    private fun setUpExerciseStatusRecyclerView()
    {
        binding?.rvExerciseStatus?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        exerciseAdapter = ExerciseStatusAdapter(exerciseList!!)
        binding?.rvExerciseStatus?.adapter = exerciseAdapter
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

        speakOut(exerciseList!![currentExercisePosition].getName())

        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding?.tvExercise?.text = exerciseList!![currentExercisePosition].getName()

        setExerciseProgressBar()
    }
    private fun setUpRestView()
    {
        try {
            val soundURI = Uri.parse("android.resource://com.zenk.workoutapp/"
                    + R.raw.press_start)

            player = MediaPlayer.create(applicationContext, soundURI)
            player?.isLooping = false
            player?.start()

        }catch(e: Exception){
            e.printStackTrace()
        }

        binding?.flRestView?.visibility = View.VISIBLE
        binding?.tvTitle?.visibility = View.VISIBLE
        binding?.tvUpcoming?.visibility = View.VISIBLE
        binding?.tvNext?.visibility = View.VISIBLE
        binding?.tvExercise?.visibility = View.INVISIBLE
        binding?.ivImage?.visibility = View.INVISIBLE
        binding?.flExerciseView?.visibility = View.INVISIBLE



        if(restTimer != null)
        {
            restTimer?.cancel()
            restProgress = 0
        }

        setRestProgressBar()

        setUpcomingText()
    }

    private fun setUpcomingText()
    {
        binding?.tvNext?.text = exerciseList!![currentExercisePosition + 1].getName()

    }

    private fun speakOut(text : String)
    {
        //Text to speech support
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
    private fun setRestProgressBar()
    {
        binding?.progressBar?.progress = restProgress

        restTimer = object : CountDownTimer(3000, 1000){
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

                exerciseList!![currentExercisePosition].setIsSelected(true)
                exerciseAdapter!!.notifyDataSetChanged()

                setUpExerciseView()

            }

        }.start()
    }

    private fun setExerciseProgressBar()
    {
        binding?.exerciseProgressBar?.progress = exerciseProgress

        exerciseTimer = object : CountDownTimer(3000, 1000){
            override fun onTick(p0: Long) {
                exerciseProgress++
                binding?.exerciseProgressBar?.progress = 30 - exerciseProgress
                binding?.tvTimerExercise?.text = (30 - exerciseProgress).toString()
            }

            override fun onFinish() {

                exerciseList!![currentExercisePosition].setIsSelected(false)
                exerciseList!![currentExercisePosition].setIsCompleted(true)
                exerciseAdapter!!.notifyDataSetChanged()

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

        if(player != null)
        {
            player?.stop()
        }
        if(tts != null)
        {
            tts?.stop()
            tts?.shutdown()
        }
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

    override fun onInit(status: Int) {

        if(status == TextToSpeech.SUCCESS)
        {
            val result = tts!!.setLanguage(Locale.US)

            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Log.e("TTS", "The language specified is not supported!")
            }
        }
        else
        {
            Log.e("TTS", "Initialization Failed!")
        }
    }
}