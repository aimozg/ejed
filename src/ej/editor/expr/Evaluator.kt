package ej.editor.expr

import ej.utils.escapeJs

/*
 * Created by aimozg on 26.07.2018.
 * Confidential until published on GitHub
 */

interface IStringValue {
	val stringValue: String
}

interface INumberValue {
	val intValue: Int
	val doubleValue: Double
}

sealed class Evaluated {
	abstract fun coerceToString(): IStringValue
	open fun repr(): String = super.toString()
	override fun toString() = repr()
	open fun coerceToNumber(): INumberValue = coerceToString().let { sv ->
		if (sv.stringValue.isEmpty()) IntValue(0)
		else sv.stringValue.toIntOrNull()?.let { IntValue(it) }
				?: FloatValue(sv.stringValue.toDoubleOrNull() ?: Double.NaN)
	}
	
	open fun isTrue(): Boolean = coerceToNumber().doubleValue.let { !it.isNaN() && it != 0.0 }
	open fun call(arguments: List<Evaluated> = emptyList()): Evaluated = ErrorValue("Not a function: $this")
	protected open fun getOrNull(key: String): Evaluated? = when (key) {
		"toString" -> FunctionValue {-> StringValue(coerceToString().stringValue) }
		else -> null
	}
	
	fun get(key: String): Evaluated = getOrNull(key) ?: NullValue
	
	class ErrorValue(val msg: String) : Evaluated(), IStringValue, INumberValue {
		override val stringValue: String = ""
		override val intValue: Int = 0
		override val doubleValue: Double = Double.NaN
		
		override fun isTrue(): Boolean = false
		override fun coerceToString(): IStringValue = this
		override fun coerceToNumber(): INumberValue = this
	}
	
	object NullValue : Evaluated() {
		override fun isTrue(): Boolean = false
		override fun repr() = "null"
		override fun coerceToString(): IStringValue = StringValue("null")
		override fun coerceToNumber(): INumberValue = IntValue(0)
	}
	
	object TrueValue : Evaluated() {
		override fun isTrue(): Boolean = true
		override fun repr() = "true"
		override fun coerceToString(): IStringValue = StringValue("true")
		override fun coerceToNumber(): INumberValue = IntValue(1)
	}
	
	object FalseValue : Evaluated() {
		override fun isTrue(): Boolean = false
		override fun repr() = "false"
		override fun coerceToString(): IStringValue = StringValue("false")
		override fun coerceToNumber(): INumberValue = IntValue(0)
	}
	
	data class IntValue(override val intValue: Int) : Evaluated(), INumberValue {
		override val doubleValue: Double get() = intValue.toDouble()
		override fun isTrue(): Boolean = intValue != 0
		override fun repr() = intValue.toString()
		override fun coerceToString(): IStringValue = StringValue(intValue.toString())
		override fun coerceToNumber(): INumberValue = this
	}
	
	data class FloatValue(override val doubleValue: Double) : Evaluated(), INumberValue {
		override val intValue get() = doubleValue.toInt()
		override fun repr() = doubleValue.toString()
		override fun coerceToString(): IStringValue = StringValue(doubleValue.toString())
		override fun coerceToNumber(): INumberValue = this
	}
	
	data class StringValue(override val stringValue: String) : Evaluated(), IStringValue {
		override fun isTrue(): Boolean = stringValue.isNotEmpty()
		override fun repr() = "\"" + stringValue.escapeJs() + "\""
		override fun coerceToString(): IStringValue = this
	}
	
	class ListValue(val values: List<Evaluated>) : Evaluated() {
		override fun isTrue(): Boolean = false
		override fun repr() = values.joinToString(prefix = "[", postfix = "]") { it.repr() }
		override fun coerceToString(): IStringValue = StringValue(values.joinToString(",") { it.coerceToString().stringValue })
	}
	
	class ObjectValue(val values: Map<String, Evaluated>) : Evaluated() {
		override fun isTrue(): Boolean = false
		override fun repr() = values.entries.joinToString(prefix = "{", postfix = "}") {
			"\"" + it.key.escapeJs() + "\": " + it.value.repr()
		}
		override fun coerceToString(): IStringValue = StringValue("[object Object]")
	}
	
	class ProxyValue(val operatorGet: (String) -> Evaluated) : Evaluated() {
		override fun coerceToString(): IStringValue = operatorGet("toString").call().coerceToString()
		
	}
	
	class FunctionValue(val arity: Int,
	                    val operatorCall: (List<Evaluated>) -> Evaluated) : Evaluated() {
		override fun coerceToString(): IStringValue = StringValue("[function Function]")
		override fun call(arguments: List<Evaluated>): Evaluated = operatorCall(arguments)
		
		constructor(call: () -> Evaluated) : this(0, { call() })
		constructor(call: (Evaluated) -> Evaluated) : this(0, { call(it[0]) })
	}
	
