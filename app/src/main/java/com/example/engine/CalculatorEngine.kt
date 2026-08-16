package com.example.engine

import com.example.model.AngleMode
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

object CalculatorEngine {

    val FUNCS = setOf("sin", "cos", "tan", "asin", "acos", "atan", "ln", "log", "sqrt", "cbrt")
    val OPS = setOf("+", "-", "×", "÷", "^")
    val POST_OPS = setOf("%", "!", "²", "³")

    fun isNum(token: String): Boolean {
        return token.matches(Regex("^-?(\\d+(\\.\\d*)?|\\.\\d+)$"))
    }

    fun isVal(token: String?): Boolean {
        if (token == null) return false
        return isNum(token) || token == "π" || token == "e" || token == ")" || POST_OPS.contains(token)
    }

    fun fact(n: Double): Double {
        if (n < 0 || n % 1.0 != 0.0) throw IllegalArgumentException("Fatorial só de inteiros ≥ 0")
        if (n > 170) throw IllegalArgumentException("Número muito grande")
        var r = 1.0
        val limit = n.toInt()
        for (i in 2..limit) {
            r *= i
        }
        return r
    }

    fun applyFunc(f: String, x: Double, angleMode: AngleMode): Double {
        val d = Math.PI / 180.0
        return when (f) {
            "sin" -> {
                val rad = if (angleMode == AngleMode.DEG) x * d else x
                val v = sin(rad)
                if (abs(v) < 1e-10) 0.0 else v
            }
            "cos" -> {
                val rad = if (angleMode == AngleMode.DEG) x * d else x
                val v = cos(rad)
                if (abs(v) < 1e-10) 0.0 else v
            }
            "tan" -> {
                val rad = if (angleMode == AngleMode.DEG) x * d else x
                val cosVal = cos(rad)
                if (abs(cosVal) < 1e-10) throw IllegalArgumentException("Tangente indefinida")
                val v = tan(rad)
                if (abs(v) < 1e-10) 0.0 else v
            }
            "asin" -> {
                if (abs(x) > 1.0) throw IllegalArgumentException("Fora do domínio (−1 a 1)")
                val r = asin(x)
                if (angleMode == AngleMode.DEG) r / d else r
            }
            "acos" -> {
                if (abs(x) > 1.0) throw IllegalArgumentException("Fora do domínio (−1 a 1)")
                val r = acos(x)
                if (angleMode == AngleMode.DEG) r / d else r
            }
            "atan" -> {
                val r = atan(x)
                if (angleMode == AngleMode.DEG) r / d else r
            }
            "ln" -> {
                if (x <= 0.0) throw IllegalArgumentException("ln exige número positivo")
                ln(x)
            }
            "log" -> {
                if (x <= 0.0) throw IllegalArgumentException("log exige número positivo")
                log10(x)
            }
            "sqrt" -> {
                if (x < 0.0) throw IllegalArgumentException("Raiz de número negativo")
                sqrt(x)
            }
            "cbrt" -> cbrt(x)
            else -> throw IllegalArgumentException("Função desconhecida: $f")
        }
    }

    fun clean(v: Double): Double {
        if (!v.isFinite()) throw IllegalArgumentException("Número muito grande")
        if (abs(v) < 1e-14) return 0.0
        val bd = BigDecimal(v).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
        val res = bd.toDouble()
        return if (res == -0.0) 0.0 else res
    }

