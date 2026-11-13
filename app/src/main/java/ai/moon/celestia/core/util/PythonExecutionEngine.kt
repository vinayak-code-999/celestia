package ai.moon.celestia.core.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.math.*

data class PythonExecutionResult(
    val isSuccess: Boolean,
    val stdout: String,
    val error: String? = null,
    val executionTimeMs: Long = 0L
)

object PythonExecutionEngine {

    fun convertPromptToPython(prompt: String): String {
        val p = prompt.trim()

        // 1. If explicit python code block present in prompt
        val codeBlockRegex = Regex("```(?:python|py)?\\s*(.*?)\\s*```", RegexOption.DOT_MATCHES_ALL)
        val match = codeBlockRegex.find(p)
        if (match != null) {
            return match.groupValues[1].trim()
        }

        // 2. Factorial
        val factRegex = Regex("(?:factorial of|factorial)\\s*(\\d+)|\\b(\\d+)!\\b", RegexOption.IGNORE_CASE)
        val factMatch = factRegex.find(p)
        if (factMatch != null) {
            val num = factMatch.groupValues[1].ifEmpty { factMatch.groupValues[2] }
            if (num.isNotEmpty()) {
                return "import math\nprint(math.factorial($num))"
            }
        }

        // 3. Square root
        val sqrtRegex = Regex("(?:sqrt of|square root of|sqrt)\\s*([\\d.]+)", RegexOption.IGNORE_CASE)
        val sqrtMatch = sqrtRegex.find(p)
        if (sqrtMatch != null) {
            val num = sqrtMatch.groupValues[1]
            return "import math\nprint(math.sqrt($num))"
        }

        // 4. Arithmetic expressions (e.g. 98765 * 4321, 2^10, (123 + 456) / 7)
        val cleaned = p.replace(Regex("[^\\d+\\-*/%^().\\s]"), "").trim()
        if (cleaned.length >= 2 && cleaned.any { it in "+-*/%^" }) {
            val pyExpr = cleaned.replace("^", "**")
            return "print($pyExpr)"
        }

        return p
    }

    suspend fun executePythonCode(code: String, timeoutMs: Long = 5000L): PythonExecutionResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        if (code.isBlank()) {
            return@withContext PythonExecutionResult(false, "", "Code block is empty.")
        }

        val result = kotlinx.coroutines.withTimeoutOrNull(timeoutMs) {
            try {
                val lines = code.lines()
                val variables = mutableMapOf<String, Any>()

                // Pre-register standard math constants
                variables["pi"] = Math.PI
                variables["e"] = Math.E
                variables["tau"] = Math.PI * 2

                val outputStream = ByteArrayOutputStream()
                val printStream = PrintStream(outputStream)

                for (rawLine in lines) {
                    val line = rawLine.trim()
                    if (line.isEmpty() || line.startsWith("#")) continue

                    // Check print statement
                    if (line.startsWith("print(") && line.endsWith(")")) {
                        val expr = line.substring(6, line.length - 1).trim()
                        val valRes = evaluateExpression(expr, variables)
                        printStream.println(valRes)
                    } else if (line.contains("=") && !line.contains("==") && !line.contains("<=") && !line.contains(">=") && !line.contains("!=")) {
                        val parts = line.split("=", limit = 2)
                        val varName = parts[0].trim()
                        val expr = parts[1].trim()
                        val valRes = evaluateExpression(expr, variables)
                        if (valRes != null) {
                            variables[varName] = valRes
                        }
                    } else {
                        // Standalone expression evaluation
                        val valRes = evaluateExpression(line, variables)
                        if (valRes != null && valRes != line) {
                            printStream.println(valRes)
                        }
                    }
                }

                printStream.flush()
                val rawOutput = outputStream.toString("UTF-8").trim()
                val executionTime = System.currentTimeMillis() - startTime

                if (rawOutput.isNotBlank()) {
                    PythonExecutionResult(true, rawOutput, null, executionTime)
                } else {
                    PythonExecutionResult(true, "Execution finished cleanly.", null, executionTime)
                }
            } catch (e: Exception) {
                val executionTime = System.currentTimeMillis() - startTime
                PythonExecutionResult(false, "", e.message ?: "Python Execution Error", executionTime)
            }
        }

