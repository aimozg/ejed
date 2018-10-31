package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.KnownIds
import ej.editor.expr.lists.AnyExprChooser
import ej.editor.utils.*
import ej.mod.XsSet
import tornadofx.*

class ScSet(stmt: XsSet) : StatementControl<XsSet>(stmt) {
	override fun createDefaultSkin() = SetSkin()
	
	enum class SetStmtKind(val displayName: String) {
		LOCAL("local variable"),
		MOD_STATE("mod variable"),
		PLAYER_PROP("player property"),
		OTHER_OBJECT_PROP("object property");
		
		override fun toString() = displayName
		
		companion object {
			
			fun fromObj(inobj: String?): SetStmtKind = when (inobj) {
				null -> LOCAL
				KnownIds.MOD_STATE -> MOD_STATE
				KnownIds.PLAYER -> PLAYER_PROP
				else -> OTHER_OBJECT_PROP
			}
		}
	}
	
	@Suppress("RedundantLambdaArrow")
	val stmtKindProperty = object : WritableExpression<SetStmtKind>() {
		override fun doGet(): SetStmtKind = SetStmtKind.fromObj(stmt.inobj)
		
		override fun doSet(value: SetStmtKind) {
			stmt.inobj = when (value) {
				SetStmtKind.LOCAL -> null
				SetStmtKind.MOD_STATE -> KnownIds.MOD_STATE
				SetStmtKind.PLAYER_PROP -> KnownIds.PLAYER
				SetStmtKind.OTHER_OBJECT_PROP ->
					if (SetStmtKind.fromObj(stmt.inobj) == SetStmtKind.OTHER_OBJECT_PROP) stmt.inobj
					else ""
			}
		}
		
		init {
			stmt.inobjProperty.addListener { _ -> markInvalid() }
		}
	}
	
	inner class SetSkin : ScSkin<XsSet, ScSet>(this, {
		addClass(Styles.xcommand)
		scFlow(Styles.xcommand) {
			val words = // X prop Y - set smth to, increase smth by
					stmt.opProperty.objectBinding {
						when (it) {
							null, "=", "assign" ->
								Pair("Set ", " to ")
							"+", "+=", "add" ->
								Pair("Increase ", " by ")
							"-" ->
								Pair("Decrease ", " by ")
							"*" ->
								Pair("Multiply ", " by ")
							"/" ->
								Pair("Divide ", " by ")
							else ->
								Pair(it, " ")
						}
					}
			// (Set/increase) (mod variable/object property) (property name) [of object (object name)] (to/by) (value)
			
			// Set/increase/...
			text(words.stringBinding { it?.first })
			
			// mod variable/object property/...
			combobox(stmtKindProperty, SetStmtKind.values().asList())
			
			// property name
			textfield(stmt.varnameProperty) {
				presentWhen(stmtKindProperty.isEqualToAny(
						SetStmtKind.OTHER_OBJECT_PROP,
						SetStmtKind.PLAYER_PROP, // TODO externalize player properties
						SetStmtKind.LOCAL))
				prefColumnCount = 6
			}
			combobox(stmt.varnameProperty, mod()?.stateVars?.transformed { it.name }) {
				presentWhen(stmtKindProperty.isEqualToAny(SetStmtKind.MOD_STATE))
			}
			
			// of object (name)
			text(" of object ") {
				presentWhen(stmtKindProperty.isEqualTo(SetStmtKind.OTHER_OBJECT_PROP))
			}
			textfield(stmt.inobjProperty, NullableStringConverter) {
				presentWhen(stmtKindProperty.isEqualTo(SetStmtKind.OTHER_OBJECT_PROP))
				prefColumnCount = 6
			}
			
			// to/by/...
			text(words.stringBinding { it?.second })
			
			// value
			textfield(stmt.valueProperty)
			button("...") {
				action {
					AnyExprChooser.pickValue("Value", stmt.valueProperty.toBuilder())?.let { v ->
						stmt.valueProperty.fromBuilder(v)
					}
				}
			}
		}
	})
}