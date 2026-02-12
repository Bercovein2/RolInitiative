package com.ocreboy.rolinitiative
import TutorialManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ocreboy.rolinitiative.adapter.CharacterAdapter
import com.ocreboy.rolinitiative.animations.ButtonAnimationHelper
import com.ocreboy.rolinitiative.language.LanguageManager
import com.ocreboy.rolinitiative.popups.PopupDices
import com.ocreboy.rolinitiative.popups.PopupFolders
import com.ocreboy.rolinitiative.popups.PopupGreetings
import com.ocreboy.rolinitiative.popups.PopupHideAllTimers
import com.ocreboy.rolinitiative.popups.PopupNotes
import com.ocreboy.rolinitiative.popups.PopupPatchNotes
import com.ocreboy.rolinitiative.popups.PopupSoundTimerEditor
import com.ocreboy.rolinitiative.popups.PopupTimer
import com.ocreboy.rolinitiative.sounds.SoundManager
import com.ocreboy.rolinitiative.utils.AppVersion
import com.ocreboy.rolinitiative.utils.Filters
import com.ocreboy.rolinitiative.utils.FrameColor
import com.ocreboy.rolinitiative.sounds.SoundPlayer
import com.ocreboy.rolinitiative.utils.TimerHelper
import com.ocreboy.rolinitiative.utils.TimerUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity()  {

    lateinit var characterAdapter: CharacterAdapter
    val characterList = mutableListOf<Character>()
    lateinit var recyclerView: RecyclerView
    private lateinit  var start: String
    private lateinit  var next: String

    lateinit var frameColor: FrameColor
    lateinit var turnCount : TurnCount

    private lateinit var editTextName: EditText
    private lateinit var editTextInitiative: EditText
    private lateinit var editTextArmorClass: EditText
    private lateinit var editTextLife: EditText
    private lateinit var buttonAdd: Button
    lateinit var buttonSort: Button
    private lateinit var buttonStartNext: Button
    private lateinit var buttonRollDice: ImageButton
    private lateinit var buttonRestart: Button
    private lateinit var editGameName : EditText
    private lateinit var buttonNotes: ImageButton
    private lateinit var turnCounter: TextView
    private lateinit var textRound: TextView
    private lateinit var footerMenu: TextView
    private lateinit var footerVersion: TextView

    private lateinit var lifeHeaderImageButton: ImageView
    private lateinit var armorHeaderImageButton: ImageView
    private lateinit var initiativeHeaderImageButton: ImageView
    private lateinit var lifeEditImageButton: ImageView
    private lateinit var armorEditImageButton: ImageView
    private lateinit var initiativeEditImageButton: ImageView

    lateinit var tutorialManager : TutorialManager

    private lateinit var buttonFolder: ImageButton

    private lateinit var menuHandler: MenuHandler

    //TIMER OPTIONS
    lateinit var itemTimer: View
    lateinit var buttonToggleTimer: ImageButton
    lateinit var timerHelper: TimerHelper
    private lateinit var timerTextView: TextView
    private var isTimerRunning = false
    private var timerDuration = 0L // Duración en milisegundos
    private var isTimerVisible = false
    private lateinit var buttonStartPause : ImageButton
    private lateinit var buttonHideAllTimers : ImageButton
    private lateinit var buttonPlayAllTimers : ImageButton
    private lateinit var buttonPauseAllTimers : ImageButton
    private lateinit var buttonStopAllTimers : ImageButton
    private lateinit var buttonChangeAllTimerSounds : ImageButton
    private lateinit var buttonStop : ImageButton
    lateinit var soundPlayer : SoundPlayer
    lateinit var buttonSound : ImageButton
//    var selectedSoundResource by Delegates.notNull<Int>()
    var selectedSoundResourceFile : File? = null
    var selectedSoundResourceUri : Uri? = null
    var selectedSoundResourceAllTimersFile : File? = null
    var selectedSoundResourceAllTimersUri : Uri? = null
    lateinit var soundManager :SoundManager
    lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
//    var filePickerCallback: ((File?) -> Unit)? = null
    var filePickerCallbackUri: ((Uri?) -> Unit)? = null
    //ANIMATIONS
    val animationHelper = ButtonAnimationHelper()

    //LENGUAJE
//    lateinit var languageManager : LanguageManager

    //image options
    lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    var imagePickerCallback: ((Uri?) -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val languageManager = GlobalVariables.languageManager
        languageManager.loadSavedLanguage(this)

        setContentView(R.layout.activity_main)

        soundManager = SoundManager(this)
//        soundManager.copySoundsToExternalStorage()
        soundManager.copySoundsToMediaStore()

        filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // Usar el Uri directamente en lugar de File
                    soundManager.saveFileToExternalStorage(uri)

                    // Ejecuta el callback con el Uri seleccionado
                    filePickerCallbackUri?.invoke(uri)
                } ?: run {
                    // Si no hay archivo seleccionado, pasa null al callback
                    filePickerCallbackUri?.invoke(null)
                }
            } else {
                // Si se cancela, pasa null al callback
                filePickerCallbackUri?.invoke(null)
            }
        }

        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri = result.data?.data
                    imagePickerCallback?.invoke(uri)
                } else {
                    imagePickerCallback?.invoke(null)
                }
            }

        // Encuentra el layout item_timer
        itemTimer = findViewById(R.id.item_timer)
        // Encuentra el botón que alterna la visibilidad
        buttonToggleTimer = findViewById(R.id.buttonToggleTimer)
        animationHelper.applyScaleAnimationWithoutBackground(buttonToggleTimer)

        timerTextView = findViewById(R.id.timerTextView)

        buttonStartPause = findViewById(R.id.buttonStartPause)
        animationHelper.applyScaleAnimationWithoutBackground(buttonStartPause)

        buttonStop = findViewById(R.id.buttonStop)
        animationHelper.applyScaleAnimationWithoutBackground(buttonStop)

        buttonSound = findViewById(R.id.buttonSound)
        animationHelper.applyScaleAnimationWithoutBackground(buttonSound)

        buttonPlayAllTimers = findViewById(R.id.playAllTimers)
        animationHelper.applyScaleAnimationWithoutBackground(buttonPlayAllTimers)

        buttonPauseAllTimers = findViewById(R.id.pauseAllTimers)
        animationHelper.applyScaleAnimationWithoutBackground(buttonPauseAllTimers)

        buttonStopAllTimers = findViewById(R.id.stopAllTimers)
        animationHelper.applyScaleAnimationWithoutBackground(buttonStopAllTimers)

        buttonChangeAllTimerSounds = findViewById(R.id.changeAllTimerSounds)
        animationHelper.applyScaleAnimationWithoutBackground(buttonChangeAllTimerSounds)


        buttonHideAllTimers = findViewById(R.id.hideAllTimers)
        animationHelper.applyScaleAnimationWithoutBackground(buttonHideAllTimers)

        //reproductor de sonido
        soundPlayer = SoundPlayer(this)

        start = getString(R.string.start)
        next = getString(R.string.next)

        // Configurar la barra de acción como la barra de herramientas
        setSupportActionBar(findViewById(R.id.toolbar))

