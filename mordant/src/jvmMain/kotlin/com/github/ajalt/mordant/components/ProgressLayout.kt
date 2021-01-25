package com.github.ajalt.mordant.components

import com.github.ajalt.mordant.rendering.DEFAULT_STYLE
import com.github.ajalt.mordant.rendering.Renderable
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextStyle
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.grid

open class ProgressBuilder internal constructor() {
    var padding: Int = 2

    fun text(text: String, style: TextStyle = DEFAULT_STYLE) {
        cells += TextProgressCell(Text(text, style))
    }

    fun percentage(style: TextStyle = DEFAULT_STYLE) {
        cells += PercentageProgressCell(style)
    }

    fun progressBar(width: Int? = null) {
        cells += BarProgressCell(width)
    }

    fun completed(suffix: String = "", includeTotal: Boolean = true, style: TextStyle = DEFAULT_STYLE) {
        cells += CompletedProgressCell(suffix, includeTotal, style)
    }

    fun speed(suffix: String = "it/s", style: TextStyle = DEFAULT_STYLE, frameRate: Int? = 1) {
        cells += SpeedProgressCell(suffix, frameRate, style)
    }

    fun timeRemaining(prefix: String = "eta ", style: TextStyle = DEFAULT_STYLE, frameRate: Int? = 1) {
        cells += EtaProgressCell(prefix, frameRate, style)
    }

    internal fun build(): ProgressLayout {
        return ProgressLayout(cells, padding)
    }

    private val cells = mutableListOf<ProgressCell>()
}

class ProgressLayout internal constructor(
    private val cells: List<ProgressCell>,
    private val paddingSize: Int,
) {
    fun build(
        completed: Long,
        total: Long? = null,
        elapsedSeconds: Double = 0.0,
        completedPerSecond: Double? = null,
    ): Renderable {
        val cps = completedPerSecond ?: when {
            completed <= 0 || elapsedSeconds <= 0 -> 0.0
            else -> completed.toDouble() / elapsedSeconds
        }
        val state = ProgressState(
            completed = completed,
            total = total,
            completedPerSecond = cps,
            elapsedSeconds = elapsedSeconds,
        )
        return grid {
            rowFrom(cells.map { it.makeRenderable(state) })
            align = TextAlign.RIGHT
            borders = Borders.NONE
            cells.forEachIndexed { i, it ->
                column(i) {
                    padding = when (i) {
                        cells.lastIndex -> Padding.none()
                        else -> Padding.of(right = paddingSize)
                    }
                    // Expand fixed columns to account for padding
                    width = when (i) {
                        cells.lastIndex -> it.columnWidth
                        else -> (it.columnWidth as? ColumnWidth.Fixed)
                            ?.let { ColumnWidth.Fixed(it.width + paddingSize) }
                            ?: it.columnWidth
                    }
                }
            }
        }
    }

    private fun ProgressCell.makeRenderable(state: ProgressState): Renderable = state.makeRenderable()
}

fun progressLayout(init: ProgressBuilder.() -> Unit): ProgressLayout {
    return ProgressBuilder().apply(init).build()
}
