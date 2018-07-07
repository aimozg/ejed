package ej.editor.stmts

import ej.editor.Styles
import ej.editor.utils.binding4
import ej.editor.views.StatementTree
import ej.mod.XsSet
import tornadofx.*

/*
 * Created by aimozg on 07.07.2018.
 * Confidential until published on GitHub
 */

object SetMgr : StatementManager<XsSet>() {
	override fun editorBody(stmt: XsSet) = defaultEditorBody() {
		label("Property ")
		textfield(stmt.varname)
		checkbox("in object") {
			isDisable = stmt.inobj.isNullOrBlank()
		}
		when (stmt.op) {
			null, "=", "assign" -> label("set to")
			"+", "+=", "add" -> label("add ")
			"-" -> label("subtract ")
			"*" -> label("multiply by ")
			"/" -> label("divide by ")
		}
		textfield(stmt.value)
	}
	
	override fun treeGraphic(stmt: XsSet, tree: StatementTree) =
			simpleTreeLabel(
					binding4(stmt.inobjProperty,
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