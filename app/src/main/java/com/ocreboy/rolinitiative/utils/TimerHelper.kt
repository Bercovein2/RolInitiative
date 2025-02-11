package com.ocreboy.rolinitiative.utils

import android.os.CountDownTimer
import android.widget.TextView
import com.ocreboy.rolinitiative.MainActivity
import androidx.lifecycle.MutableLiveData
class TimerHelper(
    private val context: MainActivity, // Para poder usar SharedPreferences
    private val originalDuration: Long, // Duración total del temporizador
    private val interval: Long, // Intervalo de actualización
    private val timerTextView: TextView, // TextView para mostrar el tiempo
    private val onFinishAction: (() -> Unit)? = null // Acción al finalizar el temporizador
) {
    private var countDownTimer: CountDownTimer? = null
    private var remainingTime: Long = originalDuration // Tiempo restante del temporizador
    private var isPaused: Boolean = false

    // LiveData para observar el estado del temporizador
    val timerFinishedLiveData = MutableLiveData<Boolean>()

    fun start() {
        // Si el temporizador está pausado, se reanuda desde donde se detuvo
        if (countDownTimer == null || isPaused) {
            createTimer(remainingTime) // Crear el temporizador con el tiempo restante
            isPaused = false
            countDownTimer?.start()
        }
    }

    fun pause() {
        countDownTimer?.cancel()
        isPaused = true
        context.saveTimerOnMemory() // Guardar el tiempo restante al pausar
    }

    fun stop() {
        countDownTimer?.cancel()
        remainingTime = originalDuration // Reiniciar el tiempo restante al tiempo original
        isPaused = false
        countDownTimer = null // Limpiar el temporizador
        timerTextView.text = TimerUtils.getZeroFormat() // Resetear la vista al tiempo inicial
        context.saveTimerOnMemory() // Guardar el tiempo original al detener el temporizador
    }

    private fun createTimer(time: Long) {
        countDownTimer = object : CountDownTimer(time, interval) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished

                // Formatear el tiempo restante como hh:mm:ss
                val timeFormatted = String.format("%02d:%02d:%02d", getHours(), getMinutes(), getSeconds())

                timerTextView.text = timeFormatted
                context.saveTimerOnMemory()
            }

            override fun onFinish() {
                remainingTime = 0
                timerTextView.text = TimerUtils.getZeroFormat()
                timerFinishedLiveData.postValue(true) // Emitir evento de finalización
                onFinishAction?.invoke()
                context.saveTimerOnMemory() // Guardar el tiempo final al terminar
            }
        }
    }

    // Métodos para obtener las horas, minutos y segundos por separado
    fun getHours(): Long {
        return remainingTime / (1000 * 60 * 60)
    }
    fun getMinutes(): Long {
        return (remainingTime / (1000 * 60)) % 60
    }
    fun getSeconds(): Long {
        return (remainingTime / 1000) % 60
    }

    fun getRemainingTime():Long{
        return remainingTime
    }


}