        if (result == null) {
            val executionTime = System.currentTimeMillis() - startTime
            PythonExecutionResult(false, "", "Execution timed out after ${timeoutMs / 1000}s.", executionTime)
        } else {
            result
        }
    }

    private fun evaluateExpression(exprStr: String, vars: Map<String, Any>): Any? {
        val expr = exprStr.trim()
        if (expr.isEmpty()) return null

        // String literals
        if ((expr.startsWith("\"") && expr.endsWith("\"")) || (expr.startsWith("'") && expr.endsWith("'"))) {
            return expr.substring(1, expr.length - 1)
        }

        // Direct variable lookup
        if (vars.containsKey(expr)) {
            return vars[expr]
        }

        // json.dumps handling
        if (expr.startsWith("json.dumps(") && expr.endsWith(")")) {
            val innerVar = expr.substring(11, expr.length - 1).trim()
            val innerVal = vars[innerVar] ?: evaluateExpression(innerVar, vars)
            if (innerVal != null) {
                return if (innerVal is Map<*, *>) {
                    org.json.JSONObject(innerVal as Map<String, Any>).toString()
                } else {
                    innerVal.toString()
                }
            }
        }

        // Math functions
        if (expr.startsWith("math.sqrt(") && expr.endsWith(")")) {
            val inner = evaluateExpression(expr.substring(10, expr.length - 1), vars) as? Number
            return if (inner != null) sqrt(inner.toDouble()) else null
        }
        if (expr.startsWith("math.factorial(") && expr.endsWith(")")) {
            val inner = evaluateExpression(expr.substring(15, expr.length - 1), vars) as? Number
            return if (inner != null) factorial(inner.toInt()) else null
        }
        if (expr.startsWith("math.sin(") && expr.endsWith(")")) {
            val inner = evaluateExpression(expr.substring(9, expr.length - 1), vars) as? Number
            return if (inner != null) sin(inner.toDouble()) else null
        }
        if (expr.startsWith("math.cos(") && expr.endsWith(")")) {
            val inner = evaluateExpression(expr.substring(9, expr.length - 1), vars) as? Number
            return if (inner != null) cos(inner.toDouble()) else null
        }
        if (expr.startsWith("math.pow(") && expr.endsWith(")")) {
            val parts = expr.substring(9, expr.length - 1).split(",")
            if (parts.size == 2) {
                val base = evaluateExpression(parts[0], vars) as? Number
                val exp = evaluateExpression(parts[1], vars) as? Number
                if (base != null && exp != null) return base.toDouble().pow(exp.toDouble())
            }
        }

        // Math expression evaluator
        return try {
            evalMathExpression(expr, vars)
        } catch (_: Exception) {
            expr
        }
    }

    private fun evalMathExpression(expression: String, vars: Map<String, Any>): Number {
        var str = expression
        for ((k, v) in vars) {
            if (v is Number) {
                str = str.replace(Regex("\\b$k\\b"), v.toString())
            }
        }
        str = str.replace("**", "^").replace(" ", "")

        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < str.length) str.elementAt(pos).code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected char: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm()
                        eat('-'.code) -> x -= parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor()
                        eat('/'.code) -> x /= parseFactor()
                        eat('%'.code) -> x %= parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                } else if (ch >= 'a'.code && ch <= 'z'.code) {
                    while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                    val func = str.substring(startPos, pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> sqrt(x)
                        "sin" -> sin(Math.toRadians(x))
                        "cos" -> cos(Math.toRadians(x))
                        "tan" -> tan(Math.toRadians(x))
                        "log" -> ln(x)
                        "abs" -> abs(x)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected char: " + ch.toChar())
                }

                if (eat('^'.code)) x = x.pow(parseFactor())

                return x
            }
        }.parse()
    }

    private fun factorial(n: Int): Long {
        if (n <= 1) return 1L
        var res = 1L
        for (i in 2..minOf(n, 20)) {
            res *= i
        }
        return res
    }
}