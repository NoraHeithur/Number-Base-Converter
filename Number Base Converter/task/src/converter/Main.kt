package converter

import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

const val SOURCE_BASE = 0
const val TARGET_BASE = 1
const val ZERO = 0
const val ONE = 1
const val SCALE = 5
const val DECIMAL_BASE = 10
const val ZERO_FRACTION = "0E-10"

fun main() {
    question()
}

fun question() {
    var exit = false
    while (!exit) {
        println("Enter two numbers in format: {source base} {target base} (To quit type /exit)")
        val sequenceBase = readln().split(" ")
        if (sequenceBase[SOURCE_BASE] == "/exit") {
            exit = true
        } else {
            var back = false
            while (!back) {
                println("Enter number in base ${sequenceBase[SOURCE_BASE]} to convert to base ${sequenceBase[TARGET_BASE]} (To go back type /back)")
                when (val number = readln()) {
                    "/back" -> {
                        back = true
                    }
                    else -> {
                        questionProduce(number, sequenceBase[SOURCE_BASE], sequenceBase[TARGET_BASE])
                    }
                }
            }
        }
    }
}

/*fun convertFromDecimal() {
    print("Enter number in decimal system: ")
    val numberInDecimal = readLine()!!.toInt()

    print("Enter target base: ")
    val base = readLine()!!.toInt()

    println(
        "Conversion result: ${
            when (base) {
                2 -> Integer.toBinaryString(numberInDecimal)
                8 -> Integer.toOctalString(numberInDecimal)
                16 -> Integer.toHexString(numberInDecimal)
                else -> UNKNOWN_BASE
            }
        }"
    )
}*/

/*fun convertToDecimal() {
    print("Enter source number: ")
    val sourceNumber = readLine()!!

    print("Enter source base: ")
    val sourceBase = readLine()!!.toInt()

    println(
        "Conversion to decimal result: ${
            when (sourceBase) {
                2 -> baseConversion(sourceBase, sourceNumber)
                8 -> baseConversion(sourceBase, sourceNumber)
                16 -> baseConversion(sourceBase, sourceNumber)
                else -> UNKNOWN_BASE
            }
        }"
    )
}*/

fun questionProduce(number: String, sourceBase: String, targetBase: String) {
    val splitIntPart: String
    val splitFractionPart: String
    val integerPart: String
    val fractionPart: String

    if (number.contains(".")) {
        splitIntPart = splitToIntegerPart(number)
        splitFractionPart = splitToFractionPart(number)

        integerPart = convertIntegerToTargetBase(splitIntPart, sourceBase, targetBase)
        fractionPart = convertFractionalToTargetBase(splitFractionPart, sourceBase, targetBase)
    } else {
        splitIntPart = splitToIntegerPart(number)

        integerPart = convertIntegerToTargetBase(splitIntPart, sourceBase, targetBase)
        fractionPart = ""
    }

    val fractionalNumber = combineToFractionalNumber(integerPart, fractionPart)
    println("Conversion result: $fractionalNumber")
}

// Split number to integer part from input
fun splitToIntegerPart(number: String) = number.substringBefore(".")

// Split number to fraction part from input -> and scale fraction to 5 point
fun splitToFractionPart(number: String) = number.substringAfter(".").substring(ZERO, SCALE)

// Converse integer to target base
fun convertIntegerToTargetBase(
    number: String,
    sourceBase: String = "$DECIMAL_BASE",
    targetBase: String = "$DECIMAL_BASE"
): String = BigInteger(number, sourceBase.toInt()).toString(targetBase.toInt())

// Converse fraction to target base
fun convertFractionalToTargetBase(fractionNum: String, sourceBase: String, targetBase: String): String {
    val fractionCollector = mutableListOf<String>()
    fractionCollector.addAll(
        calculateFraction(
            convertFractionToDecimalBaseList(fractionNum, sourceBase),
            targetBase
        )
    )
    return fractionCollector.joinToString("")
}

// Calculate value of fraction to target base
fun calculateFraction(fractionInDecimalBaseList: List<String>, targetBase: String): List<String> {
    println("fractions = $fractionInDecimalBaseList")
    val fraction = fractionInDecimalBaseList.sumOf { it.toBigDecimal().setScale(SCALE, RoundingMode.CEILING) }
    println("sum of fraction = $fraction")
    val remainderCollector = mutableListOf<String>()
    var remainder = fraction * targetBase.toBigDecimal()
    var counter = ONE

    fun checkZeroFraction(fraction: String) {
        if (fraction == ZERO_FRACTION) {
            remainderCollector.add("$ZERO")
        } else {
            remainderCollector.add(
                convertIntegerToTargetBase(
                    splitToIntegerPart(remainder.toString()),
                    targetBase = targetBase
                )
            )
            remainder -= splitToIntegerPart(remainder.toString()).toBigDecimal()
            remainder *= targetBase.toBigDecimal()
        }
    }

    while (counter <= SCALE) {
        checkZeroFraction(remainder.toString())
        counter++
    }
    return remainderCollector.toList()
}

// Converse fraction from any base to decimal
fun convertFractionToDecimalBaseList(fractionNum: String, sourceBase: String): List<String> {
    val fractionToDecimalBaseList = mutableListOf<String>()
    val fractionToValueInDecimalBaseList = mutableListOf<Int>()

    // Converse to decimal from any source
    for (digit in fractionNum) {
        val currentNum = convertIntegerToTargetBase(digit.toString(), sourceBase).toInt()
        fractionToValueInDecimalBaseList.add(currentNum)
    }
    // Converse to target
    for (index in fractionToValueInDecimalBaseList.indices) {
        fractionToDecimalBaseList.add(
            (fractionToValueInDecimalBaseList[index].toBigDecimal() * sourceBase.toBigDecimal()
                .pow(-index - ONE, MathContext.DECIMAL64)).toString()
        )
    }
    return fractionToDecimalBaseList.toList()
}

// Combine integer and fraction to BigDecimal number
fun combineToFractionalNumber(integerPart: String, fractionPart: String) = toFractionPattern(integerPart, fractionPart)

// Take fraction to string template
fun toFractionPattern(integerPart: String, fractionPart: String) =
    if (fractionPart != "") "$integerPart.$fractionPart" else integerPart