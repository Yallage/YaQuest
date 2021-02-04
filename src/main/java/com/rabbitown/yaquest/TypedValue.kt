package com.rabbitown.yaquest

/**
 * @author Yoooooory
 */
class TypedValue(val type: String, val value: String) {
    companion object {
        fun String.toTypedValue(defaultType: String): TypedValue {
            val split = this.split("::", limit = 2)
            return if (split.size == 1) TypedValue(defaultType, this)
            else TypedValue(split[0].trim(), split[1].trim())
        }
    }
}