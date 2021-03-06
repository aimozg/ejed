package ej.editor.stmts.old

import ej.editor.Styles
import ej.editor.utils.presentWhen
import ej.editor.views.FlashTagFilter
import ej.editor.views.HtmlEditorLite
import ej.mod.XcText
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*
import java.util.concurrent.atomic.AtomicReference

/*
 * Created by aimozg on 07.07.2018.
 * Confidential until published on GitHub
 */

object TextMgr: StatementManager<XcText>() {
	private val cachedEditor = AtomicReference<HtmlEditorLite?>(null)
	
	override fun editorBody(stmt: XcText,
	                        tree: StatementTree
	) = defaultEditorBody(VBox()) {
		(cachedEditor.getAndSet(null) ?: HtmlEditorLite().apply {
			hgrow = Priority.ALWAYS
			processor = FlashTagFilter()
			sceneProperty().onChange {
				if (it == null) cachedEditor.compareAndSet(null, this)
			}
		}).attachTo(this) {
			bindHtmlContentBidirectional(stmt.textProperty())
		}
	}
	override fun treeGraphic(stmt: XcText, tree: StatementTree) = VBox().apply {
		val g = this
		val fnExpanded = tree.expandedNodesProperty.toBinding()
		textflow {
			addClass(Styles.xtext)
			prefWidthProperty().bind(g.widthProperty())
			maxWidthProperty().bind(g.widthProperty())
			text(stmt.textProperty())
			presentWhen(fnExpanded)
		}
		label(stmt.textProperty().stringBinding {
			it?.replace("<br>","\\n")?.replace("\n"," ")
		}) {
			addClass(Styles.xtext)
			presentWhen(fnExpanded.not())
		}
	}
}