package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.FlashTextProcessor
import ej.editor.views.HtmlEditorLite
import ej.editor.views.StatementTree
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
	
	override fun editorBody(stmt: XcText) = vboxEditorBody() {
		(cachedEditor.getAndSet(null) ?: HtmlEditorLite().apply {
			hgrow = Priority.ALWAYS
			processor = FlashTextProcessor()
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
		val fnCollapsed = fnExpanded.not()
		textflow {
			addClass(Styles.xtext)
			prefWidthProperty().bind(g.widthProperty())
			maxWidthProperty().bind(g.widthProperty())
			text(stmt.textProperty())
			hiddenWhen(fnCollapsed)
			managedWhen(fnExpanded)
		}
		label(stmt.textProperty().stringBinding {it?.replace("\n"," ")}) {
			addClass(Styles.xtext)
			hiddenWhen(fnExpanded)
			managedWhen(fnCollapsed)
		}
	}
}