package ej.mod

import ej.editor.expr.ExpressionProperty
import ej.utils.affix
import ej.xml.XmlSerializableCompanion
import ej.xml.XmlSzInfoBuilder
import ej.xml.inherit
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*


class XsDisplay : XStatement {
	val refProperty = SimpleStringProperty("")
	var ref: String by refProperty
	
	override fun toString() = defaultToString("display","",ref)
	
	companion object : XmlSerializableCompanion<XsDisplay> {
		override val szInfoClass = XsDisplay::class
		
		override fun XmlSzInfoBuilder<XsDisplay>.buildSzInfo() {
			attribute(XsDisplay::ref)
			emptyBody()
		}
		
	}
}

class XsForward : XStatement {
	val refProperty = SimpleStringProperty("")
	var ref: String by refProperty
	
	override fun toString() = defaultToString("forward", "", ref)
	
	companion object : XmlSerializableCompanion<XsForward> {
		override val szInfoClass = XsForward::class
		
		override fun XmlSzInfoBuilder<XsForward>.buildSzInfo() {
			attribute(XsForward::ref)
			emptyBody()
		}
		
	}
}

class XsSet : XStatement {
	val varnameProperty = SimpleObjectProperty("")
	var varname: String by varnameProperty
	
	val inobjProperty = SimpleObjectProperty<String?>()
	var inobj: String? by inobjProperty
	
	// TODO enum
	val opProperty = SimpleObjectProperty<String?>( null)
	var op: String? by opProperty
	
	val valueProperty = ExpressionProperty("")
	var value: String by valueProperty
	
	
	override fun toString() = defaultToString(
			"set",
			"var='$varname'" +
			inobj.affix(" in='", "'") +
			op.affix(" op='", "'") +
			" value='$value'",
			"")
	
	companion object : XmlSerializableCompanion<XsSet> {
		override val szInfoClass = XsSet::class
		
		override fun XmlSzInfoBuilder<XsSet>.buildSzInfo() {
			attribute(XsSet::varname, "var")
			attribute(XsSet::inobj, "in")
			attribute(XsSet::op, "op")
			attribute(XsSet::value, "value")
			emptyBody()
		}
		
	}
}

class XsOutput : XStatement {
	val expressionProperty = SimpleStringProperty("")
	var expression:String by expressionProperty
	
	override fun toString() = defaultToString("output","",expression)
	
	companion object : XmlSerializableCompanion<XsOutput> {
		override val szInfoClass = XsOutput::class
		
		override fun XmlSzInfoBuilder<XsOutput>.buildSzInfo() {
			textBody(XsOutput::expression)
		}
		
	}
}

class XsMenu : XContentContainer() {
	
	override fun toString() = defaultToString("menu","")
	
	companion object : XmlSerializableCompanion<XsMenu> {
		override val szInfoClass = XsMenu::class
		
		override fun XmlSzInfoBuilder<XsMenu>.buildSzInfo() {
			inherit(XContentContainer)
		}
		
	}
}

class XsButton() : XStatement {
	constructor(text:String):this() {
		this.text =text
	}
	
	val textProperty = SimpleStringProperty("")
	var text:String by textProperty
	
	val disabledProperty = SimpleBooleanProperty(false)
	var disabled by disabledProperty
	
	val refProperty = SimpleStringProperty("")
	var ref:String by refProperty
	
	val posProperty = SimpleObjectProperty<Int?>(null)
	var pos:Int? by posProperty
	
	val hintProperty = SimpleObjectProperty<XsButtonHint?>()
	var hint by hintProperty
	
	
	override fun toString() = defaultToString("button", "" +
			"text='$text'" +
			(if (disabled) " disabled" else "") +
			" ref='$ref'", hint?.toString()?:"")
	
	companion object : XmlSerializableCompanion<XsButton> {
		override val szInfoClass = XsButton::class
		
		override fun XmlSzInfoBuilder<XsButton>.buildSzInfo() {
			attribute(XsButton::text)
			attribute(XsButton::disabled)
			attribute(XsButton::ref)
			attribute(XsButton::pos)
			element(XsButton::hint)
		}
		
	}
}

class XsButtonHint : XContentContainer() {
	var header: String? = null
	
	override fun toString() = defaultToString("hint", header.affix("header='", "'"))
	
	companion object : XmlSerializableCompanion<XsButtonHint> {
		override val szInfoClass = XsButtonHint::class
		
		override fun XmlSzInfoBuilder<XsButtonHint>.buildSzInfo() {
			inherit(XContentContainer)
			attribute(XsButtonHint::header)
		}
		
	}
}

class XsNext : XStatement {
	val refProperty = SimpleStringProperty("")
	var ref:String by refProperty
	
	
	override fun toString() = defaultToString("next", "", ref)
	
	companion object : XmlSerializableCompanion<XsNext>{
		override val szInfoClass = XsNext::class
		
		override fun XmlSzInfoBuilder<XsNext>.buildSzInfo() {
			attribute(XsNext::ref)
			emptyBody()
		}
		
	}
}

class XsBattle : XStatement {
	val monsterProperty = SimpleStringProperty("")
	var monster:String by monsterProperty
	
	val optionsProperty = SimpleObjectProperty<String?>()
	var options:String? by optionsProperty
	
	override fun toString() = defaultToString("battle",options.affix(" options='", "'"),monster)
	
	companion object : XmlSerializableCompanion<XsBattle> {
		override val szInfoClass = XsBattle::class
		
		override fun XmlSzInfoBuilder<XsBattle>.buildSzInfo() {
			attribute(XsBattle::monster)
			attribute(XsBattle::options)
			emptyBody()
		}
		
	}
}