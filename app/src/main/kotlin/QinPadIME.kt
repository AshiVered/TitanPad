package us.chronovir.qinpad

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager

class QinPadIME : InputMethodService() {
    private var currentType = InputType.TYPE_CLASS_TEXT
    private var ic: InputConnection? = null
    private val kpTimeout = 500 //single-char input timeout
    private var cachedDigit = 10
    private var caps = false
    private var rotationMode = false
    private var rotIndex = 0
    private var lockFlag = 0 //input lock flag for long presses
    private var currentLayoutIndex = 0 //you can change the default here
    private val rotResetHandler = Handler()

    //layoutIconsNormal, layoutIconsCaps and layouts must match each other
    private val layoutIconsNormal = arrayOf(R.drawable.ime_hebrew_normal, R.drawable.ime_latin_normal, R.drawable.ime_numbers_normal)
    private val layoutIconsCaps = arrayOf(R.drawable.ime_hebrew_caps, R.drawable.ime_latin_caps, R.drawable.ime_numbers_normal)
    private val layouts = arrayOf(
        arrayOf( //hebrew
            " ", ".,?!'\"1-~@/:\\+_\$#()[]{}",
            "ש", "נ", "ב",
            "ג", "ק", "כ", "ע",
            "י", "ן", "ח", "ל", "ך", "צ", "מ", "ם", "פ", "ת","ר", "ד", "א" ,"ו", "ה", "ףץ", "ס", "ט", "ז", "\n"
        ),
        arrayOf( //latin
            " ", ".,?!'\"1-~@/:\\+_\$#()[]{}",
            "a", "b", "c",
            "d", "e", "f", "g",
            "h", "i", "j", "k", "l", "m", "n", "o", "p", "q","r", "s", "t" ,"u", "v", "w", "x", "y", "z", "\n"
        )
    )
    private var currentLayout: Array<String>? = null

    private fun resetRotator() {
        rotIndex = 0
        rotationMode = false
        cachedDigit = 10
    }

    private fun updateCurrentStatusIcon() {
        val icon = if(caps) layoutIconsCaps[currentLayoutIndex] else layoutIconsNormal[currentLayoutIndex]
        hideStatusIcon()
        showStatusIcon(icon)
    }

    private fun kkToDigit(keyCode: Int): Int {
        return when(keyCode) {
            KeyEvent.KEYCODE_SPACE -> 0
            KeyEvent.KEYCODE_SYM -> 1
            KeyEvent.KEYCODE_A -> 2
            KeyEvent.KEYCODE_B -> 3
            KeyEvent.KEYCODE_C -> 4
            KeyEvent.KEYCODE_D -> 5
            KeyEvent.KEYCODE_E -> 6
            KeyEvent.KEYCODE_F -> 7
            KeyEvent.KEYCODE_G -> 8
            KeyEvent.KEYCODE_H -> 9
            KeyEvent.KEYCODE_I -> 10
            KeyEvent.KEYCODE_J -> 11
            KeyEvent.KEYCODE_K -> 12
            KeyEvent.KEYCODE_L -> 13
            KeyEvent.KEYCODE_M -> 14
            KeyEvent.KEYCODE_N -> 15
            KeyEvent.KEYCODE_O -> 16
            KeyEvent.KEYCODE_P -> 17
            KeyEvent.KEYCODE_Q -> 18
            KeyEvent.KEYCODE_R -> 19
            KeyEvent.KEYCODE_S -> 20
            KeyEvent.KEYCODE_T -> 21
            KeyEvent.KEYCODE_U -> 22
            KeyEvent.KEYCODE_V -> 23
            KeyEvent.KEYCODE_W -> 24
            KeyEvent.KEYCODE_X -> 25
            KeyEvent.KEYCODE_Y -> 26
            KeyEvent.KEYCODE_Z -> 27
            KeyEvent.KEYCODE_ENTER -> 28

            else -> 29
        }
    }

