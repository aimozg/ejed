package ej.editor.views

import ej.editor.AModView
import ej.editor.ModViewModel
import ej.editor.MonsterViewModel
import ej.mod.*
import javafx.scene.control.TreeItem
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*


class ModView: AModView("Mod view") {
	init {
		headingProperty.bind(modVM.name)
		controller.monsterProperty.onChange {
			if (it != null) {
				workspace.dock(MonsterView(MonsterViewModel(modVM,it)))
			}
		}
	}
	
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
	
	val treeWithEditor by lazy {
		XStatementTreeWithEditor().apply {
			vgrow = Priority.ALWAYS
		}
	}
	
	fun selectModEntry(e:ModTreeNode?) {
		root.center.replaceChildren(when (e) {
			null,
			is ModTreeNode.RootNode -> ModOverviewFragment().root
			is ModTreeNode.MonsterNode -> {
				controller.selectMonster(e.monster)
				VBox()
			}
			is ModTreeNode.MonsterListNode -> VBox().apply { text("TODO")} // TODO
			is ModView.ModTreeNode.StoryNode -> e.story.let { story ->
				when(story) {
					is XcLib -> VBox().apply { text("TODO") } // TODO
					is XContentContainer -> treeWithEditor.also {
						it.contents = story.content
						it.tree.root.expandAll()
					}
					else -> VBox().apply { text("Unknown $story") }
				}
			}
			is ModView.ModTreeNode.StoryListNode -> VBox().apply { text("NYI")} // TODO
		})
	}
	
	override val root = borderpane {
		top {
		}
		left {
			vbox {
				/*
				label("Monsters")
				listview(modVM.monsters) {
					cellFormat { text = it.id }
					onUserSelect {
						controller.selectMonster(it)
					}
				}
				*/
				treeview<ModTreeNode> {
					root = TreeItem(ModTreeNode.RootNode(modVM))
					populate { it.value.population() }
					selectionModel.selectedItemProperty().onChange {
						selectModEntry(it?.value)
					}
					root.expandTo(3)
				}
			}
		}
		center {
			this += find<ModOverviewFragment>().root
		}
	}
}