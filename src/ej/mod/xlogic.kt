package ej.mod

import ej.editor.expr.Expression
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
	var testExpression: Expression by testProperty.expressionProperty
	
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
	var testExpression: Expression by testProperty.expressionProperty
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
			handleElement("switch",handlerForElement("case")!!)
			element(XlSwitch::defaultBranch,"default")
		}
		
	}
}
sealed class PartOfSwitch: XContentContainer()
class XlSwitchCase : PartOfSwitch() {
	enum class ConditionType {
		NEVER,
		TEST,
		X_EQ_A,
		X_NEQ_A,
		X_GT_A,
		X_GTE_A,
		X_LT_A,
		X_LTE_A,
		X_LTE_A_X_GTE_B,
		X_LT_A_X_GT_B,
		X_LTE_A_X_GT_B,
		X_LT_A_X_GTE_B,
		OTHER;
		
		open operator fun plus(other: ConditionType): ConditionType = when {
			this == NEVER -> other
			other == NEVER -> this
			this == other -> this
			this == OTHER || other == OTHER -> OTHER
			this == X_LT_A && other == X_GT_A ||
					other == X_LT_A && this == X_GT_A -> X_LT_A_X_GT_B
			this == X_LTE_A && other == X_GT_A ||
					other == X_LTE_A && this == X_GT_A -> X_LT_A_X_GT_B
			this == X_LT_A && other == X_GTE_A ||
					this == X_GTE_A && other == X_LT_A -> X_LT_A_X_GT_B
			this == X_LTE_A && other == X_GTE_A ||
					this == X_GTE_A && other == X_LTE_A -> X_LTE_A_X_GTE_B
			else -> OTHER
		}
	}
	
	fun getConditionType(): ConditionType {
		var ct: ConditionType = ConditionType.NEVER
		if (test != null) ct += ConditionType.TEST
		if (value != null) ct += ConditionType.X_EQ_A
		if (ne != null) ct += ConditionType.X_NEQ_A
		if (gt != null) ct += ConditionType.X_GT_A
		if (gte != null) ct += ConditionType.X_GTE_A
		if (lt != null) ct += ConditionType.X_LT_A
		if (lte != null) ct += ConditionType.X_LTE_A
		return ct
	}
	
	fun setConditionType(ct: ConditionType) {
		val properties = arrayListOf(
				testProperty,
				valueProperty,
				neProperty,
				gtProperty,
				gteProperty,
				ltProperty,
				lteProperty)
		val stub = ne ?: gt ?: gte ?: lt ?: lte ?: test ?: ""
		when (ct) {
			ConditionType.NEVER -> {
			}
			ConditionType.TEST -> properties -= testProperty
			ConditionType.X_EQ_A -> {
				if (value == null) value = stub
				properties -= valueProperty
			}
			ConditionType.X_NEQ_A -> {
				if (ne == null) ne = stub
				properties -= neProperty
			}
			ConditionType.X_GT_A -> {
				if (gt == null) gt = stub
				properties -= gtProperty
			}
			ConditionType.X_GTE_A -> {
				if (gte == null) gte = stub
				properties -= gteProperty
			}
			ConditionType.X_LT_A -> {
				if (lt == null) lt = stub
				properties -= ltProperty
			}
			ConditionType.X_LTE_A -> {
				if (lt == null) lte = stub
				properties -= lteProperty
			}
			ConditionType.X_LTE_A_X_GTE_B -> {
				if (lte == null) lte = lt ?: stub
				if (gte == null) gte = gt ?: stub
				properties -= lteProperty
				properties -= gteProperty
			}
			ConditionType.X_LT_A_X_GT_B -> {
				if (lt == null) lt = lte ?: stub
				if (gt == null) gt = gte ?: stub
				properties -= ltProperty
				properties -= gtProperty
			}
			ConditionType.X_LTE_A_X_GT_B -> {
				if (lte == null) lte = lt ?: stub
				if (gt == null) gt = gte ?: stub
				properties -= lteProperty
				properties -= gtProperty
			}
			ConditionType.X_LT_A_X_GTE_B -> {
				if (lt == null) lt = lte ?: stub
				if (gte == null) gte = gt ?: stub
				properties -= ltProperty
				properties -= gteProperty
			}
			ConditionType.OTHER -> properties.clear()
		}
		for (property in properties) {
			property.set(null)
		}
	}
	
	val conditionTypeProperty = object : SimpleObjectProperty<ConditionType>(ConditionType.NEVER) {
		private var updating = 0
		
		override fun setValue(v: ConditionType?) {
			super.setValue(v ?: ConditionType.NEVER)
			if (updating == 0) {
				setConditionType(v ?: ConditionType.NEVER)
			}
		}
		
		override fun getValue(): ConditionType {
			val stored = super.getValue() ?: ConditionType.NEVER
			val actual = getConditionType()
			if (stored != actual) runLater {
				try {
					updating++
					value = actual
				} finally {
					updating--
				}
			}
			return stored
		}
	}
	
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