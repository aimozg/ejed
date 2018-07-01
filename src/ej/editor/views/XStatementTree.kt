package ej.editor.views

import ej.editor.Styles
import ej.mod.*
import ej.utils.squeezeWs
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.OverrunStyle
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import tornadofx.*

/*
 * Created by aimozg on 01.07.2018.
 * Confidential until published on GitHub
 */

fun statementTreeGraphic(stmt:XStatement):Node {
	return when(stmt) {
		is XlIf -> Text("If: ${stmt.test}").addClass(Styles.xlogic)
		is XsTextNode -> Text(stmt.content).addClass(Styles.xtext)
		is XsOutput -> Text("Output: ${stmt.expression.squeezeWs()}").addClass(Styles.xcommand)
		else -> Text(stmt.toSourceString().squeezeWs()).addClass(Styles.xcommand)
	}
}

open class XStatementTree : TreeView<XStatement>() {
	val contentProperty = SimpleObjectProperty<MutableList<XStatement>>(ArrayList())
	var content by contentProperty
	
	private val fakeRoot = TreeItem<XStatement>()
	fun repopulate() {
		populate {
			val stmt = it.value
			when (stmt) {
				null -> if (it == fakeRoot) content else emptyList()
				is XContentContainer -> stmt.content
				else -> emptyList()
			}
		}
	}
	
	init {
		isShowRoot = false
		root = fakeRoot
		cellFormat {
			this.prefWidthProperty().bind(this@XStatementTree.widthProperty().subtract(5.0))
			isWrapText = true
			textOverrun = OverrunStyle.ELLIPSIS
			graphic = statementTreeGraphic(it)
		}
		
		contentProperty.onChange {
			repopulate()
		}
	}
}

open class XStatementTreeWithEditor : VBox() {
	var editor: Region = Pane()
	val tree:XStatementTree = XStatementTree().apply {
		vgrow = Priority.SOMETIMES
		minHeight = 100.0
	}
	val contentsProperty = tree.contentProperty
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