    override fun onStartInput(info: EditorInfo, restarting: Boolean) {
        super.onStartInput(info, restarting)
        currentType = info.inputType
        if(currentType == 0 || currentType and EditorInfo.TYPE_MASK_CLASS == EditorInfo.TYPE_CLASS_PHONE)
            onFinishInput()
        else {
            ic = this.currentInputConnection
            caps = false
            resetRotator()
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            updateCurrentStatusIcon()
        }
    }

    override fun onFinishInput() {
        super.onFinishInput()
        ic = null
        hideStatusIcon()
        requestHideSelf(0)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if(ic == null) {
            resetRotator()
            return false
        }
        val digit = kkToDigit(keyCode)
        var pound = false
        var star = false
        if(digit > 28) {
            when(keyCode) {
                KeyEvent.KEYCODE_BACK -> return handleBack(event)
                KeyEvent.KEYCODE_DEL -> return handleDelete(event)
                KeyEvent.KEYCODE_ALT_RIGHT -> pound = true
                KeyEvent.KEYCODE_SHIFT_LEFT -> star = true
                else -> {
                    resetRotator()
                    return super.onKeyDown(keyCode, event)
                }
            }
        }

        when(currentType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER -> {
                if(pound)
                    ic!!.commitText(".", 1)
                else if(!star)
                    ic!!.commitText(Integer.toString(digit), 1)
                return true
            }
            else -> if(lockFlag == 0) {
                event.startTracking()
                return handleTextInput(digit, star, pound)
            } else
                return true
        }
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        val digit = kkToDigit(keyCode)
        if(digit > 9) {
            resetRotator()
            return false
        }
        ic!!.deleteSurroundingText(1, 0)
        ic!!.commitText(Integer.toString(digit), 1)
        resetRotator()
        lockFlag = 1
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        if (lockFlag == 1) lockFlag = 0
        return super.onKeyUp(keyCode, event)
    }

    private fun handleBack(ev: KeyEvent): Boolean {
        ic!!.sendKeyEvent(ev)
        requestHideSelf(0)
        ic!!.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
        ic!!.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
        return false
    }

    private fun handleDelete(ev: KeyEvent): Boolean {
        resetRotator()
        if (ic == null) {
            requestHideSelf(0)
            return false
        }
        if (ic!!.getTextBeforeCursor(1, 0).isEmpty()) {
            ic!!.sendKeyEvent(ev)
            return false
        } else {
            ic!!.deleteSurroundingText(1, 0)
            return true
        }
    }

    private fun nextLang() {
        val maxInd = layouts.size - 1
        currentLayoutIndex++
        if(currentLayoutIndex > maxInd) currentLayoutIndex = 0
        currentLayout = layouts[currentLayoutIndex]
        updateCurrentStatusIcon()
        resetRotator()
    }

    private fun handleTextInput(digit: Int, star: Boolean, pound: Boolean): Boolean {
        if(star) {
            caps = !caps
            updateCurrentStatusIcon()
        } else if(pound) {
            nextLang()
        } else {
            var targetSequence: CharSequence
            currentLayout = layouts[currentLayoutIndex]
            val selection = currentLayout!![digit]

            rotResetHandler.removeCallbacksAndMessages(null)
            if(digit != cachedDigit) {
                resetRotator()
                cachedDigit = digit
            } else {
                rotationMode = true //mark that we're going to delete the next char
                rotIndex++
                if(rotIndex >= selection.length)
                    rotIndex = 0
            }
            rotResetHandler.postDelayed({ resetRotator() }, kpTimeout.toLong())

            targetSequence = selection.subSequence(rotIndex, rotIndex + 1)
            if(rotationMode)
                ic!!.deleteSurroundingText(1, 0)
            if(caps)
                targetSequence = targetSequence.toString().toUpperCase()
            ic!!.commitText(targetSequence, 1)
        }
        return true
    }
}
