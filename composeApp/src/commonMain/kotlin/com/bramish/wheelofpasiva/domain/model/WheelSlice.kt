package com.bramish.wheelofpasiva.domain.model

/**
 * Represents a slice on the wheel.
 * The wheel has 8 slices total.
 */
sealed class WheelSlice {
    /**
     * Points slice - adds points to the player's score.
     */
    data class Points(val value: Int) : WheelSlice()

    /**
     * Bankrupt slice - removes all the player's accumulated points.
     */
    data object Bankrupt : WheelSlice()

    /**
     * Extra Turn slice - allows the player to spin again.
     */
    data object ExtraTurn : WheelSlice()

    companion object {
        /**
         * The 8 slices on the wheel in order.
         * 2x100, 2x200, 2x300, 1xBankrupt, 1xExtraTurn
         */
        val allSlices: List<WheelSlice> = listOf(
            Points(100),
            Points(200),
            Points(300),
            Bankrupt,
            Points(100),
            Points(200),
            Points(300),
            ExtraTurn
        )

        /**
         * Gets the display text for a slice.
         */
        fun WheelSlice.displayText(): String = when (this) {
            is Points -> "$value"
            is Bankrupt -> "BANKRUPT"
            is ExtraTurn -> "EXTRA TURN"
        }

        /**
         * Gets the slice at a specific index.
         */
        fun getSliceAtIndex(index: Int): WheelSlice {
            return allSlices[index % allSlices.size]
        }
    }
}
