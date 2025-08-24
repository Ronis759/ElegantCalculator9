package com.sergey.calc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.round

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CalculatorScreen()
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1E3C72), Color(0xFF2A5298))
    )
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }
    val clipboard: ClipboardManager = LocalClipboardManager.current

    fun append(token: String) {
        if (token == "C") { expression = ""; result = "0"; return }
        if (token == "⌫") {
            if (expression.isNotEmpty()) expression = expression.dropLast(1)
            return
        }
        if (token == "=") {
            try {
                val value = safeEval(expression)
                result = formatNumber(value)
            } catch (_: Exception) {
                result = "Ошибка"
            }
            return
        }
        // Prevent two operators in a row
        val ops = setOf('+','-','×','÷','%','.')
        if (expression.isNotEmpty() && ops.contains(expression.last()) && ops.contains(token.first())) {
            expression = expression.dropLast(1) + token
        } else {
            expression += token
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = expression.ifEmpty { "0" },
                    color = Color(0xFFB0C4FF),
                    fontSize = 28.sp,
                    maxLines = 2,
                    lineHeight = 32.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = result,
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ElevatedButton(
                    onClick = {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(result))
                    },
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Копировать результат") }
            }

            // Keypad
            val rows = listOf(
                listOf("C", "⌫", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { label ->
                            val isOp = label in listOf("+","-","×","÷","=","%")
                            val weight = if (label == "0" && row.size == 3) 2f else 1f
                            ElevatedButton(
                                onClick = { append(label) },
                                modifier = Modifier
                                    .weight(weight)
                                    .height(64.dp)
                                    .shadow(6.dp, RoundedCornerShape(20.dp), clip = false),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = when {
                                        label == "=" -> Color(0xFF00C2A8)
                                        isOp || label in listOf("C","⌫","%") -> Color(0xFF3D83F6)
                                        else -> Color(0xFF1A1B20)
                                    },
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    label,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Very small expression evaluator for + - × ÷ % and decimal
fun safeEval(expr: String): Double {
    // replace symbols with standard operators
    val s = expr.replace('×','*').replace('÷','/')
    // Use a simple shunting-yard / stack-based evaluator
    val nums = ArrayDeque<Double>()
    val ops = ArrayDeque<Char>()

    fun prec(c: Char) = when(c) {
        '+','-' -> 1
        '*','/','%' -> 2
        else -> 0
    }
    fun apply() {
        if (ops.isEmpty() || nums.size < 2) return
        val op = ops.removeLast()
        val b = nums.removeLast()
        val a = nums.removeLast()
        val res = when(op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> a / b
            '%' -> a % b
            else -> 0.0
        }
        nums.addLast(res)
    }

    var i = 0
    var canUnary = true
    while (i < s.length) {
        val c = s[i]
        when {
            c.isWhitespace() -> i++
            c.isDigit() || c == '.' || (c=='-' && canUnary) -> {
                var j = i
                if (c=='-' && canUnary) j += 1
                while (j < s.length && (s[j].isDigit() || s[j]=='.')) j++
                val numStr = s.substring(i, j)
                nums.addLast(numStr.toDouble())
                i = j
                canUnary = false
            }
            c in charArrayOf('+','-','*','/','%') -> {
                while (ops.isNotEmpty() && prec(ops.last()) >= prec(c)) apply()
                ops.addLast(c)
                i++
                canUnary = true
            }
            else -> throw IllegalArgumentException("Bad char: $c")
        }
    }
    while (ops.isNotEmpty()) apply()
    return nums.lastOrNull() ?: 0.0
}

fun formatNumber(x: Double): String {
    val rounded = String.format("%.10f", x).trimEnd('0').trimEnd('.')
    return if (rounded.isEmpty()) "0" else rounded
}