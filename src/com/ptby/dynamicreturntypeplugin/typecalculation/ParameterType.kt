package com.ptby.dynamicreturntypeplugin.typecalculation

public class ParameterType(private val parameterType: String?) {

    override fun toString(): String {
        throw RuntimeException( "use toNullableString" )
    }


    fun toNullableString(): String?{
        return cleanReturnTypeOfPreviousCalls()
    }


    private fun cleanReturnTypeOfPreviousCalls(): String? {
        val formattableType = parameterType
        if (formattableType == null) {
            return null
        }

        val returnTypeParts = formattableType.split(":")
        var returnTypePart = returnTypeParts[returnTypeParts.size - 1]
        if (returnTypePart.length() > 2 && returnTypePart.substring(0, 2) == "#C") {
            return returnTypePart.substring(2)
        }

        if (returnTypePart.length() > 2 && returnTypePart.substring(returnTypePart.length() - 2) == "|?") {
            returnTypePart = returnTypePart.substring(0, returnTypePart.length() - 2)
        }


        return returnTypePart
    }
}