	companion object {
		fun BoolValue(value: Boolean) = if (value) TrueValue else FalseValue
	}
}

abstract class Evaluator {
	private val stack = ArrayList<Any>()
	fun evaluate(e: Expression): Evaluated {
		val rslt = when (e) {
			is Identifier -> evalId(e.value)
			is IntLiteral -> Evaluated.IntValue(e.value)
			is FloatLiteral -> Evaluated.FloatValue(e.value)
			is StringLiteral -> Evaluated.StringValue(e.value)
			is ListExpression -> Evaluated.ListValue(e.parts.map { evaluate(it) })
			is ObjectExpression -> Evaluated.ObjectValue(e.items.mapValues { (_, v) -> evaluate(v) })
			is CallExpression -> evaluate(e.function).call(
					e.arguments.map { evaluate(it) }
			)
			is DotExpression -> evaluate(e.obj).get(e.key)
			is AccessExpression -> evaluate(e.obj).get(evaluate(e.index).coerceToString().stringValue)
			is ConditionalExpression ->
				if (evaluate(e.condition).isTrue()) evaluate(e.ifTrue)
				else evaluate(e.ifFalse)
			is BinaryExpression ->
				when (e.op) {
					BinaryOperator.OR ->
						if (evaluate(e.left).isTrue()) Evaluated.TrueValue
						else Evaluated.BoolValue(evaluate(e.right).isTrue())
					BinaryOperator.AND ->
						if (!evaluate(e.left).isTrue()) Evaluated.FalseValue
						else Evaluated.BoolValue(evaluate(e.right).isTrue())
					else -> {
						val a = evaluate(e.left)
						val b = evaluate(e.right)
						val an = a.coerceToNumber()
						val bn = b.coerceToNumber()
						val ai = an.intValue
						val bi = bn.intValue
						val ad = an.doubleValue
						val bd = bn.doubleValue
						val astr = a.coerceToString().stringValue
						val bstr = b.coerceToString().stringValue
						val rslt: Evaluated
						rslt = when (e.op) {
							BinaryOperator.OR -> Evaluated.BoolValue(a.isTrue() || b.isTrue())
							BinaryOperator.AND -> Evaluated.BoolValue(a.isTrue() && b.isTrue())
							BinaryOperator.LT -> Evaluated.BoolValue(ad < bd)
							BinaryOperator.LTE -> Evaluated.BoolValue(ad <= bd)
							BinaryOperator.GT -> Evaluated.BoolValue(ad > bd)
							BinaryOperator.GTE -> Evaluated.BoolValue(ad >= bd)
							BinaryOperator.NEQ,
							BinaryOperator.EQ -> {
								val eq =
										when {
											a is Evaluated.StringValue || b is Evaluated.StringValue ->
												astr == bstr
											a is Evaluated.IntValue && b is Evaluated.IntValue ->
												ai == bi
											else -> false
										}
								Evaluated.BoolValue(if (e.op == BinaryOperator.EQ) eq else !eq)
							}
							BinaryOperator.ADD -> when {
								a is Evaluated.IntValue && b is Evaluated.IntValue ->
									Evaluated.IntValue(ai + bi)
								a is Evaluated.IntValue || a is Evaluated.FloatValue ->
									Evaluated.FloatValue(ad + bd)
								else ->
									Evaluated.StringValue(astr + bstr)
							}
							BinaryOperator.SUB -> when {
								a is Evaluated.IntValue && b is Evaluated.IntValue ->
									Evaluated.IntValue(ai - bi)
								else ->
									Evaluated.FloatValue(ad - bd)
							}
							BinaryOperator.MUL -> when {
								a is Evaluated.IntValue && b is Evaluated.IntValue ->
									Evaluated.IntValue(ai * bi)
								else ->
									Evaluated.FloatValue(ad * bd)
							}
							BinaryOperator.DIV -> when {
								a is Evaluated.IntValue && b is Evaluated.IntValue ->
									Evaluated.IntValue(ai / bi)
								else ->
									Evaluated.FloatValue(ad / bd)
							}
							BinaryOperator.MOD -> when {
								a is Evaluated.IntValue && b is Evaluated.IntValue ->
									Evaluated.IntValue(ai % bi)
								else ->
									Evaluated.FloatValue(ad % bd)
							}
						}
						rslt
					}
				}
			is InvalidExpression -> Evaluated.ErrorValue("InvalidExpression ${e.source}")
		}
		println("Evaluator $e -> $rslt")
		return rslt
	}
	
	abstract fun evalId(id: String): Evaluated
	
}