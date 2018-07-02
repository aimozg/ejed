package ej.editor.views

import ej.editor.AModView
import javafx.scene.control.ListView
import tornadofx.*
import java.io.File

class ModListView: AModView() {
	
	var fileList: ListView<File> by singleAssign()
	override val root = vbox {
		label("Open mod")
		listview(this@ModListView.controller.modFiles) {
			fileList = this
			multiSelect(false)
			cellFormat { text = it.nameWithoutExtension }
			onUserSelect {
				this@ModListView.controller.loadMod(it)
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
}