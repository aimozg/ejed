package ej.editor.views

import ej.editor.EditorController
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.File


class ModListView: View("EJEd") {
	val controller: EditorController by inject(DefaultScope)
	
	var fileList: ListView<File> by singleAssign()
	override val root = vbox {
		hbox {
			alignment = Pos.BASELINE_LEFT
			label("Mod directory")
			textfield {
				textProperty().bindBidirectional(controller.modDirProperty)
				vgrow = Priority.SOMETIMES
			}
			button("...").action {
				controller.openMod()
			}
		}
		listview(controller.modFiles) {
			fileList = this
			multiSelect(false)
			cellFormat { text = it.nameWithoutExtension }
			onUserSelect {
				controller.loadMod(it)
			}
		}
		hbox {
			button("Open") {
				enableWhen(fileList.selectionModel.selectedItemProperty().isNotNull)
			}.action {
				val item = fileList.selectedItem
				if (item != null) this@ModListView.controller.loadMod(item)
			}
		}
		
	}
	
	override fun onDock() {
		controller.loadModList()
	}
}