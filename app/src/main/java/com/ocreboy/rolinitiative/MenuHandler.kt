package com.ocreboy.rolinitiative

import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.ocreboy.rolinitiative.model.Campaign
import com.ocreboy.rolinitiative.popups.PopupCampaigns
import com.ocreboy.rolinitiative.popups.PopupLanguageSelector
import com.ocreboy.rolinitiative.popups.PopupInfo
import com.ocreboy.rolinitiative.popups.PopupPatchNotes
import com.ocreboy.rolinitiative.popups.PopupSaveCharacter
import com.ocreboy.rolinitiative.popups.PopupSuggestions
import com.ocreboy.rolinitiative.utils.FrameColor

class MenuHandler(
    private val mainActivity: MainActivity,
    private val drawerLayout: DrawerLayout,
    private val navigationView: NavigationView,
    private val toolbarId: Toolbar, // ID de tu toolbar
    private val openDrawerContentDescRes: Int, // String de apertura del drawer
    private val closeDrawerContentDescRes: Int // String de cierre del drawer
) {

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var frameColor: FrameColor

    init {
        setupDrawer()
    }

    private fun setupDrawer() {
        // Configurar el toggle para el ActionBarDrawerToggle
        drawerToggle = ActionBarDrawerToggle(
            mainActivity,
            drawerLayout,
            toolbarId,
            openDrawerContentDescRes,
            closeDrawerContentDescRes
        )

        frameColor = FrameColor(mainActivity)
        // Vincular el drawerToggle con el DrawerLayout
        drawerLayout.addDrawerListener(drawerToggle)
        this.syncState()

        val drawable = DrawerArrowDrawable(mainActivity)
        drawable.color = ContextCompat.getColor(mainActivity, R.color.white)
        drawerToggle.drawerArrowDrawable = drawable

        frameColor.changeThemeToogleIcon(navigationView.menu.findItem(R.id.buttonToggleTheme))

        // Manejar clics en los elementos del NavigationView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Acciones según el item seleccionado
            when (menuItem.itemId) {
                R.id.buttonToggleTheme -> {
                    mainActivity.changeTheme()
                    true
                }
                R.id.buttonInfo -> {
                    val popupInfo = PopupInfo(mainActivity)
                    popupInfo.showPopupWindow()
                    closeDrawer()
                    mainActivity.hideKeyboardIfOpen()
                    true
                }
                R.id.buttonTutorial -> {
                closeDrawer()
                mainActivity.hideKeyboardIfOpen()
                mainActivity.tutorialManager.showCharacterInputsTutorial()
                true
                }
                R.id.buttonClear -> {
                    mainActivity.buttonClear()
                    closeDrawer()
                    true
                }
                R.id.buttonAddListToFolder -> {
                    val popup = PopupSaveCharacter(mainActivity)
                    popup.showPopupWindowToAddAll(mainActivity.characterList)
                    closeDrawer()
                    true
                }
                R.id.buttonPatchNotes -> {
                    val popupPatchNotes = PopupPatchNotes(mainActivity)
                    popupPatchNotes.showPopupWindow()
                    closeDrawer()
                    mainActivity.hideKeyboardIfOpen()
                    true
                }
                R.id.buttonRateUs -> {
                    val url = mainActivity.getString(R.string.googlePlayStoreURL)
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    mainActivity.startActivity(intent)
                    closeDrawer()
                    true
                }
                R.id.buttonSuggestAnIdea -> {

                    val popup = PopupSuggestions(mainActivity)
                    popup.showPopupWindow()
                    closeDrawer()
                    mainActivity.hideKeyboardIfOpen()
                    true
                }
                R.id.buttonChangeLanguage -> {
                    PopupLanguageSelector(mainActivity).show()
                    closeDrawer()
                    mainActivity.hideKeyboardIfOpen()
                    true
                }
                R.id.buttonChangeCampaign -> {
                    val popup = PopupCampaigns(
                        mainActivity = mainActivity,
                        campaigns = mutableListOf(
                            Campaign(1, "Curse of Strahd", null),
                            Campaign(2, "Lost Mine of Phandelver", null),
                            Campaign(3, "Homebrew Campaign", null)
                        ),
                        onCampaignSelected = { campaign ->
                            // TODO
                        }
                    )

                    popup.show(mainActivity.window.decorView)
                    closeDrawer()
                    mainActivity.hideKeyboardIfOpen()
                    true
                }
                else -> false
            }
        }
    }

    fun syncState() {
        drawerToggle.syncState()
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (drawerToggle.onOptionsItemSelected(item)) {
            openDrawer()
            true
        } else {
            closeDrawer()
            // Handle other ActionBar item clicks if needed
            false
        }
    }

    fun openDrawer() {
        drawerLayout.openDrawer(navigationView)
    }

    fun closeDrawer() {
        drawerLayout.closeDrawer(navigationView)
    }

}
