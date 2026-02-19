package com.ocreboy.rolinitiative.popups

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Spinner
import com.google.android.gms.ads.AdView
import com.ocreboy.rolinitiative.utils.FrameColor
import com.ocreboy.rolinitiative.GlobalVariables
import com.ocreboy.rolinitiative.R
import com.ocreboy.rolinitiative.RollDice
import com.ocreboy.rolinitiative.ads.AdManager

class PopupDices(private val context: Context) {

    lateinit var buttonClose: ImageButton

    @SuppressLint("InflateParams")
    fun showPopupWindow(view: View) {
        val sharedPreferences = context.getSharedPreferences(GlobalVariables.dicePopupPrefs, Context.MODE_PRIVATE)
        val frameColor = FrameColor(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_dice_content, null)

        AdManager.loadBanner(
            popupView.findViewById<AdView>(R.id.adViewPopupDice)
        )

        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        val buttonGenerateNumber: Button = popupView.findViewById(R.id.buttonGenerateNumber)
        val textViewRandomNumber: TextView = popupView.findViewById(R.id.textViewRandomNumber)
        val textViewSumDices: TextView = popupView.findViewById(R.id.textViewSumDices)
        val spinnerMaxSizeDice: Spinner = popupView.findViewById(R.id.spinnerMaxSizeDice)
        val diceQuantity: TextView = popupView.findViewById(R.id.diceQuantity)
        val diceLogo: ImageView = popupView.findViewById(R.id.diceImageViewVector)
        val buttonReduceDices: Button = popupView.findViewById(R.id.buttonReduceDices)
        val buttonIncrementDices: Button = popupView.findViewById(R.id.buttonIncrementDices)

        val buttonClear: Button = popupView.findViewById(R.id.buttonClearGenerateNumber)

        buttonClose = popupView.findViewById(R.id.buttonDiceClose)

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, GlobalVariables.maxNumberMap.keys.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMaxSizeDice.adapter = adapter

        val savedQuantity = sharedPreferences.getInt("dice_quantity", 1)
        val savedMaxSizeDice = sharedPreferences.getString("max_size_dice", "d20")
        val savedRandomNumber = sharedPreferences.getString("random_number", "")
        val savedSumDices = sharedPreferences.getString("sum_dices", "")

        diceQuantity.text = savedQuantity.toString()
        val defaultPosition = adapter.getPosition(savedMaxSizeDice)
        spinnerMaxSizeDice.setSelection(defaultPosition)
        textViewRandomNumber.text = savedRandomNumber
        textViewSumDices.text = savedSumDices
        diceLogo.setImageResource(GlobalVariables.maxDicesIcons[savedMaxSizeDice]!!)

        frameColor.changeVectorColorDarkLightGray(diceLogo)
        frameColor.changeTextColor(textViewRandomNumber)

        if(textViewRandomNumber.text.isEmpty() && textViewSumDices.text.isEmpty()) {
            buttonClear.isEnabled = false
        }

        buttonClose.setOnClickListener {
            popupWindow.dismiss()
        }

        buttonClear.setOnClickListener{
            textViewRandomNumber.text = null
            textViewSumDices.text = null
            buttonClear.isEnabled = false
        }

        spinnerMaxSizeDice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedMaxSizeDice = parent.getItemAtPosition(position).toString()
                diceLogo.setImageResource(GlobalVariables.maxDicesIcons[selectedMaxSizeDice]!!)
                frameColor.changeVectorColorDarkLightGray(diceLogo)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        buttonReduceDices.setOnClickListener {
            var currentQuantity = diceQuantity.text.toString().toInt()
            if (currentQuantity > 1) {
                currentQuantity -= 1
                diceQuantity.text = currentQuantity.toString()
            }
        }

        buttonIncrementDices.setOnClickListener {
            var currentQuantity = diceQuantity.text.toString().toInt()
            if (currentQuantity < GlobalVariables.MAX_DICES_TO_THROW) {
                currentQuantity += 1
                diceQuantity.text = currentQuantity.toString()
            }
        }

        buttonGenerateNumber.setOnClickListener {
            val quantity = diceQuantity.text.toString().toInt()
            val maxSide = spinnerMaxSizeDice.selectedItem.toString()
            val sides: Int = GlobalVariables.maxNumberMap[maxSide]!!
            val rollDice = RollDice(sides, quantity)

            if(!buttonClear.isEnabled) {
                buttonClear.isEnabled = true
            }

            var textSize = 46f

            frameColor.changeTextColor(textViewRandomNumber)
            textViewSumDices.text = rollDice.dices.joinToString(" + ")
            textViewRandomNumber.text = rollDice.total.toString()

            if (rollDice.total.toString().length >= 3) {
                textSize = 32f
            }
            textViewRandomNumber.textSize = textSize

            with(sharedPreferences.edit()) {
                putInt("dice_quantity", quantity)
                putString("max_size_dice", maxSide)
                putString("random_number", rollDice.total.toString())
                putString("sum_dices", rollDice.dices.joinToString(" + "))
                apply()
            }
        }

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        popupView.setBackgroundResource(frameColor.getFrameColor())

        popupWindow.setOnDismissListener {
            val quantity = diceQuantity.text.toString().toInt()
            val maxSide = spinnerMaxSizeDice.selectedItem.toString()
            val randomNumber = textViewRandomNumber.text.toString()
            val sumDices = textViewSumDices.text.toString()

            with(sharedPreferences.edit()) {
                putInt("dice_quantity", quantity)
                putString("max_size_dice", maxSide)
                putString("random_number", randomNumber)
                putString("sum_dices", sumDices)
                apply()
            }
        }
    }

}
