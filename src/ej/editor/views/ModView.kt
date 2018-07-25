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
import org.controlsfx.glyphfont.FontAwesome
import tornadofx.*

sealed class ModTreeNode(
		val acceptsMonsters: Boolean = false,
		val acceptsScenes: Boolean = false,
		val hasName: Boolean = false,
		val removable: Boolean = false
) {
	open val population:ObservableList<out ModTreeNode> = emptyList<ModTreeNode>().observable()
	abstract val textProperty: ObservableValue<String>
	
	class MonsterListNode(val mod:ModData): ModTreeNode(
			acceptsMonsters = true
	) {
		override val population = mod.monsters.transformed { MonsterNode(mod,it) }
		override val textProperty = StringConstant.valueOf("Monsters")
	}
	class MonsterNode(val mod:ModData, val monster:MonsterData): ModTreeNode(
			acceptsMonsters = true,
			hasName = true,
			removable = true
	) {
		override val textProperty = monster.idProperty.stringBinding(monster.nameProperty) {
			if (monster.id == monster.name || monster.name.isNullOrBlank()) monster.id
			else "${monster.id} (${monster.name})"
		}
	}
	class StoryNode(val story:StoryStmt): ModTreeNode(
			acceptsScenes = true,
			hasName = true,
			removable = true
	) {
		override val textProperty = story.nameProperty()
		override val population = story.lib.transformed { StoryNode(it) }
	}
	class StoryListNode(val stories:ObservableList<StoryStmt>): ModTreeNode(
			acceptsScenes = true
	) {
		override val textProperty = StringConstant.valueOf("Scenes")
		override val population = stories.transformed { StoryNode(it) }
	}
	class RootNode(val mod:ModData): ModTreeNode(
			acceptsScenes = true,
			acceptsMonsters = true,
			hasName = true
	) {
		override val population = listOf(MonsterListNode(mod),
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
			is ModTreeNode.StoryListNode -> VBox().apply { text("TODO")} // TODO
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
				tree.attachTo(this) {
					onUserSelect {
						selectModEntry(it)
					}
					cellFormat {
						when (it) {
							is ModTreeNode.StoryNode -> {
								//disclosureNode = null
								when (it.story) {
									is XcScene ->
										graphic = boundFaGlyph(it.story.triggerProperty.stringBinding { trigger ->
											when (trigger) {
												is EncounterTrigger -> FontAwesome.Glyph.MAP_MARKER.char.toString()
												is TimedTrigger -> FontAwesome.Glyph.CLOCK_ALT.char.toString()
												null -> FontAwesome.Glyph.SHARE_ALT.char.toString()
											}
										})
									is XcNamedText ->
										graphic = FontAwesome.Glyph.FILE_TEXT_ALT.node()
									is XcLib -> {
										/*
										graphic = boundFaGlyph(it.story.lib.listBinding { lib ->
											if (lib.isEmpty()) FontAwesome.Glyph.FOLDER_ALT.char.toString()
											else ""
										})
										*/
									}
								}
								disclosureNode?.addClass("folder")?.replaceChildren {
									stackpane { addClass("glyph") }
								}
							}
							is ModTreeNode.MonsterNode,
							is ModTreeNode.StoryListNode,
							is ModTreeNode.MonsterListNode,
							is ModTreeNode.RootNode -> {} // do nothing
						}
						textProperty().bind(it.textProperty)
					}
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
				is ModTreeNode.StoryListNode -> mod.content.remove(value.story)
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
			is ModTreeNode.StoryNode -> value.story.nameProperty()
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
		(parent?.lib?:mod.content).add(s)
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