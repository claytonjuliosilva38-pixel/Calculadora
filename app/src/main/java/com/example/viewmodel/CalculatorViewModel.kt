package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import com.example.engine.CalculatorEngine
import com.example.model.AccentOption
import com.example.model.AngleMode
import com.example.model.HistoryItem
import com.example.model.PRESET_ACCENTS
import com.example.model.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject

data class CalculatorUiState(
    val tokens: List<String> = emptyList(),
    val editing: Boolean = false,
    val justEvaluated: Boolean = false,
    val storedExpr: String = "",
    val ans: Double = 0.0,
    val memory: Double = 0.0,
    val angleMode: AngleMode = AngleMode.DEG,
    val isSecond: Boolean = false,
    val isScientificOpen: Boolean = false,
    val history: List<HistoryItem> = emptyList(),
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val accentColor: Color = PRESET_ACCENTS[0].color,
    val accentHex: String = PRESET_ACCENTS[0].hexString,
    val isSettingsOpen: Boolean = false,
    val toastMessage: String? = null,
    val shakeTrigger: Long = 0L,
    val popTrigger: Long = 0L
) {
    val displaySmall: String
        get() {
            return when {
                justEvaluated -> if (storedExpr.isNotEmpty()) "$storedExpr =" else ""
                tokens.isNotEmpty() -> CalculatorEngine.renderTokens(tokens)
                else -> ""
            }
        }

    val displayMain: String
        get() {
            return when {
                justEvaluated -> CalculatorEngine.fmt(ans)
                editing && tokens.isNotEmpty() && CalculatorEngine.isNum(tokens.last()) -> {
                    CalculatorEngine.fmtEntry(tokens.last())
                }
                tokens.isNotEmpty() -> {
                    val preview = CalculatorEngine.tryPreview(tokens, angleMode)
                    if (preview != null) CalculatorEngine.fmt(preview) else "0"
                }
                else -> "0"
            }
        }
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("tecla_calculator_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        loadPersistedState()
    }

    private fun loadPersistedState() {
        val themePrefStr = prefs.getString("theme_pref", ThemePreference.SYSTEM.name)
        val themePref = try {
            ThemePreference.valueOf(themePrefStr ?: ThemePreference.SYSTEM.name)
        } catch (_: Exception) {
            ThemePreference.SYSTEM
        }

        val accentHex = prefs.getString("accent_hex", PRESET_ACCENTS[0].hexString) ?: PRESET_ACCENTS[0].hexString
        val accentColor = parseHexColor(accentHex) ?: PRESET_ACCENTS[0].color

        val angleModeStr = prefs.getString("angle_mode", AngleMode.DEG.name)
        val angleMode = try {
            AngleMode.valueOf(angleModeStr ?: AngleMode.DEG.name)
        } catch (_: Exception) {
            AngleMode.DEG
        }

        val mem = prefs.getFloat("memory", 0.0f).toDouble()
        val isSciOpen = prefs.getBoolean("sci_open", false)

        val histJson = prefs.getString("history", "[]") ?: "[]"
        val histList = mutableListOf<HistoryItem>()
        try {
            val arr = JSONArray(histJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                histList.add(
                    HistoryItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        expression = obj.getString("expression"),
                        result = obj.getString("result"),
                        rawValue = obj.getDouble("rawValue"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
        }

        _uiState.update {
            it.copy(
                themePreference = themePref,
                accentColor = accentColor,
                accentHex = accentHex,
                angleMode = angleMode,
                memory = mem,
                isScientificOpen = isSciOpen,
                history = histList
            )
        }
    }

    private fun parseHexColor(hex: String): Color? {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorInt = cleanHex.toLong(16).toInt() or (0xFF shl 24)
            Color(colorInt)
        } catch (_: Exception) {
            null
        }
    }

    private fun saveHistory(history: List<HistoryItem>) {
        val arr = JSONArray()
        for (item in history) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("expression", item.expression)
            obj.put("result", item.result)
            obj.put("rawValue", item.rawValue)
            obj.put("timestamp", item.timestamp)
            arr.put(obj)
        }
        prefs.edit().putString("history", arr.toString()).apply()
    }

    private fun startFreshIfEvaluated(current: CalculatorUiState): CalculatorUiState {
        return if (current.justEvaluated) {
            current.copy(
                tokens = emptyList(),
                editing = false,
                justEvaluated = false,
                storedExpr = ""
            )
        } else {
            current
        }
    }

    fun pushDigit(d: String) {
        _uiState.update { st ->
            val s = startFreshIfEvaluated(st)
            val tokens = s.tokens.toMutableList()
            var editing = s.editing
            val last = tokens.lastOrNull()

            if (editing && tokens.isNotEmpty()) {
                val currentLast = tokens.last()
                when (currentLast) {
                    "0" -> tokens[tokens.lastIndex] = d
                    "-0" -> tokens[tokens.lastIndex] = "-$d"
                    else -> {
                        val pureDigits = currentLast.replace("-", "").replace(".", "")
                        if (pureDigits.length < 16) {
                            tokens[tokens.lastIndex] = currentLast + d
                        }
                    }
                }
            } else {
                if (CalculatorEngine.isVal(last)) {
                    tokens.add("×")
                }
                tokens.add(d)
                editing = true
            }

            s.copy(tokens = tokens, editing = editing)
        }
    }

    fun pushDot() {
        _uiState.update { st ->
            val s = startFreshIfEvaluated(st)
            val tokens = s.tokens.toMutableList()
            var editing = s.editing
            val last = tokens.lastOrNull()

            if (editing && tokens.isNotEmpty()) {
                val currentLast = tokens.last()
                if (!currentLast.contains(".")) {
                    tokens[tokens.lastIndex] = "$currentLast."
                }
            } else {
                if (CalculatorEngine.isVal(last)) {
                    tokens.add("×")
                }
                tokens.add("0.")
                editing = true
            }

            s.copy(tokens = tokens, editing = editing)
        }
    }

    fun pushOp(op: String) {
        _uiState.update { st ->
            var tokens = st.tokens.toMutableList()
            if (st.justEvaluated) {
                tokens = mutableListOf(st.ans.toString())
            }

            val last = tokens.lastOrNull()
            val prevT = if (tokens.size >= 2) tokens[tokens.size - 2] else null

            val mappedOp = if (op == "−") "-" else op

            if (tokens.isEmpty()) {
                if (mappedOp == "-") {
                    tokens.add("-")
                }
            } else if (last == "(" || CalculatorEngine.FUNCS.contains(last)) {
                if (mappedOp == "-") {
                    tokens.add("-")
                }
            } else if (CalculatorEngine.OPS.contains(last)) {
                if (mappedOp == "-" && last != "-" && (CalculatorEngine.OPS.contains(prevT) || prevT == "(" || prevT == null)) {
                    tokens.add("-")
                } else {
                    tokens[tokens.lastIndex] = mappedOp
                }
            } else {
                tokens.add(mappedOp)
            }

            st.copy(
                tokens = tokens,
                editing = false,
                justEvaluated = false,
                storedExpr = ""
            )
        }
    }

    fun pushFunc(f: String) {
        _uiState.update { st ->
            val s = startFreshIfEvaluated(st)
            val tokens = s.tokens.toMutableList()
            val last = tokens.lastOrNull()

            if (CalculatorEngine.isVal(last)) {
                tokens.add("×")
            }
            tokens.add(f)
            tokens.add("(")

            s.copy(tokens = tokens, editing = false)
        }
    }

    fun pushConst(c: String) {
        _uiState.update { st ->
            val s = startFreshIfEvaluated(st)
            val tokens = s.tokens.toMutableList()
            val last = tokens.lastOrNull()

            if (CalculatorEngine.isVal(last)) {
                tokens.add("×")
            }
            tokens.add(c)

            s.copy(tokens = tokens, editing = false)
        }
    }

    fun pushRawNum(n: String) {
        _uiState.update { st ->
            val s = startFreshIfEvaluated(st)
            val tokens = s.tokens.toMutableList()
            val last = tokens.lastOrNull()

            if (s.editing || CalculatorEngine.isVal(last)) {
                tokens.add("×")
            }
            tokens.add(n)

            s.copy(tokens = tokens, editing = false)
        }
    }

    fun pushPost(p: String) {
        _uiState.update { st ->
            var tokens = st.tokens.toMutableList()
            if (st.justEvaluated) {
                tokens = mutableListOf(st.ans.toString())
            }

            val last = tokens.lastOrNull()
            if (!CalculatorEngine.isVal(last)) {
                return@update st.copy(toastMessage = "Aplique em um número")
            }

            tokens.add(p)
            st.copy(
                tokens = tokens,
                editing = false,
                justEvaluated = false,
                storedExpr = ""
            )
        }
    }

    fun pushParen(p: String) {
        _uiState.update { st ->
            if (p == "(") {
                val s = startFreshIfEvaluated(st)
                val tokens = s.tokens.toMutableList()
                val last = tokens.lastOrNull()
                if (CalculatorEngine.isVal(last)) {
                    tokens.add("×")
                }
                tokens.add("(")
                s.copy(tokens = tokens, editing = false)
            } else {
                var tokens = st.tokens.toMutableList()
                if (st.justEvaluated) {
                    tokens = mutableListOf(st.ans.toString())
                }
                var open = 0
                tokens.forEach {
                    if (it == "(") open++
                    if (it == ")") open--
                }
                val last = tokens.lastOrNull()
                if (open > 0 && !CalculatorEngine.OPS.contains(last) && last != "(") {
                    tokens.add(")")
                    st.copy(
                        tokens = tokens,
                        editing = false,
                        justEvaluated = false,
                        storedExpr = ""
                    )
                } else {
                    st
                }
            }
        }
    }

    fun toggleSign() {
        _uiState.update { st ->
            val tokens = st.tokens.toMutableList()
            when {
                st.editing && tokens.isNotEmpty() -> {
                    val t = tokens.last()
                    tokens[tokens.lastIndex] = if (t.startsWith("-")) t.removePrefix("-") else "-$t"
                    st.copy(tokens = tokens)
                }
                st.justEvaluated -> {
                    val newAns = -st.ans
                    st.copy(
                        ans = newAns,
                        tokens = listOf(newAns.toString()),
                        justEvaluated = false,
                        storedExpr = ""
                    )
                }
                tokens.isNotEmpty() -> {
                    val last = tokens.last()
                    if (CalculatorEngine.isNum(last)) {
                        tokens[tokens.lastIndex] = if (last.startsWith("-")) last.removePrefix("-") else "-$last"
                        st.copy(tokens = tokens)
                    } else if (CalculatorEngine.isVal(last)) {
                        tokens.addAll(listOf("×", "(", "-1", ")"))
                        st.copy(tokens = tokens)
                    } else {
                        st
                    }
                }
                else -> st
            }
        }
    }

    fun back() {
        _uiState.update { st ->
            if (st.justEvaluated) {
                return@update st.copy(
                    tokens = emptyList(),
                    editing = false,
                    justEvaluated = false,
                    storedExpr = ""
                )
            }

            val tokens = st.tokens.toMutableList()
            var editing = st.editing

            if (editing && tokens.isNotEmpty()) {
                val currentLast = tokens.last()
                val shortened = currentLast.dropLast(1)
                if (shortened.isEmpty() || shortened == "-") {
                    tokens.removeAt(tokens.lastIndex)
                } else {
                    tokens[tokens.lastIndex] = shortened
                }
                if (tokens.isEmpty() || !CalculatorEngine.isNum(tokens.last())) {
                    editing = false
                }
            } else if (tokens.isNotEmpty()) {
                tokens.removeAt(tokens.lastIndex)
                if (tokens.isNotEmpty() && CalculatorEngine.FUNCS.contains(tokens.last())) {
                    tokens.removeAt(tokens.lastIndex)
                }
            }

            st.copy(tokens = tokens, editing = editing)
        }
    }

    fun clearAll() {
        _uiState.update {
            it.copy(
                tokens = emptyList(),
                editing = false,
                justEvaluated = false,
                storedExpr = ""
            )
        }
    }

    fun equals() {
        val st = _uiState.value
        if (st.tokens.isEmpty()) return

        val stripped = CalculatorEngine.stripClose(st.tokens)
        if (stripped.isEmpty()) return

        try {
            val result = CalculatorEngine.clean(CalculatorEngine.evalTokens(stripped, st.angleMode))
            val expressionRendered = CalculatorEngine.renderTokens(stripped)
            val formattedResult = CalculatorEngine.fmt(result)

            val newHistoryItem = HistoryItem(
                expression = expressionRendered,
                result = formattedResult,
                rawValue = result
            )
            val updatedHistory = listOf(newHistoryItem) + st.history.take(39)
            saveHistory(updatedHistory)

            _uiState.update {
                it.copy(
                    ans = result,
                    tokens = listOf(result.toString()),
                    justEvaluated = true,
                    editing = false,
                    storedExpr = expressionRendered,
                    history = updatedHistory,
                    popTrigger = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Erro"
            _uiState.update {
                it.copy(
                    toastMessage = errorMsg,
                    shakeTrigger = System.currentTimeMillis()
                )
            }
        }
    }

    fun pushInverse() {
        _uiState.update { st ->
            var tokens = st.tokens.toMutableList()
            if (st.justEvaluated) {
                tokens = mutableListOf(st.ans.toString())
            }

            val last = tokens.lastOrNull()
            if (!CalculatorEngine.isVal(last)) {
                return@update st.copy(toastMessage = "Aplique em um número")
            }

            tokens.addAll(listOf("^", "(", "-1", ")"))
            st.copy(
                tokens = tokens,
                editing = false,
                justEvaluated = false,
                storedExpr = ""
            )
        }
    }

    fun pushExp() {
        _uiState.update { st ->
            val s = startFreshIfEvaluated(st)
            val tokens = s.tokens.toMutableList()
            val last = tokens.lastOrNull()
            if (CalculatorEngine.isVal(last)) {
                tokens.add("×")
            }
            tokens.addAll(listOf("e", "^", "("))
            s.copy(tokens = tokens, editing = false)
        }
    }

    fun pushTenX() {
        _uiState.update { st ->
            val s = startFreshIfEvaluated(st)
            val tokens = s.tokens.toMutableList()
            val last = tokens.lastOrNull()
            if (s.editing || CalculatorEngine.isVal(last)) {
                tokens.add("×")
            }
            tokens.addAll(listOf("10", "^", "("))
            s.copy(tokens = tokens, editing = false)
        }
    }

    fun toggleSecond() {
        _uiState.update { it.copy(isSecond = !it.isSecond) }
    }

    fun toggleAngle() {
        _uiState.update { st ->
            val newMode = if (st.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
            prefs.edit().putString("angle_mode", newMode.name).apply()
            st.copy(angleMode = newMode)
        }
    }

    fun toggleScientific() {
        _uiState.update { st ->
            val newOpen = !st.isScientificOpen
            prefs.edit().putBoolean("sci_open", newOpen).apply()
            st.copy(isScientificOpen = newOpen)
        }
    }

    fun memOp(op: String) {
        val st = _uiState.value
        when (op) {
            "mc" -> {
                prefs.edit().putFloat("memory", 0f).apply()
                _uiState.update { it.copy(memory = 0.0) }
            }
            "mr" -> {
                if (st.memory == 0.0) return
                _uiState.update {
                    it.copy(
                        tokens = listOf(st.memory.toString()),
                        editing = false,
                        justEvaluated = false,
                        storedExpr = ""
                    )
                }
            }
            "mplus", "mminus" -> {
                val currentVal: Double? = when {
                    st.editing && st.tokens.isNotEmpty() -> st.tokens.last().toDoubleOrNull()
                    st.justEvaluated -> st.ans
                    st.tokens.isNotEmpty() -> CalculatorEngine.tryPreview(st.tokens, st.angleMode)
                    else -> null
                }

                if (currentVal == null) {
                    _uiState.update { it.copy(toastMessage = "Nada na tela para guardar") }
                    return
                }

                val delta = if (op == "mplus") currentVal else -currentVal
                val newMem = CalculatorEngine.clean(st.memory + delta)
                prefs.edit().putFloat("memory", newMem.toFloat()).apply()
                _uiState.update {
                    it.copy(
                        memory = newMem,
                        popTrigger = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    fun setThemePreference(pref: ThemePreference) {
        prefs.edit().putString("theme_pref", pref.name).apply()
        _uiState.update { it.copy(themePreference = pref) }
    }

    fun setAccentColor(accent: AccentOption) {
        prefs.edit().putString("accent_hex", accent.hexString).apply()
        _uiState.update {
            it.copy(
                accentColor = accent.color,
                accentHex = accent.hexString
            )
        }
    }

    fun setCustomAccentColor(color: Color, hex: String) {
        prefs.edit().putString("accent_hex", hex).apply()
        _uiState.update {
            it.copy(
                accentColor = color,
                accentHex = hex
            )
        }
    }

    fun openSettings(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun clearHistory() {
        prefs.edit().putString("history", "[]").apply()
        _uiState.update {
            it.copy(
                history = emptyList(),
                toastMessage = "Histórico limpo"
            )
        }
    }

    fun restoreHistoryItem(item: HistoryItem) {
        _uiState.update {
            it.copy(
                ans = item.rawValue,
                tokens = listOf(item.rawValue.toString()),
                justEvaluated = true,
                editing = false,
                storedExpr = item.expression,
                isSettingsOpen = false,
                popTrigger = System.currentTimeMillis()
            )
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
    }
}