//        // Inicializar SharedPreferences
//        GlobalVariables.sharedPreferences =
//            getSharedPreferences("app_preferences", MODE_PRIVATE)

        GlobalVariables.addBestiary = GlobalVariables.sharedPreferences.getBoolean("ADD_BESTIARY", false)

//----------------------------------------------------- LANGUAGE



        //-------------------------------------------------------------------------


        selectedSoundResourceAllTimersUri = soundManager.getFileByName(GlobalVariables.sharedPreferences.getString("ALL_TIMERS_SOUND_FILENAME", GlobalVariables.noSoundName).orEmpty())

        selectedSoundResourceUri = soundManager.getFileByName(GlobalVariables.sharedPreferences.getString("TIMER_SOUND_FILENAME", "").orEmpty())


        buttonSound.setOnClickListener {
            PopupSoundTimerEditor(this).showSoundSelectorPopup(it, selectedSoundResourceUri)
        }

        buttonChangeAllTimerSounds.setOnClickListener {
            PopupSoundTimerEditor(this).showSoundSelectorAllCharacterPopup(it, selectedSoundResourceAllTimersUri)
        }

        selectedSoundResourceUri?.let { soundPlayer.setSound(it) }

        fun playSelectedSound() {
            selectedSoundResourceUri?.let { soundPlayer.setSound(it) }
            soundPlayer.playSound()
        }

        // Inicializa timerHelper aquí
        val remainingTime = GlobalVariables.sharedPreferences.getLong("REMAINING_TIME", GlobalVariables.defaultTime)

        timerTextView.setText(TimerUtils.formatTimerFull(remainingTime/1000))
        timerDuration = remainingTime

        // Inicializar el temporizador con el tiempo cargado o el original
        timerHelper = TimerHelper(
            context = this,
            originalDuration = remainingTime,
            interval = 1000L,
            timerTextView = timerTextView,
            onFinishAction = {
                playSelectedSound()
                stopTimer()
                Toast.makeText(this, R.string.timer_finished, Toast.LENGTH_SHORT).show()
            }
        )

        saveTimerOnMemory()

        timerTextView.setOnClickListener {
            PopupTimer(this) { hours, minutes, seconds ->
                // Calcular la nueva duración del temporizador
                timerDuration = (hours * 60 * 60 * 1000 + minutes * 60 * 1000 + seconds * 1000).toLong()

                // Actualizar el texto del TextView
                timerTextView.text = TimerUtils.formatTimerFull(hours, minutes, seconds)

                // Crear el temporizador con la nueva duración
                if (!isTimerRunning) {
                    timerHelper = TimerHelper(this,
                        originalDuration = timerDuration,
                        interval = 1000L,
                        timerTextView = timerTextView,
                        onFinishAction = {
                            playSelectedSound()
                            stopTimer()
                            Toast.makeText(this, R.string.timer_finished, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }.show()
        }

        // Configurar botón para iniciar/pausar el temporizador
        buttonStartPause.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        buttonStop.setOnClickListener{
            stopTimer()
        }

        buttonHideAllTimers.setOnClickListener{
            PopupHideAllTimers(this) {
                characterAdapter.hideAllTimers()
                saveCharacterList()
            }
        }

        buttonPlayAllTimers.setOnClickListener{
            characterAdapter.playAllTimers()
            saveCharacterList()
        }
        buttonPauseAllTimers.setOnClickListener{
            characterAdapter.pauseAllTimers()
            saveCharacterList()
        }
        buttonStopAllTimers.setOnClickListener{
            characterAdapter.stopAllTimers()
            saveCharacterList()
        }


        // Inicialmente oculta el layout item_timer
        itemTimer.visibility = GlobalVariables.sharedPreferences.getInt("TIMER_VIEW", View.GONE)

        // Configura el click listener del botón
        buttonToggleTimer.setOnClickListener {
            isTimerVisible = !isTimerVisible
            // Alterna la visibilidad del layout
            itemTimer.visibility = if (isTimerVisible) View.VISIBLE else View.GONE

            GlobalVariables.sharedPreferences.edit().putInt("TIMER_VIEW", itemTimer.visibility).apply()
        }



        //-------------------------------------------------------------------------


        // Inicializar las vistas y el DrawerMenuHandler
        menuHandler = MenuHandler(
            this,
            findViewById(R.id.drawerLayout),
            findViewById(R.id.navigationView),
            findViewById(R.id.toolbar), // ID de tu toolbar
            R.string.navigation_drawer_open, // String de apertura del drawer
            R.string.navigation_drawer_close // String de cierre del drawer
        )

        // Inicializa las vistas
        editTextName = findViewById(R.id.editTextName)
        editTextInitiative = findViewById(R.id.editTextInitiative)
        editTextArmorClass = findViewById(R.id.editTextArmorClass)
        editTextLife = findViewById(R.id.editTextLife)
        buttonAdd = findViewById(R.id.buttonAdd)
        buttonSort = findViewById(R.id.buttonSort)
        buttonStartNext = findViewById(R.id.buttonStartNext)

        buttonRollDice = findViewById(R.id.buttonRollDice)
        animationHelper.applyScaleAnimationWithoutBackground(buttonRollDice)

        buttonRestart = findViewById(R.id.buttonRestart)
        editGameName = findViewById(R.id.editGameName)

        buttonNotes = findViewById(R.id.buttonNotes)
        animationHelper.applyScaleAnimationWithoutBackground(buttonNotes)

        turnCounter = findViewById(R.id.textTurn)
        textRound = findViewById(R.id.textRound)
        footerMenu = findViewById(R.id.footer_menu)
        footerVersion = findViewById(R.id.footer_version)

        buttonFolder = findViewById(R.id.buttonFolders)
        animationHelper.applyScaleAnimationWithoutBackground(buttonFolder)

        lifeHeaderImageButton = findViewById(R.id.headerLife)
        armorHeaderImageButton = findViewById(R.id.headerArmorClass)
        initiativeHeaderImageButton = findViewById(R.id.headerInitiative)
        lifeEditImageButton = findViewById(R.id.editIconLife)
        armorEditImageButton = findViewById(R.id.editIconArmorClass)
        initiativeEditImageButton = findViewById(R.id.editIconInitiative)

        val appVersion = AppVersion(this)
        footerVersion.text = appVersion.getAppVersionName()

        frameColor = FrameColor(this)
        turnCount = TurnCount()

        turnCounter.text = GlobalVariables.sharedPreferences.getString("TURN_COUNT", "0") ?: "0"

        val gameName = GlobalVariables.sharedPreferences.getString("gameName", "") ?: ""
        editGameName.setText(gameName)

        this.changeMainColorIcons()

        editTextInitiative.filters = Filters.numberBetweenZeroAndMax()
        editTextArmorClass.filters = Filters.numberBetweenZeroAndMax()
        editTextLife.filters = Filters.numberBetweenZeroAndMax()

        // Recuperar la posición actual del personaje seleccionado
        GlobalVariables.currentPosition = GlobalVariables.sharedPreferences.getInt("CURRENT_POSITION", GlobalVariables.initialPosition)

        // Recuperar el estado de la lista de personajes si está disponible
        val savedListJson = GlobalVariables.sharedPreferences.getString("CHARACTER_LIST", null)
        if (!savedListJson.isNullOrEmpty()) {
            val jsonArray = JSONArray(savedListJson)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val name = jsonObject.getString("name")
                val number = jsonObject.getInt("number")
                val armorClass = if (jsonObject.has("armorClass")) jsonObject.getInt("armorClass") else 0
                val armorTouch = if (jsonObject.has("armorTouch")) jsonObject.getString("armorTouch") else ""
                val armorFlatFooted = if (jsonObject.has("armorFlatFooted")) jsonObject.getString("armorFlatFooted") else ""
                val life = if (jsonObject.has("life")) jsonObject.getInt("life") else 0
                val isDead = if (jsonObject.has("isDead")) jsonObject.getBoolean("isDead") else false
                val hasActiveTimer = if (jsonObject.has("hasActiveTimer")) jsonObject.getBoolean("hasActiveTimer") else false
                val isPaused = if (jsonObject.has("isPaused")) jsonObject.getBoolean("isPaused") else false
                val timeLeftInSeconds = if (jsonObject.has("timeLeftInSeconds")) jsonObject.getInt("timeLeftInSeconds") else 0
                val originalTimer = if (jsonObject.has("originalTimer")) jsonObject.getInt("originalTimer") else 0

                val character = Character(name, number, armorClass, armorTouch, armorFlatFooted, false, life, isDead, hasActiveTimer, isPaused, timeLeftInSeconds, originalTimer)

                character.timerSoundName = if (jsonObject.has("timerSoundName")) jsonObject.getString("timerSoundName")
                                            else selectedSoundResourceAllTimersFile?.nameWithoutExtension

                // 🔵 NUEVO: cargar imagen si existe
                character.imageUri =
                    if (jsonObject.has("imageUri"))
                        jsonObject.getString("imageUri")
                    else
                        null

                characterList.add(character)
            }
        }

        recyclerView = findViewById(R.id.recyclerView)

        // Configura el RecyclerView
        characterAdapter = CharacterAdapter(characterList, this, this) { position ->
            // Eliminar el personaje de la lista
            characterList.removeAt(position - 1) // Restar 1 para obtener el índice correcto
            characterAdapter.notifyItemRemoved(position)
            characterAdapter.notifyItemRangeChanged(position, characterList.size)

            // Guardar el estado de la lista
            saveCharacterList()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = characterAdapter

        // Restaura el estado del botón Next y pinta el personaje seleccionado
        if (GlobalVariables.currentPosition != GlobalVariables.initialPosition) {
            characterList[GlobalVariables.currentPosition].isSelected = true
            characterAdapter.notifyItemChanged(GlobalVariables.currentPosition + 1)
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInputs()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        editTextName.addTextChangedListener(textWatcher)
        editTextInitiative.addTextChangedListener(textWatcher)
        editTextArmorClass.addTextChangedListener(textWatcher)

        validateInputs() // Initial check

        updateStartNextButton()
        updateRestartButton()
        updateButtonStateByCharacterQuantity(buttonSort, GlobalVariables.minQuantityToSort)

        editTextName.filters = Filters.textNotEmptyToMax()
        setupEditorActionListeners()

        editGameName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                // Dentro de tu actividad o fragmento
                val editText: EditText = findViewById(R.id.editGameName)
                val editor = GlobalVariables.sharedPreferences.edit()
                editor.putString("gameName", editText.text.toString())
                editor.apply()
            }

        })

//--------------------- LISTENERS ---------------------------
        editTextName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // No es necesario hacer nada aquí
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No es necesario hacer nada aquí
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Verifica si el texto ha cambiado
                // Puedes agregar lógica adicional aquí si es necesario
            }
        })

