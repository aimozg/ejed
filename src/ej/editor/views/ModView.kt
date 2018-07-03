package ej.editor.views

import ej.editor.AModView
import ej.editor.ModViewModel
import ej.editor.MonsterScope
import ej.mod.*
import javafx.geometry.Side
import javafx.scene.control.TreeItem
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

sealed class ModTreeNode {
	open fun population():List<ModTreeNode> = emptyList()
	abstract val text:String
	override fun toString() = text
	class MonsterListNode(val modVM:ModViewModel): ModTreeNode() {
		override fun population() = modVM.monsters.value.map { MonsterNode(it) }
		override val text get() = "Monsters"
	}
	class MonsterNode(val monster:MonsterData): ModTreeNode() {
		override val text get() = monster.id ?: ""
	}
	class StoryNode(val story:StoryStmt): ModTreeNode() {
		override val text get() = when(story) {
			is XcScene -> "(Scene) "
			is XcNamedText -> "(Text) "
			is XcLib -> "/"
			else -> ""
		} + story.name
		override fun population() = story.lib.map { StoryNode(it) }
	}
	class StoryListNode(val stories:List<StoryStmt>): ModTreeNode() {
		override val text get() = "Scenes"
		override fun population() = stories.map { StoryNode(it) }
	}
	class RootNode(val modVM:ModViewModel): ModTreeNode() {
		override fun population() = listOf(MonsterListNode(modVM)) + StoryListNode(modVM.item.content)
		override val text get() = modVM.item.name
	}
}

class ModView: AModView() {
	
	val treeWithEditor by lazy {
		XStatementTreeWithEditor().apply {
			vgrow = Priority.ALWAYS
		}
	}
	
	fun selectModEntry(e:ModTreeNode?) = when (e) {
			null -> null
			is ModTreeNode.RootNode -> ModOverviewPage().root
			is ModTreeNode.MonsterNode -> {
				val monsterScope = MonsterScope(modVM, e.monster)
				find<MonsterPage>(monsterScope).root
			}
			is ModTreeNode.MonsterListNode -> VBox().apply { text("TODO")} // TODO
			is ModTreeNode.StoryNode -> e.story.let { story ->
				when(story) {
					is XcLib -> VBox().apply { text("TODO") } // TODO
					is XContentContainer -> treeWithEditor.also {
						it.contents = story.content
						it.tree.root.expandAll()
					}
					else -> VBox().apply { text("Unknown $story") }
				}
			}
			is ModTreeNode.StoryListNode -> VBox().apply { text("NYI")} // TODO
		}
	
	override val root = borderpane {
		val main = this
		left = drawer(Side.LEFT) {
			item("Mod contents") {
				treeview<ModTreeNode> {
					root = TreeItem(ModTreeNode.RootNode(modVM))
					populate { it.value.population() }
					root.expandTo(4)
					onUserSelect {
						main.center = selectModEntry(it)
					}
				}
				expanded = true
			}
		}
	}
	
}