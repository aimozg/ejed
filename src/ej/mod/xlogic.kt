package ej.mod

import ej.editor.expr.ExpressionProperty
import ej.utils.affix
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlTransient

@XmlRootElement(name="comment")
class XlComment(): XStatement {
	constructor(text:String):this(){
		this.text = text
	}
	
	@XmlTransient
	val textProperty = SimpleStringProperty("")
	@get:XmlAttribute
	var text: String by textProperty
	
	override fun toString() = "[# $text #]"
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
class XlIf(): XStatement {
	constructor(test:String):this() {
		this.test = test
	}
	
	val testProperty = ExpressionProperty("")
	var test:String by testProperty
	
	val thenGroup = XlThen()
	
	val elseifGroups = ArrayList<XlElseIf>().observable()
	
	val elseGroupProperty = SimpleObjectProperty<XlElse?>()
	var elseGroup: XlElse? by elseGroupProperty
	
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

@XmlRootElement(name="if")
internal class XmlFlatIf() : XContentContainer(), XStatement {
	constructor(test:String):this() {
		this.test = test
	}
	
	@XmlTransient
	val testProperty = SimpleStringProperty("")
	@get:XmlAttribute
	var test: String by testProperty
	
	override fun toString() = defaultToString("if","test=$test")
}

@XmlRootElement(name="else")
internal class XmlFlatElse : XStatement {
	override fun toString() = "[else]"
}

@XmlRootElement(name="elseif")
internal class XmlFlatElseif() : XStatement {
	constructor(test:String):this() {
		this.test = test
	}
	
	@XmlTransient
	val testProperty = SimpleStringProperty("")
	@get:XmlAttribute
	var test: String by testProperty
	
	override fun toString() = defaultToString("elseif","test='$test'","")
}

@XmlRootElement(name="switch")
class XlSwitch : XStatement {
	
	@XmlTransient
	val valueProperty = SimpleStringProperty(null)
	@get:XmlAttribute
	var value:String? by valueProperty
	
	@XmlElement(name="switch")
	val branches = ArrayList<XlSwitchCase>().observable()
	
	@XmlTransient
	val defaultBranchProperty = SimpleObjectProperty<XlSwitchDefault?>(null)
	@get:XmlElement(name="default")
	var defaultBranch: XlSwitchDefault? by defaultBranchProperty
	
	override fun toString() = defaultToString("switch",value.affix("value="),branches.joinToString()+defaultBranch?.toString()?.affix(" "))
	
}
sealed class PartOfSwitch: XContentContainer()
@XmlRootElement(name="case")
class XlSwitchCase : PartOfSwitch() {
	
	@XmlTransient
	val testProperty = SimpleStringProperty(null)
	@get:XmlAttribute(name="test")
	var test:String? by testProperty
	
	@XmlTransient
	val valueProperty = SimpleStringProperty(null)
	@get:XmlAttribute(name="value")
	var value:String? by valueProperty
	
	@XmlTransient
	val neProperty = SimpleStringProperty(null)
	@get:XmlAttribute(name="ne")
	var ne:String? by neProperty
	
	@XmlTransient
	val ltProperty = SimpleStringProperty(null)
	@get:XmlAttribute(name="lt")
	var lt:String? by ltProperty
	
	@XmlTransient
	val gtProperty = SimpleStringProperty(null)
	@get:XmlAttribute(name="gt")
	var gt:String? by gtProperty
	
	@XmlTransient
	val lteProperty = SimpleStringProperty(null)
	@get:XmlAttribute(name="lte")
	var lte:String? by lteProperty
	
	@XmlTransient
	val gteProperty = SimpleStringProperty(null)
	@get:XmlAttribute(name="gte")
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
}

@XmlRootElement(name="default")
class XlSwitchDefault : PartOfSwitch() {
	override fun toString() = defaultToString("default")
}