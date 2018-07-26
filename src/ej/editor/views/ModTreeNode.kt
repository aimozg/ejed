package ej.editor.views

import com.sun.javafx.binding.StringConstant
import ej.editor.Styles
import ej.editor.utils.ObservableBase
import ej.editor.utils.fontAwesome
import ej.editor.utils.transformed
import ej.mod.*
import javafx.beans.Observable
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.control.TreeCell
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import tornadofx.*

/*
 * Created by aimozg on 26.07.2018.
 * Confidential until published on GitHub
 */
sealed class ModTreeNode(
		val acceptsMonsters: Boolean = false,
		val acceptsScenes: Boolean = false,
		val hasName: Boolean = false,
		val removable: Boolean = false
): ObservableBase() {
	open val population: ObservableList<out ModTreeNode> = emptyList<ModTreeNode>().observable()
	abstract val textProperty: ObservableValue<String>
	
	class MonsterListNode(val mod: ModData): ModTreeNode(
			acceptsMonsters = true
	) {
		override val population = mod.monsters.transformed { MonsterNode(mod,it) }
		override val textProperty = StringConstant.valueOf("Monsters")
	}
	class MonsterNode(val mod: ModData, val monster: MonsterData): ModTreeNode(
			acceptsMonsters = true,
			hasName = true,
			removable = true
	) {
		override val textProperty = monster.idProperty.stringBinding(monster.nameProperty) {
			if (monster.id == monster.name || monster.name.isNullOrBlank()) monster.id
			else "${monster.id} (${monster.name})"
		}
	}
	class StoryNode(val story: StoryStmt): ModTreeNode(
			acceptsScenes = true,
			hasName = true,
			removable = true
	) {
		override val textProperty = story.nameProperty
		override val population = story.lib.transformed { StoryNode(it) }
		init {
			if (story is Observable) story.invalidatesThis()
		}
	}
	class StoryListNode(val stories: ObservableList<StoryStmt>): ModTreeNode(
			acceptsScenes = true
	) {
		override val textProperty = StringConstant.valueOf("Scenes")
		override val population = stories.transformed { StoryNode(it) }
	}
	class RootNode(val mod: ModData): ModTreeNode(
			acceptsScenes = true,
			acceptsMonsters = true,
			hasName = true
	) {
		override val population = listOf(MonsterListNode(mod),
		                                 StoryListNode(mod.content)).observable()
		override val textProperty = mod.nameProperty
	}
}

class ModTreeCell : TreeCell<ModTreeNode>() {
	val classBindingProperty = SimpleStringProperty("")
	
	override fun updateItem(item: ModTreeNode?, empty: Boolean) {
		super.updateItem(item, empty)
		if (treeItem == null || item == null) {
			graphic = null
			classBindingProperty.unbind()
			classBindingProperty.value = null
		} else {
			val g = (graphic as? Glyph) ?: fontAwesome.create("").addClass(Styles.treeGraphic).also { graphic = it }
			var textBinding: ObservableValue<String>? = null
			var classBinding: ObservableValue<String>? = null
			if (item is ModTreeNode.StoryNode) {
				when (item.story) {
					is XcScene -> {
						textBinding = item.story.triggerProperty.stringBinding { trigger ->
							when (trigger) {
								is EncounterTrigger -> FontAwesome.Glyph.MAP_MARKER.char.toString()
								is TimedTrigger -> FontAwesome.Glyph.CLOCK_ALT.char.toString()
								null -> FontAwesome.Glyph.SHARE_ALT.char.toString()
							}
						}
					}
					is XcNamedText -> {
						textBinding = StringConstant.valueOf(FontAwesome.Glyph.FILE_TEXT_ALT.char.toString())
					}
					is XcLib -> {
						textBinding = StringConstant.valueOf(FontAwesome.Glyph.FOLDER_ALT.char.toString())
					}
				}
				
				classBinding = item.story.isValidProperty.stringBinding { "validation-${it?.name?.toLowerCase()}" }
			}
			g.textProperty().bind(textBinding ?: StringConstant.valueOf(""))
			classBindingProperty.bind(classBinding ?: StringConstant.valueOf(null))
		}
	}
	
	init {
		
		classBindingProperty.addListener { _, oldValue, newValue ->
			if (!oldValue.isNullOrBlank() && oldValue != newValue) removeClass(oldValue)
			if (!newValue.isNullOrBlank() && newValue != oldValue) addClass(newValue)
		}
		textProperty().bind(treeItemProperty().select { it.value.textProperty })
	}
}