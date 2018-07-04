package ej.editor.views

import com.sun.javafx.binding.StringConstant
import ej.editor.AModView
import ej.editor.utils.findItem
import ej.editor.utils.onChangeAndNow
import ej.editor.utils.transformed
import ej.mod.*
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

sealed class ModTreeNode {
	open val population:ObservableList<out ModTreeNode> = emptyList<ModTreeNode>().observable()
	abstract val textProperty: ObservableValue<String>
	class MonsterListNode(val mod:ModData): ModTreeNode() {
		override val population = mod.monsters.transformed { MonsterNode(mod,it) }
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
		override val population = story.lib.transformed { StoryNode(it) }
	}
	class EncounterNode(val encounter: Encounter): ModTreeNode() {
		override val textProperty = encounter.nameProperty.stringBinding(encounter.poolProperty) {
			"$it (in ${encounter.pool})"
		}
		override val population = encounter.scene.lib.transformed { StoryNode(it) }
	}
	class StoryListNode(val stories:ObservableList<StoryStmt>): ModTreeNode() {
		override val textProperty = StringConstant.valueOf("Scenes")
		override val population = stories.transformed { StoryNode(it) }
	}
	class EncounterListNode(val encounters: ObservableList<Encounter>): ModTreeNode() {
		override val textProperty = StringConstant.valueOf("Encounters")
		override val population = encounters.transformed { EncounterNode(it) }
	}
	class RootNode(val mod:ModData): ModTreeNode() {
		override val population = listOf(MonsterListNode(mod),
		                                 EncounterListNode(mod.encounters),
		                                 StoryListNode(mod.content)).observable()
		override val textProperty = mod.nameProperty
	}
}

class ModView: AModView() {
	
	val treeWithEditor by lazy {
		XStatementTreeWithEditor().apply {
			vgrow = Priority.ALWAYS
		}
	}
	val tree = TreeView<ModTreeNode>()
	
	fun selectModEntry(e:ModTreeNode?) {
		root.center = modEntryPane(e)
	}
	fun modEntryPane(e:ModTreeNode?) = when (e) {
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
		left = drawer(Side.LEFT) {
			item("Mod contents") {
				paddingAll = 5.0
				spacing = 5.0
				tree.attachTo(this) {
					onUserSelect {
						selectModEntry(it)
					}
					cellFormat {
						textProperty().bind(it.textProperty)
					}
					vgrow = Priority.ALWAYS
				}
				expanded = true
				label("Add: ")
				hbox(5.0) {
					vgrow = Priority.NEVER
					alignment = Pos.BASELINE_LEFT
					button("Monster"){isDisable=true}
					button("Encounter"){
						disableWhen(controller.modProperty.isNull)
					}.action {
						val e = Encounter().apply {
							pool = "forest"
							name = "Unnamed"
						}
						mod.encounters.add(e)
						tree.selectionModel.select(tree.findItem{(it as? ModTreeNode.EncounterNode)?.encounter == e})
					}
					button("Scene"){isDisable=true}
					button("Lib"){isDisable=true}
					button("Text"){isDisable=true}
				}
			}
		}
	}
	
	private fun TreeView<ModTreeNode>.repopulate() {
		root = TreeItem(ModTreeNode.RootNode(mod))
		populate { it.value.population }
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