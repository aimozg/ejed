package ej.editor.stmts

import ej.editor.Styles
import ej.editor.utils.binding4
import ej.editor.utils.isNullOrEmpty
import ej.editor.views.StatementTree
import ej.mod.XsSet
import javafx.beans.property.SimpleObjectProperty
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
		checkbox("of object") {
			selectedProperty().bindBidirectional(object : SimpleObjectProperty<Boolean>() {
				override fun setValue(v: Boolean?) {
					if (v == true) {
						if (stmt.inobj.isEmpty()) {
							stmt.inobj = "mod"
						}
					} else {
						stmt.inobj = ""
					}
				}
				
				init {
					bind(stmt.inobjProperty.isNullOrEmpty().not())
				}
			})
		}
		textfield(stmt.inobjProperty) {
			disableWhen { stmt.inobjProperty.isNullOrEmpty() }
			prefColumnCount = 6
		}
		
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