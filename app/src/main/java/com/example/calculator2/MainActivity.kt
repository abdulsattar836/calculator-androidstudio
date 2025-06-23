package com.example.calculator2

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class MainActivity : AppCompatActivity() {

    private lateinit var expression: TextView
    private lateinit var result: TextView
    private lateinit var clear: Button
    private lateinit var backspace: Button
    private lateinit var percentage: Button
    private lateinit var divide: Button
    private lateinit var multiply: Button
    private lateinit var add: Button
    private lateinit var subtract: Button
    private lateinit var dot: Button
    private lateinit var zero: Button
    private lateinit var doubleZero: Button
    private lateinit var one: Button
    private lateinit var two: Button
    private lateinit var three: Button
    private lateinit var four: Button
    private lateinit var five: Button
    private lateinit var six: Button
    private lateinit var seven: Button
    private lateinit var eight: Button
    private lateinit var nine: Button
    private lateinit var equal: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ── wire widgets ────────────────────────────────────────────────────────
        expression = findViewById(R.id.expression)
        result     = findViewById(R.id.result)
        clear      = findViewById(R.id.clear)
        backspace  = findViewById(R.id.backspace)
        percentage = findViewById(R.id.percentage)
        divide     = findViewById(R.id.divide)
        multiply   = findViewById(R.id.multiply)
        add        = findViewById(R.id.add)
        subtract   = findViewById(R.id.subtract)
        equal      = findViewById(R.id.equal)
        zero       = findViewById(R.id.zero)
        doubleZero = findViewById(R.id.doubleZero)
        dot        = findViewById(R.id.dot)
        one        = findViewById(R.id.one)
        two        = findViewById(R.id.two)
        three      = findViewById(R.id.three)
        four       = findViewById(R.id.four)
        five       = findViewById(R.id.five)
        six        = findViewById(R.id.six)
        seven      = findViewById(R.id.seven)
        eight      = findViewById(R.id.eight)
        nine       = findViewById(R.id.nine)

        expression.movementMethod = ScrollingMovementMethod()

        // ── clear ──────────────────────────────────────────────────────────────
        clear.setOnClickListener {
            expressionText("0")
            expression.textSize = 60F
            result.textSize     = 30F
            resultText()
        }

        // ── backspace ──────────────────────────────────────────────────────────
        backspace.setOnClickListener {
            val txt = expression.text.toString()
            if (txt.isNotEmpty()) {
                val newTxt = txt.dropLast(1).ifEmpty { "0" }
                expressionText(newTxt)
                resultText()
            }
        }

        // ── operators (+ – × ÷) ───────────────────────────────────────────────
        val operatorButtons = listOf(
            divide to "÷", multiply to "×", add to "+", subtract to "-",percentage to "%"
        )
        operatorButtons.forEach { (btn, sym) ->
            btn.setOnClickListener {
                val cur = expression.text.toString()
                if (!cur.endsWithAny("%", "÷", "×", "+", "-", ".")) {
                    expressionText(cur + sym)
                }
            }
        }

        // ── % key  → just add the sign, let resultText() do the math ──────────
        percentage.setOnClickListener {
            val cur = expression.text.toString()
            if (cur.isEmpty()) return@setOnClickListener
            if (cur.endsWithAny("%", "÷", "×", "+", "-", ".")) return@setOnClickListener
            expressionText(cur + "%")
            resultText()
        }

        // ── equal ──────────────────────────────────────────────────────────────
        equal.setOnClickListener {
            resultText()
            expression.textSize = 30F
            result.textSize     = 60F
        }

        // ── digits & dot ───────────────────────────────────────────────────────
        val numberButtons = mapOf(
            zero to "0", doubleZero to "00", dot to ".",
            one to "1", two to "2", three to "3", four to "4",
            five to "5", six to "6", seven to "7", eight to "8", nine to "9"
        )
        numberButtons.forEach { (btn, value) ->
            btn.setOnClickListener {
                val cur = expression.text.toString()
                val newTxt = if (cur == "0" && value != ".") value else cur + value
                expressionText(newTxt)
                resultText()
            }
        }
    }

    // ───────────────────────────────────────────────────────────────────────────
    private fun expressionText(str: String) { expression.text = str }

    // ── preview / evaluation ──────────────────────────────────────────────────
    private fun resultText() {
        val exp    = expression.text.toString()
        val evalExp = buildEvalString(exp)
            .replace("×", "*")
            .replace("÷", "/")

        val engine: ScriptEngine = ScriptEngineManager().getEngineByName("rhino")
        try {
            val resStr = engine.eval(evalExp).toString()
            result.text = resStr.removeSuffix(".0")
        } catch (e: Exception) {
            result.text = ""
        }
    }

    // convert any expression that still contains '%' into plain JavaScript -----
    private fun buildEvalString(exp: String): String {
        if (!exp.contains('%')) return exp
        // single number with "%" (e.g. "15%")
        if (!exp.contains('+') && !exp.contains('-') &&
            !exp.contains('×') && !exp.contains('÷')) {
            return exp.removeSuffix("%") + "/100"
        }

        // expression ends with % and has exactly ONE operator -> handle it
        if (exp.endsWith('%')) {
            val opIndex = exp.dropLast(1).lastIndexOfAny(charArrayOf('+', '-', '×', '÷'))
            if (opIndex != -1) {
                val op  = exp[opIndex]
                val lhs = exp.substring(0, opIndex)
                val rhs = exp.substring(opIndex + 1, exp.length - 1)

                return when (op) {
                    '×', '÷' -> "$lhs$op$rhs/100"
                    '+'      -> "$lhs+($lhs*$rhs/100)"
                    '-'      -> "$lhs-($lhs*$rhs/100)"
                    else     -> exp
                }
            }
        }
        return exp
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private fun String.endsWithAny(vararg suffixes: String): Boolean =
        suffixes.any { endsWith(it) }

//    private fun Double.stripTrailingZeros(): String =
//        BigDecimal(this).stripTrailingZeros().toPlainString()
}
