package io.github.subiyacryolite.jds

import java.io.Serializable

data class Options(
        /**
         * A value indicating whether JDS should print internal log information
         */
        var logOutput: Boolean = false,

        /**
         * A collection of tags to ignore when both saving and loading (use to filter out fields tagged as "Sensitive" for example)
         */
        var ignoreTags: Set<String> = emptySet()

) : Serializable