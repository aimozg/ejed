package ej.editor

import ej.as3.ast.AS3Class
import ej.as3.ast.AS3FunctionDeclaration
import ej.as3.ast.AS3Interface
import ej.as3.ast.AS3Var
import ej.as3.parser.ActionScriptParser
import ej.editor.utils.FlashToMod
import ej.mod.ModData
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
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
	
	
	fun openMod() {
		chooseFile("Choose mod file",
		           mode = FileChooserMode.Single,
		           filters = MOD_FILE_FILTERS) {
			initialDirectory = mod?.sourceFile?.parentFile?.takeIf { it.exists() && it.isDirectory }
					?: File(".")
		}.firstOrNull()?.let { file ->
			loadMod(file)
		}
	}
	fun loadMod(src: File) {
		println("Loading from $src")
		val mod = src.reader().use { reader ->
			ModData.loadMod(reader)
		}
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
		file.writer().use { writer ->
			ModData.saveMod(mod, writer, true)
		}
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
	
	fun doImportFromFunction(fn: AS3FunctionDeclaration): Boolean {
		val scene = FlashToMod.functionToScene(fn)
		mod!!.lib += scene
		println("Converted ${scene.name}")
		return true
	}
	
	fun doImportFromFlash(source: String): Boolean {
		val parser = ActionScriptParser()
		when (parser.parseWord(source)) {
			"package" -> {
				val file = parser.parseFile(source)
				val topLevelDeclarations = file.packageDecl?.declarations
				if (topLevelDeclarations == null) {
					alert(Alert.AlertType.ERROR, "No declaarations")
					return false
				}
				var imported = 0
				for (tld in topLevelDeclarations) when (tld) {
					is AS3Class -> {
						for (stmt in tld.body.items) when (stmt) {
							is AS3FunctionDeclaration -> {
								if (stmt.name != tld.name) {
									if (doImportFromFunction(stmt)) imported++
								}
							}
							else -> println("Skipping $stmt")
						}
					}
					is AS3Interface -> println("Skipping interface ${tld.name}")
					is AS3Var -> println("Skipping var ${tld.name}")
					is AS3FunctionDeclaration -> println("Skipping function ${tld.name}")
				}
				if (imported == 0) {
					alert(Alert.AlertType.WARNING, "Nothing imported!")
					return false
				}
				return true
			}
			"public", "private", "protected", "internal", "function", "final", "override" -> {
				val fn = parser.parseFunction(source)
				if (!doImportFromFunction(fn)) {
					alert(Alert.AlertType.WARNING, "Nothing imported!")
					return false
				}
				return true
			}
			else -> {
				alert(Alert.AlertType.ERROR, "Unknown file structure")
				return false
			}
		}
	}
	
	fun importFromFlash(uiComponent: UIComponent) {
		uiComponent.dialog("Import from Flash") {
			val source = SimpleStringProperty("")
			field("ActionScript Source") {
				textarea(source)
			}
			buttonbar {
				button("Import") {
					isDefaultButton = true
					action {
						if (doImportFromFlash(source.value)) {
							close()
						}
					}
				}
				button("Cancel") {
					isCancelButton = true
					action {
						close()
					}
				}
			}
		}
	}
	
}