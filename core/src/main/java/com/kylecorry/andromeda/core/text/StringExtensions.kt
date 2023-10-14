package com.kylecorry.andromeda.core.text

// TODO: Make this more robust - ex. [Tes]t][ is not balanced
/**
 * Determines if the brackets in the string are balanced. The brackets are balanced if for every open bracket there is a corresponding close bracket.
 */
fun String.areBracketsBalanced(openBracket: Char, closeBracket: Char): Boolean {
    var total = 0
    forEach {
        if (it == openBracket) {
            total++
        }
        if (it == closeBracket) {
            total--
        }
    }
    return total == 0
}