package ej.editor.expr

import ej.editor.utils.callAtZero
import ej.editor.utils.callDepthTracking
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.util.concurrent.atomic.AtomicInteger

open class ExpressionProperty(initialValue:String = "") : SimpleStringProperty(initialValue) {
	val expressionProperty = object: SimpleObjectProperty<Expression>() {
		override fun set(v: Expression) {
			mutating.callAtZero {
				this@ExpressionProperty.value = v.source
			}
			super.set(v)
		}
	}
	val builderProperty: Property<ExpressionBuilder?> by lazy {
		SimpleObjectProperty<ExpressionBuilder>(
				toBuilder()
		).apply {
			expressionProperty.onChange { set(toBuilder()) }
			onChange { if (it != null) fromBuilder(it) }
		}
	}
	fun fromBuilder(v: ExpressionBuilder) {
		mutating.callAtZero {
			val expr = v.build()
			expressionProperty.value = expr
			this@ExpressionProperty.value = expr.source
		}
	}
	fun toBuilder() = DefaultBuilderConverter.convert(expressionProperty.value)
	private val mutating = AtomicInteger(0)
	override fun set(v: String) {
		mutating.callAtZero {
			val expr = parseExpressionSafe(v)
			expressionProperty.value = expr
		}
		mutating.callDepthTracking {
			super.set(v)
		}
	}
	init {
		mutating.callDepthTracking {
			val expr = parseExpressionSafe(initialValue)
			expressionProperty.value = expr
		}
	}
}