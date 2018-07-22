package ej.mod

import ej.editor.expr.ExpressionProperty
import ej.editor.utils.ObservableSingletonList
import ej.editor.utils.observableConcatenation
import ej.utils.affix
import ej.xml.*
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class XlComment() : XStatement {
	constructor(text: String) : this() {
		this.text = text
	}
	
	val textProperty = SimpleStringProperty("")
	var text: String by textProperty
	
	override fun toString() = "[# $text #]"
	
	companion object : XmlSerializableCompanion<XlComment> {
		override val szInfoClass = XlComment::class
		
		override fun XmlSzInfoBuilder<XlComment>.buildSzInfo() {
			textBody(XlComment::text)
			handleAttribute("text") {
				text = it
			}
		}
		
	}
}

/*
 * if 1:
 *     then:
 *         content-1
 *     elseif 2:
 *         content-2
 *     elseif 3:
 *         content-3
 *     else:
 *         content-4
 * ----
 * flat-if 1:
 *     content-1
 *     flat-elseif 2
 *     content-2
 *     flat-elseif 3
 *     content-3
 *     flat-else
 *     content-4
 */
class XlIf(): XStatement,XmlAutoSerializable {
	constructor(test:String):this() {
		this.test = test
	}
	
	val testProperty = ExpressionProperty("")
	@Attribute
	var test:String by testProperty
	
	@Element("then")
	val thenGroup = XlThen()
	
	@Elements("elseif")
	val elseifGroups = ArrayList<XlElseIf>().observable()
	
	val elseGroupProperty = SimpleObjectProperty<XlElse?>()
	@Element("else")
	var elseGroup: XlElse? by elseGroupProperty
	
	val allGroups = observableConcatenation(
			listOf(thenGroup).observable(),
			elseifGroups,
			ObservableSingletonList(elseGroupProperty)
	)
	
	override fun toString() = defaultToString("if","test=$test",
	                                          thenGroup.toString()+" "+
			                                          elseifGroups.joinToString(" ")+
			                                          (elseGroup?.toString()?:""))
}
sealed class PartOfIf: XContentContainer()
class XlThen: PartOfIf() {
	override fun toString() = defaultToString("then")
}

class XlElseIf(): PartOfIf() {
	constructor(test:String):this() {
		this.test = test
	}
	val testProperty = ExpressionProperty("")
	@Attribute
	var test: String by testProperty
	override fun toString() = defaultToString("elseif","test=$test")
}

class XlElse: PartOfIf() {
	override fun toString() = defaultToString("else")
}
internal fun XmlFlatIf.grouped():XlIf {
	val g = XlIf(test)
	var current: XContentContainer = g.thenGroup
	for (stmt in content) {
		when (stmt) {
			is XmlFlatElseif -> {
				current = XlElseIf(stmt.test)
				g.elseifGroups.add(current)
			}
			is XmlFlatElse -> {
				current = XlElse()
				g.elseGroup = current
			}
			else -> current.content.add(stmt)
		}
	}
	return g
}
internal fun XlIf.ungrouped():XmlFlatIf {
	val u = XmlFlatIf(test)
	u.content.addAll(thenGroup.content)
	for (e in elseifGroups) {
		u.content.add(XmlFlatElseif(e.test))
		u.content.addAll(e.content)
	}
	elseGroup?.let { e ->
		u.content.add(XmlFlatElse())
		u.content.addAll(e.content)
	}
	return u
}

internal class XmlFlatIf() : XContentContainer(), XStatement {
	constructor(test:String):this() {
		this.test = test
	}
	
	val testProperty = SimpleStringProperty("")
	var test: String by testProperty
	
	override fun toString() = defaultToString("if","test=$test")
	
	companion object : XmlSerializableCompanion<XmlFlatIf> {
		override val szInfoClass = XmlFlatIf::class
		
		override fun XmlSzInfoBuilder<XmlFlatIf>.buildSzInfo() {
			inherit(XContentContainer)
			attribute(XmlFlatIf::test)
		}
	}
}

