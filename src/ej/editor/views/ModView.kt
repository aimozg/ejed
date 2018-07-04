package ej.editor.views

import com.sun.javafx.binding.StringConstant
import ej.editor.AModView
import ej.editor.utils.onChangeAndNow
import ej.mod.*
import javafx.beans.value.ObservableValue
import javafx.geometry.Side
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

sealed class ModTreeNode {
	open fun population():List<ModTreeNode> = emptyList()
	abstract val textProperty: ObservableValue<String>
	class MonsterListNode(val mod:ModData): ModTreeNode() {
		override fun population() = mod.monsters.map { MonsterNode(mod,it) }
		override val textProperty = StringConstant.valueOf("Monsters")
	}
	class MonsterNode(val mod:ModData, val monster:MonsterData): ModTreeNode() {
		override val textProperty = monster.idProperty.stringBinding{it?:""}
	}
	class StoryNode(val story:StoryStmt): ModTreeNode() {
		override val textProperty = story.nameProperty().stringBinding {
			when(story) {
				is XcScene -> "(Scene) $it"
				is XcNamedText -> "(Text) $it"
				is XcLib -> "$it/"
				else -> it
			}
		}
		override fun population() = story.lib.map { StoryNode(it) }
	}
	class EncounterNode(val encounter: Encounter): ModTreeNode() {
		override val textProperty = encounter.nameProperty.stringBinding(encounter.poolProperty) {
			"$it (in ${encounter.pool})"
		}
		override fun population() = encounter.scene.lib.map { StoryNode(it) }
	}
	class StoryListNode(val stories:List<StoryStmt>): ModTreeNode() {
		override val textProperty = StringConstant.valueOf("Scenes")
		override fun population() = stories.map { StoryNode(it) }
	}
	class EncounterListNode(val encounters:List<Encounter>): ModTreeNode() {
		override val textProperty = StringConstant.valueOf("Encounters")
		override fun population() = encounters.map { EncounterNode(it) }
	}
	class RootNode(val mod:ModData): ModTreeNode() {
		override fun population() = listOf(MonsterListNode(mod),
		                                   EncounterListNode(mod.encounters),
		                                   StoryListNode(mod.content))
		override val textProperty = mod.nameProperty
	}
}

class ModView: AModView() {
	
	val treeWithEditor by lazy {
		XStatementTreeWithEditor().apply {
			vgrow = Priority.ALWAYS
		}
	}
	val tree = TreeView<ModTreeNode>().apply {
		cellFormat {
			textProperty().bind(it.textProperty)
		}
	}
	
	fun selectModEntry(e:ModTreeNode?) = when (e) {
			null -> null
			is ModTreeNode.RootNode -> ModOverviewPage().root
			is ModTreeNode.MonsterNode -> find<MonsterPage>(MonsterScope(e.mod, e.monster)).root
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
			is ModTreeNode.EncounterNode -> find<EncounterPage>(EncounterScope(e.encounter)).root
			is ModTreeNode.EncounterListNode -> VBox().apply { text("NYI")} // TODO
		}
	
	override val root = borderpane {
		val main = this
		left = drawer(Side.LEFT) {
			item("Mod contents") {
				tree.attachTo(this) {
					onUserSelect {
						main.center = selectModEntry(it)
					}
				}
				expanded = true
			}
		}
	}
	
	private fun TreeView<ModTreeNode>.repopulate() {
		root = TreeItem(ModTreeNode.RootNode(mod?:return))
		populate { it.value.population() }
		root.expandTo(4)
		selectionModel.select(root)
	}
	
	init {
		controller.modProperty.onChangeAndNow {
			if (it != null) {
				tree.repopulate()
			}
		}
	}
}