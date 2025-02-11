package com.ocreboy.rolinitiative.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ocreboy.rolinitiative.Character
import com.ocreboy.rolinitiative.GlobalVariables
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.popups.PopupEdit
import com.ocreboy.rolinitiative.popups.PopupEditArmor
import com.ocreboy.rolinitiative.popups.PopupEditInitiative
import com.ocreboy.rolinitiative.popups.PopupEditLife
import com.ocreboy.rolinitiative.popups.PopupSaveCharacter
import com.ocreboy.rolinitiative.popups.PopupSoundTimerEditor
import com.ocreboy.rolinitiative.utils.TimerUtils

class CharacterAdapter(
    private val characterList: MutableList<Character>,
    private val mainActivity: MainActivity,
    private val context: Context,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1
    private val handler = Handler(Looper.getMainLooper())

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.visibility = View.GONE
            itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
        }
    }

    inner class CharacterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var timerTextView: TextView = itemView.findViewById(R.id.characterTimer)
        val textViewName: TextView = itemView.findViewById(R.id.characterName)
        val textViewInitiative: TextView = itemView.findViewById(R.id.characterInitiative)
        val textViewArmor: TextView = itemView.findViewById(R.id.characterArmorClass)
        val textViewLife: TextView = itemView.findViewById(R.id.characterLife)

        @RequiresApi(Build.VERSION_CODES.R)
        fun bind(character: Character) {
            textViewName.text = character.name
            textViewInitiative.text = character.initiative.toString()
            textViewArmor.text = character.armorClass.toString()
            textViewLife.text = character.life.toString()

            deadOrAlive(character, textViewName)

            // Formatear el tiempo desde el modelo y actualizar el TextView
            if(character.timeLeftInSeconds > 0) {
                timerTextView.text = TimerUtils.formatTimerFull(character.timeLeftInSeconds)
            } else {
                timerTextView.text = TimerUtils.getZeroFormat()
            }

            // Configurar el menú popup para la pulsación prolongada en el TextView
            textViewName.setOnLongClickListener { view ->
                showPopupMenu(view, adapterPosition, textViewName, timerTextView)
                true
            }

            timerTextView.setOnLongClickListener {
                showEditTimerDialog(character, timerTextView)
                true
            }

            // Control visibility of characterTimer
            if (character.hasActiveTimer) {
                timerTextView.visibility = View.VISIBLE
                if (character.isTimerRunning) {
                    character.startTimer(onTick = { millisUntilFinished ->
                        timerTextView.text = TimerUtils.format02d(
                            TimerUtils.getSecondsLeft(millisUntilFinished).toInt()
                        )
                    }, onFinish = {
                        mainActivity.playCharacterSound(character)
                        timerTextView.text = TimerUtils.getZeroFormat()
                    })
                }
            } else {
                timerTextView.visibility = View.INVISIBLE
                timerTextView.text = TimerUtils.getZeroFormat()
            }

            // Configurar el menú popup para la pulsación prolongada en el TextView
            textViewLife.setOnLongClickListener {
                showPopupEditLife(adapterPosition, textViewName, character)
                true
            }

            // Configurar el menú popup para la pulsación prolongada en el TextView
            textViewArmor.setOnLongClickListener {
                showPopupEditArmor(adapterPosition, textViewName, character)
                true
            }

            // Configurar el menú popup para la pulsación prolongada en el TextView
            textViewInitiative.setOnLongClickListener {
                showPopupEditInitiative(adapterPosition, textViewName, character)
                true
            }
        }
    }

    private fun showPopupEditLife(adapterPosition: Int, textViewName: TextView, character: Character) {
        val popupEdit = PopupEditLife(textViewName.context)
        popupEdit.showPopupWindow(textViewName, character, mainActivity, adapterPosition)
    }

    private fun showPopupEditArmor(adapterPosition: Int, textViewName: TextView, character: Character) {
        val popupEdit = PopupEditArmor(textViewName.context)
        popupEdit.showPopupWindow(textViewName, character, mainActivity, adapterPosition)
    }
    private fun showPopupEditInitiative(adapterPosition: Int, textViewName: TextView, character: Character) {
        val popupEdit = PopupEditInitiative(textViewName.context)
        popupEdit.showPopupWindow(textViewName, character, mainActivity, adapterPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_header_list, parent, false)
            HeaderViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_character, parent, false)
            CharacterViewHolder(itemView)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CharacterViewHolder) {
            val character = characterList[position - 1] // Restar 1 para obtener el elemento correcto

            var color = Color.TRANSPARENT
            holder.bind(character)
            if (character.isDead) {
                color = ContextCompat.getColor(context, R.color.darkRed)
            }  else if (character.isSelected){
                color = ContextCompat.getColor(context, R.color.darkGreen)
                holder.bind(character)
            }

            holder.textViewName.setBackgroundColor(color)

            // Reiniciar temporizador si está activo
            if (character.hasActiveTimer) {
                if(character.isTimerRunning) {
                    character.startTimer(
                        onTick = { millisUntilFinished ->
                            val secondsLeft = TimerUtils.getSecondsLeft(millisUntilFinished)
                            val hours = TimerUtils.getHours(secondsLeft)
                            val minutes = TimerUtils.getMinutes(secondsLeft)
                            val seconds = TimerUtils.getSeconds(secondsLeft)

                            holder.timerTextView.setText(
                                TimerUtils.formatTimerFull(
                                    hours,
                                    minutes,
                                    seconds
                                )
                            )
                        },
                        onFinish = {
                            mainActivity.playCharacterSound(character)
                            holder.timerTextView.setText(TimerUtils.getZeroFormat())
                        }
                    )
                }
            } else {
                holder.timerTextView.setText(TimerUtils.getZeroFormat())
            }
        }
    }

    override fun getItemCount(): Int {
        return characterList.size + 1 // Sumamos 1 para incluir el encabezado
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_HEADER
        } else {
            TYPE_ITEM
        }
    }

    // Función para mostrar el menú popup
    private fun showPopupMenu(view: View, position: Int, textViewName: TextView, timerTextView: TextView) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.character_options_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            handleMenuItemClick(menuItem, position, textViewName, timerTextView)
            true
        }

        popupMenu.show()
        // Find the "Delete" menu item and change its text color to red
        val menuItemDelete = popupMenu.menu.findItem(R.id.option_delete)
        val s = SpannableString(menuItemDelete.title)
        s.setSpan(ForegroundColorSpan(Color.RED), 0, s.length, 0)
        menuItemDelete.title = s
    }


    // Función para manejar la selección de elementos del menú popup
    private fun handleMenuItemClick(menuItem: MenuItem, position: Int, textViewName: TextView, timerTextView: TextView): Boolean {
        val character = characterList[position - 1]
        return when (menuItem.itemId) {
            R.id.option_edit -> {
                val popupEdit = PopupEdit(textViewName.context)
                popupEdit.showPopupWindow(textViewName, character, mainActivity, position)
                true
            }

            R.id.option_save -> {
                val popupEdit = PopupSaveCharacter(textViewName.context)
                popupEdit.showPopupWindow(character)
                true
            }

            R.id.option_kill -> {
                character.isDead = !character.isDead
                deadOrAlive(character,  textViewName)
                true
            }

            R.id.option_delete -> {
                removeCharacter(position)
                true
            }

            R.id.option_timer -> {
                alternateCharacterTimer(position, timerTextView)
                true
            }
            else -> false
        }
    }

    //función para alternar timer
    private fun alternateCharacterTimer(position: Int, timerTextView: TextView) {
        try {
            val character = characterList[position - 1]
            if (character.hasActiveTimer) {
                // Detener el temporizador y reiniciar al tiempo original
                character.hasActiveTimer = false
                timerTextView.visibility = View.INVISIBLE
            } else {
                // Iniciar el temporizador
                character.hasActiveTimer = true
                timerTextView.visibility = View.VISIBLE
                timerTextView.text = TimerUtils.formatTimerFull(character.timeLeftInSeconds)
            }
            mainActivity.saveOneCharacterInListOnMemory(position - 1, character)
        } catch (e: Exception) {
            return
        }
    }


    // Función para eliminar un personaje de la lista
    private fun removeCharacter(position: Int) {

        if (position <= GlobalVariables.currentPosition + 1) {
            GlobalVariables.currentPosition -= 1
            GlobalVariables.sharedPreferences.edit()
                .putInt("CURRENT_POSITION", GlobalVariables.currentPosition).apply()
        }

        characterList.removeAt(position - 1)

        if(characterList.isEmpty()) {
            mainActivity.buttonClear()
        }

        notifyItemRemoved(position) // Sumamos 1 porque el encabezado ocupa la primera posición
    }

    private fun deadOrAlive(character: Character, textViewName: TextView) {
        var backgroundNameColor = Color.TRANSPARENT
        if (character.isDead) {
            backgroundNameColor = ContextCompat.getColor(context, R.color.darkRed)
        }
        textViewName.setBackgroundColor(backgroundNameColor)
        mainActivity.saveCharacterList()
    }


    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("DefaultLocale", "NotifyDataSetChanged")
    fun showEditTimerDialog(character: Character, timerTextView: TextView) {
        val dialogView = LayoutInflater.from(mainActivity).inflate(R.layout.popup_edit_timer_character, null)
        val hoursEditText = dialogView.findViewById<EditText>(R.id.editHoursCharacter)
        val minutesEditText = dialogView.findViewById<EditText>(R.id.editMinutesCharacter)
        val secondsEditText = dialogView.findViewById<EditText>(R.id.editSecondsCharacter)
        val playButton = dialogView.findViewById<ImageButton>(R.id.buttonPlay)
        val stopButton = dialogView.findViewById<ImageButton>(R.id.buttonStop)
        val soundButton = dialogView.findViewById<ImageButton>(R.id.buttonSound)

        mainActivity.animationHelper.applyScaleAnimation(playButton)
        mainActivity.animationHelper.applyScaleAnimation(stopButton)
        mainActivity.animationHelper.applyScaleAnimation(soundButton)

        // Get the current time from the TextView
        val timerText = timerTextView.text.toString()
        val timeParts = timerText.split(":").map { it.toIntOrNull() ?: 0 }

        // Prefill the EditTexts with the current time
        val hours = if (timeParts.size > 0) timeParts[0] else 0
        val minutes = if (timeParts.size > 1) timeParts[1] else 0
        val seconds = if (timeParts.size > 2) timeParts[2] else 0

        hoursEditText.setText(TimerUtils.formatTimer60(hours))
        minutesEditText.setText(TimerUtils.formatTimer60(minutes))
        secondsEditText.setText(TimerUtils.formatTimer60(seconds))

        // Limitar a 2 caracteres en cada campo
        hoursEditText.filters = arrayOf(InputFilter.LengthFilter(2))
        minutesEditText.filters = arrayOf(InputFilter.LengthFilter(2))
        secondsEditText.filters = arrayOf(InputFilter.LengthFilter(2))

        val alertDialog = AlertDialog.Builder(mainActivity)
            .setView(dialogView)
            .setOnDismissListener  {
                try {
                    val newHours = hoursEditText.text.toString().toIntOrNull() ?: 0
                    val newMinutes = minutesEditText.text.toString().toIntOrNull() ?: 0
                    val newSeconds = secondsEditText.text.toString().toIntOrNull() ?: 0

                    val totalSeconds = TimerUtils.getSecondsFull(newHours, newMinutes, newSeconds)
                    character.timeLeftInSeconds = totalSeconds

                    if(!character.isTimerRunning && !character.isPaused) {
                        character.originalTimer = totalSeconds
                    }
                    timerTextView.text = TimerUtils.formatTimerFull(newHours, newMinutes, newSeconds)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .create()

        if(character.isTimerRunning && !character.isPaused) {
            playButton.setImageResource(R.drawable.ic_timer_pause)
        } else {
            playButton.setImageResource(R.drawable.ic_timer_play)
        }

        playButton.setOnClickListener {
            //si el timer no está corriendo (PLAY)
            if(!character.isTimerRunning || character.isPaused) {

                val totalSeconds = (hoursEditText.text.toString().toIntOrNull() ?: 0) * 3600 +
                        (minutesEditText.text.toString().toIntOrNull() ?: 0) * 60 +
                        (secondsEditText.text.toString().toIntOrNull() ?: 0)

                character.timeLeftInSeconds = totalSeconds

                character.startTimer(onTick = {
                    timerTextView.text = TimerUtils.formatTimerFull(totalSeconds)
                    notifyDataSetChanged()
                }, onFinish = {
                    timerTextView.text = TimerUtils.getZeroFormat()
                    playButton.setImageResource(R.drawable.ic_timer_play)
                    mainActivity.playCharacterSound(character)
                    notifyDataSetChanged()
                })

                if (character.isTimerRunning && !character.isPaused) {
                    playButton.setImageResource(R.drawable.ic_timer_pause)
                }

                alertDialog.dismiss() // Close the dialog

                Toast.makeText(context, R.string.session_timer_started, Toast.LENGTH_SHORT).show()

            } else { // en caso de Pause

                character.pauseTimer()
                playButton.setImageResource(R.drawable.ic_timer_play)
                Toast.makeText(context, R.string.session_timer_paused, Toast.LENGTH_SHORT).show()
            }
            mainActivity.saveCharacterList()
        }

        //en caso de stop
        stopButton.setOnClickListener {

            character.stopTimer()

            val hours = TimerUtils.getHours(character.originalTimer)
            val minutes = TimerUtils.getMinutes(character.originalTimer)
            val seconds = TimerUtils.getSeconds(character.originalTimer)

            hoursEditText.setText(TimerUtils.formatTimer60(hours))
            minutesEditText.setText(TimerUtils.formatTimer60(minutes))
            secondsEditText.setText(TimerUtils.formatTimer60(seconds))

            timerTextView.text = TimerUtils.getZeroFormat()
            playButton.setImageResource(R.drawable.ic_timer_play)

            mainActivity.saveCharacterList()
            Toast.makeText(context, R.string.session_timer_stoped, Toast.LENGTH_SHORT).show()

        }

        soundButton.setOnClickListener {
            PopupSoundTimerEditor(mainActivity).showSoundSelectorCharacterPopup(it, character)
        }

        alertDialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun hideAllTimers() {
        // Recorremos cada Character en la lista y cambiamos su propiedad hasActiveTimer a false
        characterList.forEach { character ->
            character.stopTimer()
            character.hasActiveTimer = false
            character.countDownTimer?.cancel()
            character.countDownTimer = null
        }

        // Notificamos que los datos han cambiado para actualizar la UI
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun stopAllTimers() {
        characterList.forEach { character ->
            handler.removeCallbacksAndMessages(null)
            character.stopTimer()
            notifyDataSetChanged()
        }
        Toast.makeText(context, R.string.all_character_timer_stopped, Toast.LENGTH_SHORT).show()

    }

    @SuppressLint("NotifyDataSetChanged")
    fun pauseAllTimers() {
        characterList.forEach { character ->
            handler.removeCallbacksAndMessages(null)
            character.pauseTimer()
        }
        Toast.makeText(context, R.string.all_character_timer_paused, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun playAllTimers() {
        characterList.forEachIndexed { index, character ->
            if (character.hasActiveTimer && !character.isTimerRunning) {
                character.startTimer(onTick = {
                    notifyDataSetChanged()
                }, onFinish = {
                    if(character.isTimerRunning) {
                        mainActivity.playCharacterSound(character)
                        notifyDataSetChanged()
                    }
                })
            }
        }
        Toast.makeText(context, R.string.all_character_timer_started, Toast.LENGTH_SHORT).show()

    }

    fun areThereAnyCharacterWithTimer():Boolean{
        return characterList.any { it.hasActiveTimer}
    }
}