internal class XmlFlatElse : XStatement {
	override fun toString() = "[else]"
	
	companion object : XmlSerializableCompanion<XmlFlatElse> {
		override val szInfoClass = XmlFlatElse::class
		
		override fun XmlSzInfoBuilder<XmlFlatElse>.buildSzInfo() {
			emptyBody()
		}
	}
}

internal class XmlFlatElseif() : XStatement {
	constructor(test:String):this() {
		this.test = test
	}
	
	val testProperty = SimpleStringProperty("")
	var test: String by testProperty
	
	override fun toString() = defaultToString("elseif","test='$test'","")
	
	companion object : XmlSerializableCompanion<XmlFlatElseif> {
		override val szInfoClass = XmlFlatElseif::class
		
		override fun XmlSzInfoBuilder<XmlFlatElseif>.buildSzInfo() {
			attribute(XmlFlatElseif::test)
			emptyBody()
		}
		
	}
}

class XlSwitch : XStatement {
	
	val valueProperty = SimpleStringProperty(null)
	var value:String? by valueProperty
	
	val branches = ArrayList<XlSwitchCase>().observable()
	
	val defaultBranchProperty = SimpleObjectProperty<XlSwitchDefault?>(null)
	var defaultBranch: XlSwitchDefault? by defaultBranchProperty
	
	val allGroups = observableConcatenation(
			branches,
			ObservableSingletonList(defaultBranchProperty))
	
	override fun toString() = defaultToString("switch",value.affix("value="),branches.joinToString()+defaultBranch?.toString()?.affix(" "))
	
	companion object : XmlSerializableCompanion<XlSwitch> {
		override val szInfoClass = XlSwitch::class
		
		override fun XmlSzInfoBuilder<XlSwitch>.buildSzInfo() {
			attribute(XlSwitch::value)
			elements("case",XlSwitch::branches)
			element(XlSwitch::defaultBranch,"default")
		}
		
	}
}
sealed class PartOfSwitch: XContentContainer()
class XlSwitchCase : PartOfSwitch() {
	
	val testProperty = SimpleStringProperty(null)
	var test:String? by testProperty
	
	val valueProperty = SimpleStringProperty(null)
	var value:String? by valueProperty
	
	val neProperty = SimpleStringProperty(null)
	var ne:String? by neProperty
	
	val ltProperty = SimpleStringProperty(null)
	var lt:String? by ltProperty
	
	val gtProperty = SimpleStringProperty(null)
	var gt:String? by gtProperty
	
	val lteProperty = SimpleStringProperty(null)
	var lte:String? by lteProperty
	
	val gteProperty = SimpleStringProperty(null)
	var gte:String? by gteProperty
	
	override fun toString() = defaultToString(
			"case", test.affix("test=") +
			value.affix(" value=")+
			ne.affix(" ne=")+
			lt.affix(" lt=")+
			gt.affix(" gt=")+
			lte.affix(" lte=")+
			gte.affix(" gte=")
	)
	
	companion object : XmlSerializableCompanion<XlSwitchCase> {
		override val szInfoClass = XlSwitchCase::class
		
		override fun XmlSzInfoBuilder<XlSwitchCase>.buildSzInfo() {
			inherit(XContentContainer)
			attribute(XlSwitchCase::test, "test")
			attribute(XlSwitchCase::value, "value")
			attribute(XlSwitchCase::ne, "ne")
			attribute(XlSwitchCase::lt, "lt")
			attribute(XlSwitchCase::gt, "gt")
			attribute(XlSwitchCase::lte, "lte")
			attribute(XlSwitchCase::gte, "gte")
		}
		
	}
}

class XlSwitchDefault : PartOfSwitch() {
	override fun toString() = defaultToString("default")
	
	companion object : XmlSerializableCompanion<XlSwitchDefault> {
		override val szInfoClass = XlSwitchDefault::class
		
		override fun XmlSzInfoBuilder<XlSwitchDefault>.buildSzInfo() {
			inherit(XContentContainer)
		}
		
	}
}