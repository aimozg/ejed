package ej.editor.views

import com.sun.javafx.binding.StringConstant
import ej.editor.AModView
import ej.editor.utils.*
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
		override val textProperty = monster.idProperty.stringBinding(monster.nameProperty) {
			if (monster.id == monster.name || monster.name.isNullOrBlank()) monster.id
			else "${monster.id} (${monster.name})"
		}
	}
	class StoryNode(val story:StoryStmt): ModTreeNode() {
		override val textProperty = bindingN(story.nameProperty()) {
			when(story) {
				is XcScene -> "(Scene) $it"
				is XcNamedText -> "(Subscene) $it"
				is XcLib -> "$it/"
				else -> it?:""
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
					is XContentContainer -> StatementTreeWithEditor(mod).apply {
						rootStatement = story
						tree.root.expandAll()
						vgrow = Priority.ALWAYS
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
				expanded = true
				label("Add: ")
				hbox(5.0) {
					vgrow = Priority.NEVER
					alignment = Pos.BASELINE_LEFT
					button("Monster").action{
						textInputDialog("New Monster","Name of new monster","Unnamed") { dialogResult ->
							addMonster(MonsterData().apply {
								id = dialogResult
								name = dialogResult
							})
						}
					}
					button("Encounter").action {
						textInputDialog(
								"New Encounter",
								"ID of new encounter",
								"Unnamed"
						) { dialogResult ->
							addEncounter(Encounter().apply {name = dialogResult})
						}
					}
					button("Scene").action {
						val parent:StoryStmt? = (tree.selectedValue as? ModTreeNode.StoryNode)?.story
						textInputDialog(
								"New Scene",
								"ID of new scene",
								"Unnamed"
						) { dialogResult ->
							addScene(parent, XcScene().apply {name = dialogResult})
						}
					}
					button("Lib").action {
						val parent:StoryStmt? = (tree.selectedValue as? ModTreeNode.StoryNode)?.story
						textInputDialog(
								"New Scene Lib",
								"ID of new scene lib",
								"Unnamed"
						) { dialogResult ->
							addScene(parent, XcLib().apply { name = dialogResult })
						}
					}
					button("Subscene").action {
						val parent:StoryStmt? = (tree.selectedValue as? ModTreeNode.StoryNode)?.story
						textInputDialog(
								"New Subscene",
								"ID of new subscene",
								"Unnamed"
						) { dialogResult ->
							addScene(parent, XcNamedText().apply {name = dialogResult })
						}
					}
				}
				tree.attachTo(this) {
					onUserSelect {
						selectModEntry(it)
					}
					cellFormat {
						textProperty().bind(it.textProperty)
					}
					vgrow = Priority.ALWAYS
				}
			}
		}
	}
	
	fun addEncounter(e: Encounter) {
		mod.encounters.add(e)
		tree.select { (it as? ModTreeNode.EncounterNode)?.encounter == e }
	}
	
	fun addMonster(m: MonsterData) {
		mod.monsters.add(m)
		tree.select { (it as? ModTreeNode.MonsterNode)?.monster == m }
	}
	
	fun addScene(parent: StoryStmt?, s: StoryStmt) {
		(parent?.lib?:mod.content).add(s)
		tree.select { (it as? ModTreeNode.StoryNode)?.story == s }
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