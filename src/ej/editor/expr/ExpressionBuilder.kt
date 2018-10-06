package ej.editor.expr

import ej.editor.EditorController
import javafx.beans.property.Property
import javafx.beans.value.WritableValue
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
abstract class ExpressionBuilder : WithReadableText {
	protected val controller:EditorController by lazy { find<EditorController>() }
	abstract fun build():Expression
	abstract fun editorBody():Pane
	abstract fun name():String
	abstract fun copyMe():ExpressionBuilder
	
	override fun toString(): String {
		return "<${name()}: ${build()}>"
	}
}

abstract class ValueChooser<T:Any> {
	abstract fun pickValue(title:String,initial:T?=null):T?
	fun pickValueFor(title:String,prop:WritableValue<T?>):T? {
		val v = pickValue(title,prop.value)
		if (v != null) prop.value = v
		return v
	}
}
abstract class AbstractListValueChooser<T:Any>(val items: List<T>): ValueChooser<T>() {
	open fun formatter(item:T?):String = item?.toString()?:"<???>"
	override fun pickValue(title:String,initial: T?): T? {
		return find<ListChooserDialog<T>>().showModal(title,initial,items) {
			text = formatter(item)
		}
	}
}
open class ListValueChooser<T:Any>(items: List<T>,
                                   val formatterFn: (T?) -> String) : AbstractListValueChooser<T>(items) {
	override fun formatter(item: T?): String = formatterFn(item)
	
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

open class EnumChooser<E:Enum<E>>(val enumConsts:Array<E>,val nameProperty: KProperty1<E, String>) : ListValueChooser<E>(enumConsts.asList(),{
	it?.let{ e -> nameProperty.get(e) } ?: "<Choose value>"
})
inline fun<reified E:Enum<E>> EnumChooser(nameProperty:KProperty1<E, String>) =
		EnumChooser(enumValues(),nameProperty)

