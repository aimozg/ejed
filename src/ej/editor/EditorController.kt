package ej.editor

import ej.editor.utils.onChangeAndNow
import ej.mod.ModData
import javafx.beans.property.SimpleObjectProperty
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File

class EditorController : Controller() {
	companion object {
		val MOD_FILE_FILTERS = arrayOf(
				FileChooser.ExtensionFilter("XML files", "*.xml")
		)
	}
	
	val modFiles = ArrayList<File>().observable()
	
	val modDirProperty = SimpleObjectProperty("content/mods")
	var modDir: String by modDirProperty
	
	val modProperty = SimpleObjectProperty<ModData>(null)
	var mod: ModData? by modProperty
	
	fun loadModList() {
		File(modDir).takeIf{it.exists() && it.isDirectory}?.let { dir ->
			modFiles.setAll(*dir.listFiles { file -> file.isFile && file.extension == "xml" })
		}
	}
	
	fun openMod() {
		chooseFile("Choose mod file",
		           mode = FileChooserMode.Single,
		           filters = MOD_FILE_FILTERS) {
			initialDirectory = File(modDir).takeIf { it.exists() && it.isDirectory } ?: File(".")
		}.firstOrNull()?.let { file ->
			modDir = file.parent
			loadMod(file)
		}
	}
	fun loadMod(src: File) {
		println("Loading from $src")
		val mod = ModData.jaxbContext.createUnmarshaller().apply {
			setEventHandler { false }
		}.unmarshal(src) as ModData
		mod.sourceFile = src
		this.mod = mod
	}
	
	fun saveMod() {
		val file = mod?.sourceFile?.takeIf { it.exists() && it.isFile && it.canWrite() } ?: return saveModAs()
		println("Saving to $file")
		ModData.jaxbContext.createMarshaller().marshal(mod, file)
	}
	
	fun saveModAs() {
		chooseFile("Choose mod file",
		           mode = FileChooserMode.Save,
		           filters = MOD_FILE_FILTERS) {
			initialDirectory = (mod?.sourceFile?.parentFile
					?: File(modDir)).takeIf { it.exists() && it.isDirectory } ?: File(".")
		}.firstOrNull()?.let {
			it.createNewFile()
			mod?.sourceFile = it
			saveMod()
		}
	}
	
	init {
		modDirProperty.onChangeAndNow {
			loadModList()
		}
	}
	
}