//--------------------- ADD ---------------------------
        buttonAdd.setOnClickListener {
            val name = editTextName.text.toString()
            val numberText = editTextInitiative.text.toString()
            val armorClassText = editTextArmorClass.text.toString()
            val lifeText = editTextLife.text.toString()

            if (name.isNotEmpty() && numberText.isNotEmpty()) {

                val life = if (lifeText.isNotEmpty()) lifeText.toInt() else 0

                val number = numberText.toInt()
                val armorClass = if (armorClassText.isNotEmpty()) armorClassText.toInt() else 0
                this.addCharacterToActualList(name, number,
                    armorClass, "", "",
                    false, life, null)
                characterAdapter.notifyDataSetChanged()
                editTextName.text.clear()
                editTextInitiative.text.clear()
                editTextArmorClass.text.clear()
                editTextLife.text.clear()

                // Guardar el estado de la lista
                saveCharacterList()

                // Actualizar el estado del botón después de agregar un nuevo elemento
                updateStartNextButton()
                updateButtonStateByCharacterQuantity(buttonSort, GlobalVariables.minQuantityToSort)

                editTextName.requestFocus()
            } else {
                Toast.makeText(this, R.string.enter_name_and_initiative, Toast.LENGTH_SHORT).show()
            }
        }


//--------------------- SORT ---------------------------
        buttonSort.setOnClickListener {

            var selectedCharacter: Character? = null
            if (GlobalVariables.currentPosition != GlobalVariables.initialPosition) {
                selectedCharacter = characterList[GlobalVariables.currentPosition]
            }

            characterList.sortByDescending { it.initiative }

            if (selectedCharacter != null) {
                val characterIndex = characterList.indexOf(selectedCharacter)

                if (characterIndex != GlobalVariables.currentPosition) {
                    GlobalVariables.currentPosition = characterIndex
                }
            }

            characterAdapter.notifyDataSetChanged()

            // Esconder el teclado
            hideKeyboardIfOpen()

            // Guardar el estado de la lista
            saveCharacterList()
            updateStartNextButton()

        }


