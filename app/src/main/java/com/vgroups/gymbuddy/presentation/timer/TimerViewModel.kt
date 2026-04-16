package com.vgroups.gymbuddy.presentation.timer

import android.content.Context
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vgroups.gymbuddy.data.repository.ExerciseRepository
import com.vgroups.gymbuddy.domain.model.Exercise
import com.vgroups.gymbuddy.domain.model.WorkoutSession
import com.vgroups.gymbuddy.domain.model.WorkoutSplits
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class TimerUiState(
    val exercises: List<Exercise> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val currentSet: Int = 1,
    val totalSets: Int = 4,
    val setElapsedSeconds: Int = 0,   // active set timer (counts up)
    val restSecondsLeft: Int = 0,     // rest timer (counts down)
    val isResting: Boolean = false,
    val isPreparing: Boolean = false,
    val prepSecondsLeft: Int = 0,
    val workoutComplete: Boolean = false,
    val workoutElapsedSeconds: Int = 0,
    val restDurationSeconds: Int = 60
) {
    val currentExercise: Exercise? get() = exercises.getOrNull(currentExerciseIndex)
    val overallProgress: Float
        get() {
            if (exercises.isEmpty()) return 0f
            val totalSetsAll = exercises.sumOf { it.sets }
            val completedSets = exercises.take(currentExerciseIndex).sumOf { it.sets } +
                    (currentSet - 1)
            return completedSets.toFloat() / totalSetsAll
        }
}

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val splitId: String = checkNotNull(savedStateHandle["splitId"])
    val dayIndex: Int = checkNotNull(savedStateHandle["dayIndex"])
    val split = WorkoutSplits.all.first { it.id == splitId }
    val workoutDay = split.days[dayIndex]

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var setTimerJob: Job? = null
    private var restTimerJob: Job? = null
    private var prepTimerJob: Job? = null
    private var workoutTimerJob: Job? = null
    private val workoutStartTime = System.currentTimeMillis()

    init {
        loadExercises()
        startWorkoutTimer()
    }

    private fun loadExercises() {
        viewModelScope.launch(Dispatchers.IO) {
            val exercises = repository.getExercisesForDay(workoutDay.bodyParts)
            _uiState.update { 
                it.copy(
                    exercises = exercises, 
                    totalSets = exercises.firstOrNull()?.sets ?: 4,
                    currentSet = 1,
                    currentExerciseIndex = 0
                ) 
            }
            startPreparation()
        }
    }

    private fun startWorkoutTimer() {
        workoutTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(workoutElapsedSeconds = it.workoutElapsedSeconds + 1) }
            }
        }
    }

    private fun startPreparation() {
        prepTimerJob?.cancel()
        setTimerJob?.cancel()
        _uiState.update { it.copy(isPreparing = true, prepSecondsLeft = 5, isResting = false) }
        
        prepTimerJob = viewModelScope.launch {
            for (i in 5 downTo 1) {
                _uiState.update { it.copy(prepSecondsLeft = i) }
                delay(1000)
            }
            // Prep complete -> Start Active Set
            beepAndVibrate() // Single beep for start
            _uiState.update { it.copy(isPreparing = false, prepSecondsLeft = 0) }
            startSetTimer()
        }
    }

    private fun startSetTimer() {
        setTimerJob?.cancel()
        _uiState.update { it.copy(setElapsedSeconds = 0) }
        setTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (!_uiState.value.isResting && !_uiState.value.isPreparing) {
                    _uiState.update { it.copy(setElapsedSeconds = it.setElapsedSeconds + 1) }
                }
            }
        }
    }

    fun onSetDone() {
        val state = _uiState.value
        if (state.isResting || state.isPreparing) return 

        playDoubleBeep()
        setTimerJob?.cancel()
        startRestTimer(state.restDurationSeconds)
    }

    fun onSkipRest() {
        restTimerJob?.cancel()
        _uiState.update { it.copy(isResting = false, restSecondsLeft = 0) }
        advanceSet()
    }

    private fun startRestTimer(seconds: Int) {
        _uiState.update { it.copy(isResting = true, restSecondsLeft = seconds, isPreparing = false) }
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                delay(1000)
                _uiState.update { it.copy(restSecondsLeft = i - 1) }
            }
            // Rest complete -> Prepare for next set
            _uiState.update { it.copy(isResting = false, restSecondsLeft = 0) }
            advanceSet()
        }
    }

    private fun advanceSet() {
        val state = _uiState.value
        val nextSet = state.currentSet + 1
        val currentExercise = state.currentExercise ?: return

        if (nextSet <= currentExercise.sets) {
            // Move to next set of current exercise
            _uiState.update { it.copy(currentSet = nextSet) }
            startPreparation()
        } else {
            // Move to next exercise
            val nextIndex = state.currentExerciseIndex + 1
            if (nextIndex < state.exercises.size) {
                val nextExercise = state.exercises[nextIndex]
                _uiState.update {
                    it.copy(
                        currentExerciseIndex = nextIndex,
                        currentSet = 1,
                        totalSets = nextExercise.sets
                    )
                }
                startPreparation()
            } else {
                // Workout complete!
                setTimerJob?.cancel()
                workoutTimerJob?.cancel()
                _uiState.update { it.copy(workoutComplete = true) }
                saveSession()
            }
        }
    }

    private fun saveSession() {
        viewModelScope.launch {
            val durationSeconds = (System.currentTimeMillis() - workoutStartTime) / 1000
            repository.saveWorkoutSession(
                WorkoutSession(
                    splitId = splitId,
                    dayIndex = dayIndex,
                    splitName = split.name,
                    dayLabel = workoutDay.label,
                    totalExercises = _uiState.value.exercises.size,
                    durationSeconds = durationSeconds
                )
            )
        }
    }

    private fun beepAndVibrate() {
        // Beep via ToneGenerator
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 80)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
        } catch (_: Exception) { }

        // Vibrate
        vibrate(100)
    }

    private fun playDoubleBeep() {
        viewModelScope.launch {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 80)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                vibrate(50)
                delay(300)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                vibrate(50)
            } catch (_: Exception) { }
        }
    }

    private fun vibrate(duration: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
            }
        } catch (_: Exception) { }
    }

    override fun onCleared() {
        super.onCleared()
        setTimerJob?.cancel()
        restTimerJob?.cancel()
        prepTimerJob?.cancel()
        workoutTimerJob?.cancel()
    }
}

fun Int.toMmSs(): String {
    val m = this / 60
    val s = this % 60
    return "%02d:%02d".format(m, s)
}
