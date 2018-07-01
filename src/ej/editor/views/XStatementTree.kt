package ej.editor.views

import ej.mod.*
import ej.utils.squeezeWs
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.OverrunStyle
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import tornadofx.*

/*
 * Created by aimozg on 01.07.2018.
 * Confidential until published on GitHub
 */

fun statementTreeLabel(stmt:XStatement):String {
	return when(stmt) {
		is XlIf -> "If (${stmt.test})"
		is XsTextNode -> "Output \"${stmt.content.squeezeWs()}\""
		is XsOutput -> "Output (${stmt.expression.squeezeWs()})"
		else -> stmt.toSourceString().squeezeWs()
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
			isWrapText = false
			textOverrun = OverrunStyle.ELLIPSIS
			text = statementTreeLabel(it)
		}
		
		contentProperty.onChange {
			repopulate()
		}
	}
}