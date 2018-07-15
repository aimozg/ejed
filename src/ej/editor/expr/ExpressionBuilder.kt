package ej.editor.expr

import ej.editor.EditorController
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
abstract class ExpressionBuilder : WithReadableText {
	protected val controller:EditorController by lazy { find<EditorController>() }
	abstract fun build():Expression
	abstract fun editorBody():Pane
	abstract fun name():String
	protected fun mktext(vararg parts:Any?):String = parts.joinToString("") {
		when(it) {
			null -> "<???>"
			is Property<*> -> {
				val v = it.value
				when(v) {
					null -> "<???>"
//					is ExpressionBuilder -> "("+v.text()+")"
					is WithReadableText -> v.text()
					is String -> v
					else -> v.toString()
				}
			}
//			is ExpressionBuilder -> "("+it.text()+")"
			is WithReadableText -> it.text()
			is String -> it
			else -> it.toString()
		}
	}
}

abstract class ValueChooser<T:Any> {
	abstract fun pickValue(initial:T?=null):T?
	fun pickValueFor(prop:Property<T?>) {
		val v = pickValue(prop.value)
		if (v != null) prop.value = v
	}
}
open class ListValueChooser<T:Any>(val items: List<T>,
                              val formatter: (T?) -> String) : ValueChooser<T>() {
	override fun pickValue(initial: T?): T? {
		return find<ListChooserDialog<T>>().showModal(initial,items) {
			text = formatter(item)
		}
	}
	
}
abstract class ExpressionChooser : ValueChooser<ExpressionBuilder>(){
	fun pickFromList(initial:ExpressionBuilder?,items:List<ExpressionBuilder>):ExpressionBuilder? {
		return find<ExpressionChooserDialog>().showModal(initial,items)
	}
}

open class EnumChooser<E:Enum<E>>(val enumConsts:Array<E>,val nameProperty: KProperty1<E, String>) : ListValueChooser<E>(enumConsts.asList(),{
	it?.let{ e -> nameProperty.get(e) } ?: "<Choose value>"
})
inline fun<reified E:Enum<E>> EnumChooser(nameProperty:KProperty1<E, String>) =
		EnumChooser(enumValues<E>(),nameProperty)

