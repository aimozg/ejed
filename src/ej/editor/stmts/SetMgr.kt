package ej.editor.stmts

import ej.editor.Styles
import ej.editor.utils.bindingN
import ej.editor.utils.isNullOrEmpty
import ej.editor.utils.stringValueToggler
import ej.editor.views.StatementTree
import ej.mod.XsSet
import tornadofx.*

/*
 * Created by aimozg on 07.07.2018.
 * Confidential until published on GitHub
 */

object SetMgr : StatementManager<XsSet>() {
	override fun editorBody(stmt: XsSet) = defaultEditorBody {
		label(stmt.opProperty.stringBinding {
			when (it) {
				null, "=", "assign" -> "Set to "
				"+", "+=", "add" -> "Add "
				"-" -> "Subtract "
				"*" -> "Multiply by "
				"/" -> "Divide by "
				else -> it
			}
		})
		textfield(stmt.valueProperty)
		label(stmt.opProperty.stringBinding {
			when (it) {
				null, "=", "assign",
				"*", "/" -> "property"
				"+", "+=", "add" -> "to property"
				"-" -> "from property "
				else -> "property"
			}
		})
		textfield(stmt.varnameProperty) {
			prefColumnCount = 6
		}
		checkbox("of object", stringValueToggler(stmt.inobjProperty,"mod"))
		textfield(stmt.inobjProperty) {
			disableWhen { stmt.inobjProperty.isNullOrEmpty() }
			prefColumnCount = 6
		}
		
	}
	
	override fun treeGraphic(stmt: XsSet, tree: StatementTree) =
			simpleTreeLabel(
					bindingN(stmt.inobjProperty,
					         stmt.varnameProperty,
					         stmt.opProperty,
					         stmt.valueProperty){inobj,varname,op,value ->
				val s: String = if (inobj != null) {
					"property '$varname' of $inobj"
				} else {
					"variable '$varname'"
				}
				when (op) {
					"add", "+", "+=" -> "Add $value to $s"
					null, "set", "=" -> "Set $value to $s"
					else -> "Apply $op$value to $s"
				}
			}).addClass(Styles.xcommand)
	
}