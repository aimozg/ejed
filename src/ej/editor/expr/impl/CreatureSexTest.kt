package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.expr.lists.CreatureChooser
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*

/*
 * Created by aimozg on 15.07.2018.
 * Confidential until published on GitHub
 */
class CreatureSexTest : ExpressionBuilder() {
	override fun name() = "Creature: Check sex"
	
	override fun editorBody() = defaultBuilderBody {
		valueLink(creature, CreatureChooser, "<Creature>")
		text(" is ")
		valueLink(sex, EnumChooser(SexTest::shortName))
	}
	
	override fun text() = mktext(creature, " is ", sex)
	override fun build() = CallExpression(
			DotExpression(creature.value?.build() ?: nop(),
			              sex.value?.function ?: "error"),
			emptyList())
	
	val creature = SimpleObjectProperty<ExpressionBuilder?>(ConstPlayer())
	val sex = SimpleObjectProperty<SexTest?>()
	
	companion object: PartialBuilderConverter<CallExpression> {
		override fun tryConvert(converter: BuilderConverter, expr: CallExpression): CreatureSexTest? {
			if (expr.arguments.isNotEmpty()) return null
			val dot = expr.function.asDot ?: return null
			val test = SexTest.byFunction(dot.key) ?: return null
			return CreatureSexTest().apply {
				creature.value = converter.convert(dot.obj)
				sex.value = test
			}
		}
		
	}
	
	enum class SexTest(val shortName: String, val function: String) : WithReadableText {
		NONE("genderless", "isGenderless"),
		MALE("male", "isMale"),
		FEMALE("female", "isFemale"),
		HERM("herm", "isHerm"),
		MALE_OR_HERM("male or herm", "isMaleOrHerm"),
		FEMALE_OR_HERM("female or herm", "isMaleOrHerm");
		
		override fun text() = shortName
		
		companion object {
			fun byFunction(function: String) = values().firstOrNull { it.function == function }
		}
	}
}