package ej.editor.expr

import ej.editor.EditorController
import ej.editor.utils.ListValueChooser
import ej.editor.utils.ValueChooser
import javafx.beans.property.Property
import javafx.scene.layout.Pane
import tornadofx.*
import kotlin.reflect.KProperty1

/*
 * Created by aimozg on 15.07.2018.
 * Confidential until published on GitHub
 */

interface WithReadableText {
	fun text():String
}
fun WithReadableText.mktext(vararg parts:Any?):String = mktext(parts.asList())
fun WithReadableText.mktext(parts:Iterable<Any?>):String = parts.joinToString("") {
	when(it) {
		null -> "<???>"
		is Property<*> -> {
			val v = it.value
			when(v) {
				null -> "<???>"
				is WithReadableText -> v.text()
				is String -> v
				else -> v.toString()
			}
		}
		is WithReadableText -> it.text()
		is String -> it
		else -> it.toString()
	}
}

abstract class VisualBuilder<T : Any> : WithReadableText {
	abstract fun build(): T
	abstract fun editorBody():Pane
	abstract fun name():String
	
	override fun toString(): String {
		return "<${name()}: ${build()}>"
	}
}

abstract class ExpressionBuilder : VisualBuilder<Expression>() {
	protected val controller: EditorController by lazy { find<EditorController>() }
	abstract fun copyMe(): ExpressionBuilder
	
	override fun equals(other: Any?): Boolean {
		val o = other as? ExpressionBuilder ?: return false
		return o.name() == name() && o.build() == build()
	}
	
	override fun hashCode(): Int {
		return name().hashCode().shl(31) + build().hashCode()
	}
	
	open fun initializableBy(initial: ExpressionBuilder): Boolean {
		return javaClass == initial.javaClass
	}
}

abstract class ExpressionChooser : ValueChooser<ExpressionBuilder>(){
	abstract fun list(): List<ExpressionBuilder>
	/*fun pickFromList(title:String,initial:ExpressionBuilder?,items:List<ExpressionBuilder>):ExpressionBuilder? {
		return find<ExpressionChooserDialog>().showModal(title,initial?.copyMe(),items)
	}*/
	
	override fun pickValue(title: String, initial: ExpressionBuilder?): ExpressionBuilder? {
		return find<ExpressionChooserDialog>().showModal(title, initial?.copyMe(), list())
	}
	
	open val expressionType: String = ExpressionTypes.ANY
	val noBuilder: ValueChooser<Expression> by lazy {
		object : ValueChooser<Expression>() {
			override fun pickValue(title: String, initial: Expression?): Expression? {
				val initialBuilder = initial?.let { DefaultBuilderConverter.convert(it, expressionType) }
				return pickValue(title, initialBuilder)?.build()
			}
		}
	}
}

open class EnumChooser<E : Enum<E>>(enumConsts: List<E>, val nameProperty: KProperty1<E, String>) : ListValueChooser<E>(
		enumConsts,
		{
	it?.let{ e -> nameProperty.get(e) } ?: "<Choose value>"
		}) {
	constructor(enumConsts: Array<E>, nameProperty: KProperty1<E, String>) : this(enumConsts.asList(), nameProperty)
}

