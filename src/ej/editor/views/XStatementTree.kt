package ej.editor.views

import ej.editor.Styles
import ej.mod.*
import ej.utils.affixNonEmpty
import ej.utils.squeezeWs
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Label
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import tornadofx.*

/*
 * Created by aimozg on 01.07.2018.
 * Confidential until published on GitHub
 */

fun statementTreeGraphic(stmt: XStatement): Region {
	return when (stmt) {
		is XlIf -> Label("If: ${stmt.test}").addClass(Styles.xlogic)
		is XlElse -> Label("Else:").addClass(Styles.xlogic)
		is XlElseIf -> Label("Else if: ${stmt.test}").addClass(Styles.xlogic)
		is XcTextNode -> Label(stmt.content).addClass(Styles.xtext)

		is XsOutput -> Label("Output: ${stmt.expression.squeezeWs()}").addClass(Styles.xcommand)
		is XsDisplay -> Label("Display: ${stmt.ref}").addClass(Styles.xcommand)
		is XsBattle ->
			Label(
					"Battle ${stmt.monster}" + (stmt.options.affixNonEmpty(" with options: "))
			).addClass(Styles.xcommand)
		
		is XcLib -> Label("<lib ${stmt.name}>").addClass(Styles.xcomment)

		else -> Label("<Unknown/TODO> " + stmt.toSourceString().squeezeWs()).addClass(Styles.xcommand)
	}
}

open class XStatementTree : TreeView<XStatement>() {
	val contentsProperty = SimpleObjectProperty<MutableList<XStatement>>(ArrayList())
	var contents by contentsProperty
	
	private val fakeRoot = TreeItem<XStatement>()
	fun repopulate() {
		populate {
			val stmt = it.value
			when (stmt) {
				null -> if (it == fakeRoot) contents else emptyList()
				is XContentContainer -> stmt.content
				else -> emptyList()
			}
		}
	}
	
	init {
		isShowRoot = false
		root = fakeRoot
		val tree = this
		cellFormat {
			val cell = this
			this.prefWidthProperty().bind(tree.widthProperty().minus(16)) // vscrollbar
			this.maxWidthProperty().bind(tree.widthProperty().minus(16)) // vscrollbar
			graphic = statementTreeGraphic(it).also { g ->
				g.maxWidthProperty().bind(
					cell.maxWidthProperty()
							.doubleBinding(g.layoutXProperty()) { cellMaxWidth ->
								(cellMaxWidth?.toDouble()?:0.0) -
										g.layoutX -
										cell.paddingHorizontal.toDouble()
							}
				)
			}
		}
		
		contentsProperty.onChange {
			repopulate()
		}
	}
}

open class XStatementTreeWithEditor : VBox() {
	var editor: Region = Pane()
	val tree: XStatementTree = XStatementTree().apply {
		vgrow = Priority.SOMETIMES
		minHeight = 100.0
	}
	val contentsProperty = tree.contentsProperty
	var contents by contentsProperty
	
	init {
		vgrow = Priority.ALWAYS
		add(tree)
		
		tree.selectionModel.selectedItemProperty().onChange { treeItem ->
			editor.removeFromParent()
			val value = treeItem?.value ?: return@onChange
			editor = StmtEditorBody.bodyFor(value).also {
				it.vgrow = Priority.ALWAYS
				this@XStatementTreeWithEditor += it
			}
		}
	}
}