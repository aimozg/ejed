package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.expr.lists.CreatureChooser
import ej.editor.utils.EnumChooser
import ej.editor.utils.withPropertiesFrom
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*

/*
 * Created by aimozg on 17.07.2018.
 * Confidential until published on GitHub
 */

class CreatureStat : ExpressionBuilder() {
	override fun name() = "Creature: Stat"
	override fun copyMe() = CreatureStat().withPropertiesFrom(this,
	                                                          CreatureStat::creature,
	                                                          CreatureStat::stat)
	
	override fun editorBody() = defaultEditorTextFlow {
		valueLink(creature, "Creature", CreatureChooser)
		text("'s ")
		valueLink(stat, "Stat", EnumChooser(Stat::shortName))
	}
	
	override fun text() = mktext(creature, "'s ", stat)
	override fun build() = DotExpression(
			creature.value?.build() ?: nop(),
			stat.value?.impl ?: "error"
	)
	
	val creature = SimpleObjectProperty<ExpressionBuilder?>(ConstPlayer())
	val stat = SimpleObjectProperty<Stat>(Stat.STR)
	
	enum class Stat(val impl:String,val shortName:String): WithReadableText {
		STR("str","Strength"),
		TOU("tou","Toughness"),
		SPE("spe","Speed"),
		INT("inte","Intelligence"),
		WIS("wis","Wisdom"),
		LIB("lib","Libido"),
		SENS("sens","Sensitivity"),
		COR("cor","Corruption"),
		
		HP("HP","Hit Points"),
		LUST("lust","Lust"),
		LEVEL("level", "Level"),
		
		FEMININITY("femininity", "Femininity"),
		BODYTONE("tone", "Body tone"),
		BUTTRATING("buttRating", "Butt rating"),
		HIPRATING("hipRating", "Hip rating"),
		;
		
		override fun text() = shortName
		companion object {
			fun byImpl(impl: String) = values().firstOrNull { it.impl == impl }
		}
	}
	
	companion object : PartialBuilderConverter<DotExpression> {
		override fun tryConvert(converter: BuilderConverter, expr: DotExpression): ExpressionBuilder? {
			val stat = Stat.byImpl(expr.key) ?: return null
			return CreatureStat().apply {
				creature.value = converter.convert(expr.obj, ExpressionTypes.CREATURE)
				this.stat.value = stat
			}
		}
		
	}
}