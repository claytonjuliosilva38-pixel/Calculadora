package com.example

import com.example.engine.CalculatorEngine
import com.example.model.AngleMode
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {

    @Test
    fun testBasicArithmetic() {
        val tokens = listOf("2", "+", "2")
        val result = CalculatorEngine.evalTokens(tokens, AngleMode.DEG)
        assertEquals(4.0, result, 1e-9)
    }

    @Test
    fun testPercentageAddAndSubtract() {
        // 20 + 50% = 30
        val tokensAdd = listOf("20", "+", "50", "%")
        val resultAdd = CalculatorEngine.evalTokens(tokensAdd, AngleMode.DEG)
        assertEquals(30.0, resultAdd, 1e-9)

        // 20 - 50% = 10
        val tokensSub = listOf("20", "-", "50", "%")
        val resultSub = CalculatorEngine.evalTokens(tokensSub, AngleMode.DEG)
        assertEquals(10.0, resultSub, 1e-9)
    }

    @Test
    fun testPowerAndFactorial() {
        val tokensPow = listOf("2", "^", "3")
        assertEquals(8.0, CalculatorEngine.evalTokens(tokensPow, AngleMode.DEG), 1e-9)

        val tokensFact = listOf("5", "!")
        assertEquals(120.0, CalculatorEngine.evalTokens(tokensFact, AngleMode.DEG), 1e-9)
    }

    @Test
    fun testTrigonometry() {
        val tokensSin30Deg = listOf("sin", "(", "30", ")")
        assertEquals(0.5, CalculatorEngine.evalTokens(tokensSin30Deg, AngleMode.DEG), 1e-9)
    }
}
