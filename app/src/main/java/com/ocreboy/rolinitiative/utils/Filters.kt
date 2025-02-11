package com.ocreboy.rolinitiative.utils

import android.text.InputFilter
import com.ocreboy.rolinitiative.GlobalVariables
import com.ocreboy.rolinitiative.InputFilterMinMax
import com.ocreboy.rolinitiative.MyApplication

class Filters {

    companion object {
        fun numberBetweenZeroAndMax(): Array<InputFilter> {
            return arrayOf(
                InputFilter.LengthFilter(MyApplication.instance.maxCantDigitsInNumbers),
                InputFilterMinMax(
                    MyApplication.instance.minNumberInInputNumbers,
                    MyApplication.instance.maxNumberInInputNumbers
                )
            )
        }

        fun textNotEmptyToMax(): Array<InputFilter> {
            return arrayOf(
                InputFilter.LengthFilter(GlobalVariables.maxLengthInputName),
                NonEmptyInputFilter()
            )
        }
    }


}