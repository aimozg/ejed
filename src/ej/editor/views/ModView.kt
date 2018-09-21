package ej.editor.views

import ej.editor.AModView
import ej.editor.player.ModPreview
import ej.editor.utils.onChangeAndNow
import ej.editor.utils.select
import ej.editor.utils.textInputDialog
import ej.mod.*
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.Callback
import tornadofx.*


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
					is XContentContainer -> SceneEditor(mod).apply {
						rootStatement = story
						vgrow = Priority.ALWAYS
					}
					else -> VBox().apply { text("Unknown $story") }
				}
			}
			is ModTreeNode.StoryListNode -> VBox().apply { text("TODO")} // TODO
		}
	
	override val root = borderpane {
		left = drawer(Side.LEFT) {
			item("Mod contents") {
				paddingAll = 5.0
				spacing = 5.0
				expanded = true
				/*
				label("Add: ")
				hbox(5.0) {
					vgrow = Priority.NEVER
					alignment = Pos.BASELINE_LEFT
					button("Monster").action{
						createMonster()
					}
					button("Scene").action {
						createScene()
					}
					button("Group").action {
						createLibrary()
					}
					button("Named text").action {
						createSubscene()
					}
				}
				*/
				hbox(5.0) {
					vgrow = Priority.NEVER
					alignment = Pos.BASELINE_LEFT
					button("Validate").action {
						mod.validate()
					}
					button("Play") {
						action {
							val story = (tree.selectedValue as? ModTreeNode.StoryNode)?.story
							if (story != null) find<ModPreview>().apply {
								openWindow()
								clearOutput()
								play(story)
							}
						}
						
						disableWhen {
							tree.selectionModel.selectedItemProperty().booleanBinding {
								(it?.value as? ModTreeNode.StoryNode)?.story !is XContentContainer
							}
						}
					}
				}
				tree.attachTo(this) {
					onUserSelect {
						selectModEntry(it)
					}
					cellFactory = Callback { ModTreeCell() }
					vgrow = Priority.ALWAYS
					contextmenu {
						menu("Add") {
							item("Monster") {
								enableWhen(tree.selectionModel.selectedItemProperty().booleanBinding {
									it?.value?.acceptsMonsters == true
								})
								action { createMonster() }
							}
							item("Scene") {
								enableWhen(tree.selectionModel.selectedItemProperty().booleanBinding {
									it?.value?.acceptsScenes == true
								})
								action { createScene() }
							}
							item("Named Text") {
								enableWhen(tree.selectionModel.selectedItemProperty().booleanBinding {
									it?.value?.acceptsScenes == true
								})
								action { createSubscene() }
							}
							item("Group") {
								enableWhen(tree.selectionModel.selectedItemProperty().booleanBinding {
									it?.value?.acceptsScenes == true
								})
								action { createLibrary() }
							}
						}
						item("Rename") {
							enableWhen(tree.selectionModel.selectedItemProperty().booleanBinding {
								it?.value?.hasName == true
							})
							action { renameSelected() }
						}
						item("Delete") {
							enableWhen(tree.selectionModel.selectedItemProperty().booleanBinding {
								it?.value?.removable == true
							})
							action { removeSelected() }
						}
					}
				}
			}
		}
	}
	
	fun removeSelected() {
		val parent = tree.selectionModel.selectedItem.parent?.value
		val value = tree.selectedValue
		when(value) {
			is ModTreeNode.MonsterNode -> mod.monsters.remove(value.monster)
			is ModTreeNode.StoryNode -> when (parent) {
				is ModTreeNode.StoryListNode -> mod.lib.remove(value.story)
				is ModTreeNode.StoryNode -> parent.story.lib.remove(value.story)
				else -> kotlin.error("Cannot remove")
			}
			else -> kotlin.error("Cannot remove")
		}
	}
	fun renameSelected() {
		val value = tree.selectedValue
		val nameProp = when (value) {
			is ModTreeNode.RootNode -> mod.nameProperty
			is ModTreeNode.MonsterNode -> value.monster.idProperty
			is ModTreeNode.StoryNode -> value.story.nameProperty
			else -> kotlin.error("Cannot rename")
		}
		textInputDialog("Rename","Change ID to",nameProp.value) {
			nameProp.value = it
		}
	}
	
	fun addMonster(m: MonsterData) {
		mod.monsters.add(m)
		tree.select { (it as? ModTreeNode.MonsterNode)?.monster == m }
	}
	fun createMonster() {
		textInputDialog("New Monster", "Name of new monster", "Unnamed") { dialogResult ->
			addMonster(MonsterData().apply {
				id = dialogResult
				name = dialogResult
			})
		}
	}
	
	fun addScene(parent: StoryStmt?, s: StoryStmt) {
		(parent?.lib?:mod.lib).add(s)
		tree.select { (it as? ModTreeNode.StoryNode)?.story == s }
	}
	
	fun createScene() {
		val parent: StoryStmt? =
				(tree.selectedValue as? ModTreeNode.StoryNode)?.story
		textInputDialog(
				"New Scene",
				"ID of new scene",
				"Unnamed"
		) { dialogResult ->
			addScene(parent, XcScene().apply { name = dialogResult })
		}
	}
	
	fun createSubscene() {
		val parent: StoryStmt? =
				(tree.selectedValue as? ModTreeNode.StoryNode)?.story
		textInputDialog(
				"New Named Text",
				"ID of new named text",
				"Unnamed"
		) { dialogResult ->
			addScene(parent, XcNamedText().apply { name = dialogResult })
		}
	}
	fun createLibrary() {
		val parent:StoryStmt? =
				(tree.selectedValue as? ModTreeNode.StoryNode)?.story
		textInputDialog(
				"New Group",
				"ID of new Group",
				"Unnamed"
		) { dialogResult ->
			addScene(parent, XcLib().apply { name = dialogResult })
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