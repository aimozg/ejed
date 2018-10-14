package ej.editor

import ej.editor.utils.onChangeAndNow
import ej.mod.ModData
import javafx.beans.property.SimpleObjectProperty
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

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
		val mod = ModData.loadMod(src.reader())
		mod.sourceFile = src
		this.mod = mod
	}
	
	fun saveMod() {
		val mod = mod ?: return
		val file = mod.sourceFile?.takeIf { it.exists() && it.isFile && it.canWrite() } ?: return saveModAs()
		Files.move(
				file.toPath(),
				file.toPath().parent.resolve(file.nameWithoutExtension + ".bak"),
				StandardCopyOption.REPLACE_EXISTING
		)
		println("Saving to $file")
		ModData.saveMod(mod, file.writer())
	}
	
	fun saveModAs() {
		chooseFile("Choose mod file",
		           mode = FileChooserMode.Save,
		           filters = MOD_FILE_FILTERS) {
			initialDirectory = (mod?.sourceFile?.parentFile
					?: File(modDir)).takeIf { it.exists() && it.isDirectory } ?: File(".")
		}.firstOrNull()?.let { file ->
			if (file.exists()) file.renameTo(File(file.parent, file.nameWithoutExtension + ".bak"))
			else file.createNewFile()
			mod?.sourceFile = file
			saveMod()
		}
	}
	
	init {
		modDirProperty.onChangeAndNow {
			loadModList()
		}
	}
	
}