//--------------------- START/NEXT ---------------------------
        buttonStartNext.setOnClickListener {
            // Esconder el teclado
            hideKeyboardIfOpen()

            if (characterList.isNotEmpty()) { // Verificar que haya elementos en la lista
                var allCharactersDead = true
                for (character in characterList) {
                    if (!character.isDead) {
                        allCharactersDead = false
                        break
                    }
                }

                if (allCharactersDead) {
                    // Todos los personajes están muertos, salir de la función
                    return@setOnClickListener
                }

                // Desseleccionar el personaje actual solo si ya se ha seleccionado uno antes
                if (GlobalVariables.currentPosition >= 0) {
                    characterList[GlobalVariables.currentPosition].isSelected = false
                    characterAdapter.notifyItemChanged(GlobalVariables.currentPosition + 1) // +1 por la cabecera
                }

                // Encontrar el siguiente personaje que no esté muerto
                var nextPosition = GlobalVariables.currentPosition
                do {
                    nextPosition = (nextPosition + 1) % characterList.size
                    if (nextPosition == 0) {
                        turnCount.updateTurnCount(turnCounter)
                    }
                } while (characterList[nextPosition].isDead)

                GlobalVariables.currentPosition = nextPosition

                // Seleccionar el siguiente personaje
                characterList[GlobalVariables.currentPosition].isSelected = true
                // Notificar al adaptador sobre el cambio
                characterAdapter.notifyItemChanged(GlobalVariables.currentPosition + 1) // +1 por la cabecera
                // Scrollear hasta donde está la posición en verde
                recyclerView.scrollToPosition(GlobalVariables.currentPosition + 1)

                updateStartNextButton()
                updateRestartButton()

            }

            // Guardar la posición actual del personaje seleccionado
            GlobalVariables.sharedPreferences.edit().putInt("CURRENT_POSITION", GlobalVariables.currentPosition).apply()
            GlobalVariables.sharedPreferences.edit().putString("BUTTON_STATE", buttonStartNext.text.toString()).apply()

        }

