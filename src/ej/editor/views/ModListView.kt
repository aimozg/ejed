package ej.editor.views

import ej.editor.EditorController
import ej.editor.utils.onChangeAndNow
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*
import java.io.File


class ModListView: View("EJEd") {
	val controller: EditorController by inject(DefaultScope)
	
	fun VBox.modDirView(basedir: String) {
		hgrow = Priority.SOMETIMES
		vgrow = Priority.SOMETIMES
		val modDirProperty = SimpleStringProperty(basedir)
		val modFiles = ArrayList<String>().observable()
		fun openFile(fn: String) {
			val file = File(modDirProperty.value).resolve(fn)
			if (file.exists() && file.isDirectory) {
				modDirProperty.set(file.absolutePath)
			} else if (file.exists() && file.isFile) {
				controller.loadMod(file)
			}
		}
		
		
		modDirProperty.onChangeAndNow { it ->
			File(it).takeIf { it.exists() && it.isDirectory }?.let { dir ->
				modFiles.setAll(
						listOf("..") +
								dir.listFiles().mapNotNull { file ->
									when {
										file.isDirectory -> file.name + "/"
										file.isFile && file.extension == "xml" -> file.name
										else -> null
									}
								}
				)
			}
		}
		hbox {
			alignment = Pos.BASELINE_LEFT
			label("Current directory:")
			textfield {
				textProperty().bindBidirectional(modDirProperty)
				hgrow = Priority.SOMETIMES
			}
			button("...").action {
				controller.openMod()
			}
		}
		val fileList = listview(modFiles) {
			multiSelect(false)
			onUserSelect {
				openFile(it)
			}
			vgrow = Priority.SOMETIMES
		}
		hbox {
			button("Open") {
				enableWhen(fileList.selectionModel.selectedItemProperty().isNotNull)
			}.action {
				val item = fileList.selectedItem
				if (item != null) openFile(item)
			}
		}
	}
	
	override val root = hbox {
		/*hbox {
			alignment = Pos.BASELINE_LEFT
			label("Mod directory")
			textfield {
				textProperty().bindBidirectional(controller.modDirProperty)
				vgrow = Priority.SOMETIMES
			}
			button("...").action {
				controller.openMod()
			}
		}*/
		vbox {
			modDirView("content/mods")
		}
		vbox {
			modDirView("content/coc")
		}
		
		
	}
}