    private class ExpressionParser(
        private val tokens: List<String>,
        private val angleMode: AngleMode
    ) {
        private var i = 0

        private fun peek(): String? = if (i < tokens.size) tokens[i] else null

        fun parse(): Double {
            val res = expr()
            if (i != tokens.size) throw IllegalArgumentException("Expressão inválida")
            return res
        }

        private fun expr(): Double {
            var v = term()
            while (peek() == "+" || peek() == "-") {
                val op = tokens[i++]
                val s = i
                var r = term()
                // Porcentagem contextual: se o termo é só "N%", vale N% do acumulado.
                // 20 + 50% → 20 + (20 * 0.5) = 30; 20 - 50% → 10
                if (i == s + 2 && tokens[s + 1] == "%" && (isNum(tokens[s]) || tokens[s] == "π" || tokens[s] == "e")) {
                    r = v * r
                }
                v = if (op == "+") v + r else v - r
            }
            return v
        }

        private fun term(): Double {
            var v = unary()
            while (peek() == "×" || peek() == "÷") {
                val op = tokens[i++]
                val r = unary()
                if (op == "÷") {
                    if (r == 0.0) throw ArithmeticException("Divisão por zero")
                    v /= r
                } else {
                    v *= r
                }
            }
            return v
        }

        private fun unary(): Double {
            if (peek() == "-") {
                i++
                return -unary()
            }
            if (peek() == "+") {
                i++
                return unary()
            }
            return power()
        }

        private fun power(): Double {
            var v = post()
            if (peek() == "^") {
                i++
                val r = unary()
                v = v.pow(r)
                if (!v.isFinite()) throw IllegalArgumentException("Número muito grande")
            }
            return v
        }

        private fun post(): Double {
            var v = atom()
            while (true) {
                val t = peek()
                when (t) {
                    "%" -> {
                        v /= 100.0
                        i++
                    }
                    "!" -> {
                        v = fact(v)
                        i++
                    }
                    "²" -> {
                        v *= v
                        i++
                    }
                    "³" -> {
                        v = v * v * v
                        i++
                    }
                    else -> break
                }
            }
            return v
        }

        private fun atom(): Double {
            val t = peek() ?: throw IllegalArgumentException("Expressão incompleta")
            if (isNum(t)) {
                i++
                return t.toDouble()
            }
            if (t == "π") {
                i++
                return Math.PI
            }
            if (t == "e") {
                i++
                return Math.E
            }
            if (t == "(") {
                i++
                val v = expr()
                if (peek() != ")") throw IllegalArgumentException("Parênteses incompletos")
                i++
                return v
            }
            if (FUNCS.contains(t)) {
                i++
                if (peek() != "(") throw IllegalArgumentException("Expressão inválida")
                i++
                val v = expr()
                if (peek() != ")") throw IllegalArgumentException("Parênteses incompletos")
                i++
                return applyFunc(t, v, angleMode)
            }
            throw IllegalArgumentException("Expressão inválida: $t")
        }
    }

    fun evalTokens(tokens: List<String>, angleMode: AngleMode): Double {
        return ExpressionParser(tokens, angleMode).parse()
    }

    fun stripClose(tokens: List<String>): List<String> {
        val t = tokens.toMutableList()
        while (t.isNotEmpty() && (OPS.contains(t.last()) || t.last() == "(" || FUNCS.contains(t.last()))) {
            t.removeAt(t.lastIndex)
        }
        var open = 0
        t.forEach {
            if (it == "(") open++
            if (it == ")") open--
        }
        while (open > 0) {
            t.add(")")
            open--
        }
        return t
    }

    fun tryPreview(tokens: List<String>, angleMode: AngleMode): Double? {
        if (tokens.isEmpty()) return null
        val stripped = stripClose(tokens)
        if (stripped.isEmpty()) return null
        return try {
            clean(evalTokens(stripped, angleMode))
        } catch (_: Exception) {
            null
        }
    }

    fun fmtEntry(s: String): String {
        val neg = s.startsWith("-")
        val body = if (neg) s.removePrefix("-") else s
        val parts = body.split(".")
        val integerPart = parts[0]
        val fracPart = if (parts.size > 1) parts[1] else null
        val hasDot = body.contains(".")

        val formattedInt = integerPart.reversed().chunked(3).joinToString(".").reversed()
        val result = (if (neg) "−" else "") + formattedInt + (if (hasDot) ",${fracPart ?: ""}" else "")
        return result
    }

    fun fmt(n: Double): String {
        if (!n.isFinite()) return "Erro"
        val value = if (n == -0.0) 0.0 else n
        val a = abs(value)
        val symbols = DecimalFormatSymbols(Locale("pt", "BR")).apply {
            decimalSeparator = ','
            groupingSeparator = '.'
        }

        if (value != 0.0 && (a >= 1e15 || a < 1e-9)) {
            val expFormat = DecimalFormat("0.########E0", symbols)
            return expFormat.format(value).replace("E", "e")
        }

        val df = DecimalFormat("#,##0.############", symbols)
        return df.format(value)
    }

    fun renderTokens(tokens: List<String>): String {
        val sb = StringBuilder()
        val dispMap = mapOf(
            "sin" to "sin",
            "cos" to "cos",
            "tan" to "tan",
            "asin" to "sin⁻¹",
            "acos" to "cos⁻¹",
            "atan" to "tan⁻¹",
            "ln" to "ln",
            "log" to "log",
            "sqrt" to "√",
            "cbrt" to "∛"
        )

        for (t in tokens) {
            when {
                FUNCS.contains(t) -> sb.append(dispMap[t] ?: t)
                t == "(" || t == ")" -> sb.append(t)
                POST_OPS.contains(t) -> sb.append(t)
                t == "^" -> sb.append(" ^ ")
                t == "+" -> sb.append(" + ")
                t == "-" -> sb.append(" − ")
                t == "×" -> sb.append(" × ")
                t == "÷" -> sb.append(" ÷ ")
                t == "π" || t == "e" -> sb.append(t)
                isNum(t) -> sb.append(fmtEntry(t))
                else -> sb.append(t)
            }
        }
        return sb.toString()
    }
}
