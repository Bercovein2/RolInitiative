import android.content.Context
import android.content.SharedPreferences
import com.ocreboy.rolinitiative.MainActivity
import com.ocreboy.rolinitiative.R
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

class TutorialManager(private val activity: MainActivity) {

    private val PREFS_NAME = "TutorialPrefs"
    private val KEY_TUTORIAL_SHOWN = "TutorialShown"

    private fun setTutorialShown(shown: Boolean) {
        val sharedPreferences: SharedPreferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(KEY_TUTORIAL_SHOWN, shown)
            apply()
        }
    }

    fun showCharacterInputsTutorial() {
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(activity.findViewById(R.id.buttonAdd))
            .setPrimaryText(R.string.add_button)
            .setSecondaryText(R.string.add_button_desc)
            .setFocalColour(activity.frameColor.getFrameColor())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                    showHoldTapTutorial()
                }
            }
            .show()
    }

    fun showHoldTapTutorial() {
        // Obtener el tamaño de la pantalla para colocar el prompt en el centro
        val displayMetrics = activity.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(screenWidth / 2f, screenHeight / 2f)
            .setPrimaryText(R.string.hold_tap)
            .setSecondaryText(R.string.hold_tap_desc)
            .setFocalColour(activity.frameColor.getFrameColor())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                    showSortButtonTutorial()
                }
            }
            .show()
    }


    fun showSortButtonTutorial() {
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(activity.findViewById(R.id.buttonSort))
            .setPrimaryText(R.string.sort_button)
            .setSecondaryText(R.string.sort_button_desc)
            .setFocalColour(activity.frameColor.getFrameColor())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                    showStartButtonTutorial()
                }
            }
            .show()
    }

    fun showStartButtonTutorial() {
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(activity.findViewById(R.id.buttonStartNext))
            .setPrimaryText(R.string.start_next_button)
            .setSecondaryText(R.string.start_next_button_desc)
            .setFocalColour(activity.frameColor.getFrameColor())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                    showReStartButtonTutorial()
                }
            }
            .show()
    }

    fun showReStartButtonTutorial() {
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(activity.findViewById(R.id.buttonRestart))
            .setPrimaryText(R.string.restart_button)
            .setSecondaryText(R.string.restart_button_desc)
            .setFocalColour(activity.frameColor.getFrameColor())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                    showFolderButtonTutorial()
                }
            }
            .show()
    }

    fun showFolderButtonTutorial() {
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(activity.findViewById(R.id.buttonFolders))
            .setPrimaryText(R.string.folder_button)
            .setSecondaryText(R.string.folder_button_desc)
            .setFocalColour(activity.frameColor.getFrameColor())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                    showNotesButtonTutorial()
                }
            }
            .show()
    }

    fun showNotesButtonTutorial() {
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(activity.findViewById(R.id.buttonNotes))
            .setPrimaryText(R.string.note_button)
            .setSecondaryText(R.string.note_button_desc)
            .setFocalColour(activity.frameColor.getFrameColor())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                    showDicesButtonTutorial()
                }
            }
            .show()
    }

    fun showDicesButtonTutorial() {
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(activity.findViewById(R.id.buttonRollDice))
            .setPrimaryText(R.string.dice_button)
            .setSecondaryText(R.string.dice_button_desc)
            .setFocalColour(activity.frameColor.getFrameColor())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                    showTimerButtonTutorial()
                }
            }
            .show()
    }

    fun showTimerButtonTutorial() {
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(activity.findViewById(R.id.buttonToggleTimer))
            .setPrimaryText(R.string.timer_button)
            .setSecondaryText(R.string.timer_button_desc)
            .setFocalColour(activity.frameColor.getFrameColor())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                    showMenuButtonTutorial()
                }
            }
            .show()
    }

    fun showMenuButtonTutorial() {
        // Obtener el tamaño de la pantalla para colocar el prompt en el centro
        val displayMetrics = activity.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(screenWidth / 2f, screenHeight / 2f)
            .setPrimaryText(R.string.side_menu)
            .setSecondaryText(R.string.side_menu_desc)
            .setFocalColour(activity.frameColor.getFrameColor())
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED || state == MaterialTapTargetPrompt.STATE_FINISHED) {
                    setTutorialShown(true)
                }
            }
            .show()
    }

}