//--------------------- ROLL DICE BUTTON ---------------------------
        buttonRollDice.setOnClickListener {
            hideKeyboardIfOpen()
            PopupDices(this).showPopupWindow(it)
        }

//--------------------- ROLL DICE BUTTON ---------------------------

        buttonFolder.setOnClickListener {
            hideKeyboardIfOpen()
            PopupFolders(this).showPopupWindow()
        }

//--------------------- RESTART BUTTON ---------------------------
        buttonRestart.setOnClickListener {
            resetList()
            saveCharacterList()
            characterAdapter.notifyDataSetChanged()
            resetCurrentPosition()
            updateStartNextButton()
            updateButtonStateByCharacterQuantity(buttonSort, GlobalVariables.minQuantityToSort)
            turnCount.resetTurnCount(turnCounter)
        }

        buttonRestart.setOnClickListener {
            // Crear el AlertDialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirm Action")
            builder.setMessage("Are you sure you want to restart turns?\nIt will only restart the turn count, not stat changes")

            // Botón Accept
            builder.setPositiveButton("Accept") { dialog, _ ->
                // Realizar la acción de limpiar la lista

                resetList()
                saveCharacterList()
                characterAdapter.notifyDataSetChanged()
                resetCurrentPosition()
                updateStartNextButton()
                updateButtonStateByCharacterQuantity(buttonSort, GlobalVariables.minQuantityToSort)
                turnCount.resetTurnCount(turnCounter)
                updateRestartButton()
                dialog.dismiss() // Cerrar el diálogo
            }

            // Botón Cancel
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Cerrar el diálogo sin hacer nada
            }

            // Mostrar el AlertDialog
            val dialog = builder.create()
            dialog.show()
        }

        buttonNotes.setOnClickListener {
            PopupNotes(this).showPopupWindow()
        }

        findViewById<View>(android.R.id.content).rootView.post {
            Log.d("MainActivity", "Attempting to show popup")
            PopupPatchNotes(this).evaluateUpdate()
        }

        footerMenu.setOnLongClickListener() {
            PopupGreetings(this).showPopupWindow()
            menuHandler.closeDrawer()
            true
        }

        footerMenu.setOnClickListener {
            val url = getString(R.string.instagramURL)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            menuHandler.closeDrawer()
            startActivity(intent)
        }


        // Crear una instancia del TutorialManager y mostrar el tutorial
        tutorialManager = TutorialManager(this)
    }

    @SuppressLint("SetTextI18n")
    private fun startTimer() {

        if (timerDuration > 0) {
            // Si el temporizador ya fue creado y está pausado o detenido, simplemente reanudarlo

            // Si el temporizador está pausado, simplemente reanudarlo desde donde quedó
            timerHelper.start() // Llama al método start() de TimerHelper, que maneja la reanudación correctamente
            isTimerRunning = true
            buttonStartPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_timer_pause))
        } else {
            Toast.makeText(this, R.string.set_valid_timer_first, Toast.LENGTH_SHORT).show()
        }
    }

    fun saveTimerOnMemory(){
        GlobalVariables.sharedPreferences.edit().putLong("REMAINING_TIME", timerHelper.getRemainingTime()
            .or(0L)).apply()
    }

    @SuppressLint("SetTextI18n")
    private fun pauseTimer() {
        timerHelper.pause()
        isTimerRunning = false
        buttonStartPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_timer_play))
        saveTimerOnMemory()
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun stopTimer() {
        // Detener el temporizador
        timerHelper.stop()
        isTimerRunning = false

        // Restablecer el tiempo al original
        timerTextView.text = TimerUtils.formatTimerFull(
            (timerDuration / (60 * 60 * 1000)).toInt(),
            ((timerDuration / (60 * 1000)) % 60).toInt(),
            ((timerDuration / 1000) % 60).toInt()
        )

        // Cambiar el ícono del botón de play
        buttonStartPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_timer_play))
        saveTimerOnMemory()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Delegar el manejo de los clics en la ActionBar al DrawerMenuHandler
        return menuHandler.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    // ---------------- BUTTON START/NEXT LOGIC ---------------
    fun updateStartNextButton() {

        if (characterList.isEmpty() || (characterList.size < GlobalVariables.minQuantityToStart)) {
            setButtonStartDisabled(buttonStartNext)
        } else {
            setButtonStartEnabled(buttonStartNext)
        }

        if (buttonStartNext.text.toString() == start && GlobalVariables.currentPosition != GlobalVariables.initialPosition
            && characterList.isNotEmpty() && characterList.size >= GlobalVariables.minQuantityToStart) {
            buttonStartNext.isEnabled = true
            setButtonNext(buttonStartNext)
        }

        GlobalVariables.sharedPreferences.edit().putString("BUTTON_STATE", buttonStartNext.text.toString()).apply()

    }


    private fun setButtonStartEnabled(button: Button) {
        button.isEnabled = true
        button.text = start
        button.setBackgroundColor(Color.GREEN)
    }

    private fun setButtonStartDisabled(button: Button) {
        button.isEnabled = false
        button.text = start
        button.setBackgroundColor(Color.GRAY)
    }

    private fun setButtonNext(button: Button) {
        button.isEnabled = true
        button.text = next
        button.setBackgroundColor(Color.CYAN)
    }

    private fun updateRestartButton (){
        if (GlobalVariables.currentPosition != GlobalVariables.initialPosition) {
            turnCounter.visibility = View.VISIBLE
            textRound.visibility = View.VISIBLE
            buttonRestart.isEnabled =  true
        } else {
            turnCounter.visibility = View.INVISIBLE
            textRound.visibility = View.INVISIBLE
            buttonRestart.isEnabled =  false
        }
    }

    private fun resetCurrentPosition() {
        GlobalVariables.currentPosition = GlobalVariables.initialPosition
        GlobalVariables.sharedPreferences.edit().remove("CURRENT_POSITION").apply()
    }

    fun updateButtonStateByCharacterQuantity(button: Button, minCharacterQuantity: Int) {
        button.isEnabled = characterList.size > minCharacterQuantity
    }

    fun hideKeyboardIfOpen() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus
        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun resetList() {
        for (character in characterList) {
            character.isDead = false
            character.isSelected = false
        }
    }

    // Función para guardar el estado de la lista de personajes en SharedPreferences
    @SuppressLint("NotifyDataSetChanged")
    fun saveCharacterList() {
        val jsonArray = JSONArray()
        for (character in characterList) {
            val jsonObject = JSONObject()
            jsonObject.put("name", character.name)
            jsonObject.put("number", character.initiative)
            jsonObject.put("armorClass", character.armorClass)
            jsonObject.put("armorTouch", character.armorTouch)
            jsonObject.put("armorFlatFooted", character.armorFlatFooted)
            jsonObject.put("life", character.life)
            jsonObject.put("isSelected", character.isSelected)
            jsonObject.put("isDead", character.isDead)
            jsonObject.put("hasActiveTimer", character.hasActiveTimer)
            jsonObject.put("isPaused", character.isPaused)
            jsonObject.put("timeLeftInSeconds", character.timeLeftInSeconds)
            jsonObject.put("originalTimer", character.originalTimer)
            jsonObject.put("timerSoundName", character.timerSoundName)
            jsonObject.put("imageUri", character.imageUri)

            jsonArray.put(jsonObject)
        }
        GlobalVariables.sharedPreferences.edit().putString("CHARACTER_LIST", jsonArray.toString()).apply()
    }

    fun saveOneCharacterInListOnMemory(position: Int, character: Character) {
        // Obtener el JSON almacenado en SharedPreferences
        val savedListJson = GlobalVariables.sharedPreferences.getString("CHARACTER_LIST", null)

        if (!savedListJson.isNullOrEmpty()) {
            // Convertir la cadena JSON en un JSONArray
            val jsonArray = JSONArray(savedListJson)

            // Verificar que la posición esté dentro del rango del JSONArray
            if (position >= 0 && position < jsonArray.length()) {
                // Obtener el JSONObject en la posición indicada
                val jsonObject = jsonArray.getJSONObject(position)

                // Modificar el contenido del JSONObject con los nuevos datos
                jsonObject.put("name", character.name)
                jsonObject.put("number", character.initiative)
                jsonObject.put("armorClass", character.armorClass)
                jsonObject.put("armorTouch", character.armorTouch)
                jsonObject.put("armorFlatFooted", character.armorFlatFooted)
                jsonObject.put("life", character.life)
                jsonObject.put("isSelected", character.isSelected)
                jsonObject.put("isDead", character.isDead)
                jsonObject.put("hasActiveTimer", character.hasActiveTimer)
                jsonObject.put("isPaused", character.isPaused)
                jsonObject.put("timeLeftInSeconds", character.timeLeftInSeconds)
                jsonObject.put("originalTimer", character.originalTimer)
                jsonObject.put("timerSoundName", character.timerSoundName)
                jsonObject.put("imageUri", character.imageUri)

                // Guardar el JSONArray modificado de vuelta en SharedPreferences
                GlobalVariables.sharedPreferences.edit().putString("CHARACTER_LIST", jsonArray.toString()).apply()
            }
        }
    }

    fun saveCharacter(character: Character, position: Int) {
        if (characterList.isNotEmpty() && position > 0) {
            characterList[position-1].life = character.life
            characterList[position-1].name = character.name
            characterList[position-1].initiative = character.initiative
            characterList[position-1].armorClass = character.armorClass
            characterList[position-1].armorTouch = character.armorTouch
            characterList[position-1].armorFlatFooted = character.armorFlatFooted
            characterList[position-1].imageUri = character.imageUri
            characterAdapter.notifyItemChanged(position)
            this.saveCharacterList()
        }
    }

    private fun setupEditorActionListeners(
    ) {
        editTextName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                editTextArmorClass.requestFocus()
                true
            } else {
                false
            }
        }

        editTextArmorClass.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                editTextLife.requestFocus()
                true
            } else {
                false
            }
        }

        editTextLife.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                editTextInitiative.requestFocus()
                true
            } else {
                false
            }
        }

        editTextInitiative.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                buttonAdd.performClick()
                true
            } else {
                false
            }
        }
    }

    private fun clearPreferences(preferences: String) {
        val sharedPreferences = getSharedPreferences(preferences, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }

    fun validateInputs() {
        val isNameNotEmpty = editTextName.text.toString().trim().isNotEmpty()
        val isNumberNotEmpty = editTextInitiative.text.toString().trim().isNotEmpty()
        buttonAdd.isEnabled = isNameNotEmpty && isNumberNotEmpty
    }

    fun changeTheme(){
        // Guardar el estado de la lista antes de cambiar el tema
        saveCharacterList()

        // Guardar la posición actual del personaje seleccionado
        GlobalVariables.sharedPreferences.edit().putInt("CURRENT_POSITION", GlobalVariables.currentPosition).apply()
        GlobalVariables.sharedPreferences.edit().putString("BUTTON_STATE", buttonStartNext.text.toString()).apply()

        // Obtener el modo nocturno actual
        val currentNightMode = AppCompatDelegate.getDefaultNightMode()
        val bool: Boolean

        // Cambiar entre modos
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            bool = false
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            bool = true
        }

        GlobalVariables.sharedPreferences.edit().putBoolean("NIGHT_MODE", bool).apply()

        // Recargar la actividad para aplicar el nuevo tema
        recreate()
    }

    fun buttonClear (){
        // Crear el AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.confirm_action)
        builder.setMessage("Are you sure you want to clear the character list? \nRemember: it will erase only actual fight, not characters in folders")
        builder.setMessage(R.string.are_you_sure_to_clear_character_list)

        // Botón Accept
        builder.setPositiveButton(R.string.accept_buttons) { dialog, _ ->
            // Realizar la acción de limpiar la lista
            clearCharacterList()

            dialog.dismiss() // Cerrar el diálogo
            Toast.makeText(this, R.string.current_fight_cleaned, Toast.LENGTH_SHORT).show()
        }

        // Botón Cancel
        builder.setNegativeButton(R.string.cancel_buttons) { dialog, _ ->
            dialog.dismiss() // Cerrar el diálogo sin hacer nada
        }

        // Mostrar el AlertDialog
        val dialog = builder.create()
        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearCharacterList(){
        characterList.clear()
        characterAdapter.notifyDataSetChanged()
        resetCurrentPosition()
        updateStartNextButton()
        updateButtonStateByCharacterQuantity(buttonSort, GlobalVariables.minQuantityToSort)

        // Guardar el estado de la lista
        saveCharacterList()
        clearPreferences(GlobalVariables.dicePopupPrefs)
        turnCount.resetTurnCount(turnCounter)
    }

    fun addCharacterToActualList(name: String, number: Int, armorClass: Int,
                                 armorTouch: String, armorFlatFooted: String,
                                 isSelected: Boolean, life: Int, imageUri: String?){
        val newCharacter = Character(name, number,
            armorClass, armorTouch, armorFlatFooted,
            isSelected, life, false, false, false, 0, 0, false, null,
            null, imageUri)

        newCharacter.timerSoundName = selectedSoundResourceAllTimersFile?.nameWithoutExtension

        characterList.add(newCharacter)
    }

    fun changeMainColorIcons(){
        frameColor.changeVectorColorBlackWhite(lifeHeaderImageButton)
        frameColor.changeVectorColorBlackWhite(armorHeaderImageButton)
        frameColor.changeVectorColorBlackWhite(initiativeHeaderImageButton)
        frameColor.changeVectorColorDarkLightGray(lifeEditImageButton)
        frameColor.changeVectorColorDarkLightGray(armorEditImageButton)
        frameColor.changeVectorColorDarkLightGray(initiativeEditImageButton)
        frameColor.changeVectorColorBlackWhite(lifeHeaderImageButton)
    }

    fun playCharacterSound(character: Character) {
        val soundPlayer = SoundPlayer(MyApplication.context)
        val soundFile = character.timerSoundName?.let { soundManager.getFileByName(it) }

        if (soundFile != null) {
            soundPlayer.setSound(soundFile)
            soundPlayer.playSound()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SoundManager.REQUEST_DELETE_PERMISSION) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("SoundManager", "Permiso concedido, archivo eliminado.")
            } else {
                Log.e("SoundManager", "Permiso denegado, no se eliminó el archivo.")
            }
        }
    }

    fun openImagePicker(onImageSelected: (Uri?) -> Unit) {
        imagePickerCallback = onImageSelected

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        imagePickerLauncher.launch(intent)
    }
    fun pickAndPreviewImage(
        imageView: ImageView,
        onImagePicked: (String) -> Unit
    ) {
        openImagePicker { uri ->
            if (uri == null) return@openImagePicker

            // Guardar permiso
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            // Mostrar preview
            imageView.visibility = View.VISIBLE
            imageView.load(uri) {
                crossfade(true)
            }

            // Devolver uri como String para guardar
            onImagePicked(uri.toString())
        }
    }

}