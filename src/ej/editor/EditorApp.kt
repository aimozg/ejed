package ej.editor

import ej.editor.utils.SplittingOutputStream
import ej.editor.utils.TextAreaOutputStream
import ej.editor.views.ModListView
import ej.mod.ModData
import ej.mod.MonsterData
import ej.utils.affixNonEmpty
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*
import java.io.File
import java.io.PrintStream


/*
 * Created by aimozg on 25.06.2018.
 * Confidential until published on GitHub
 */

val modDir = "content/mods"

class EditorController : Controller() {
	val modFiles = ArrayList<File>().observable()
	
	val modProperty = SimpleObjectProperty<ModData>(ModData())
	var mod by modProperty
	val modVM = ModViewModel(modProperty)
	
	val monsterProperty = SimpleObjectProperty<MonsterData?>(null)
	var monster by monsterProperty
	
	fun loadModList() {
		modFiles.setAll(*File(modDir).listFiles { file -> file.isFile && file.extension == "xml" })
	}
	
	fun loadMod(src: File) {
		mod = ModData.jaxbContext.createUnmarshaller().apply {
			setEventHandler { false }
		}.unmarshal(src) as ModData
		ModData.jaxbContext.createMarshaller().marshal(mod, System.out)
		println()
	}
	
	fun selectMonster(monster: MonsterData?) {
//		println(monster)
		this.monster = monster
	}
}

abstract class AModFragment(title: String? = null) : Fragment(title) {
	val controller: EditorController by inject()
	val modVM get() = controller.modVM
}

abstract class AModView(title: String) : View(title) {
	
	override fun onDock() {
		super.onDock()
		this.title = "EJEd" + modVM.name.value.affixNonEmpty(" - ", "")
		modVM.name.onChange {
			this.title = "EJEd" + it.affixNonEmpty(" - ", "")
		}
	}
	
	val controller: EditorController by inject()
	val modVM get() = controller.modVM
}

abstract class AModWorkspace(title: String) : Workspace(title) {
	val controller: EditorController by inject()
	val modVM get() = controller.modVM
}

class EditorWorkspace : AModWorkspace("EJEd") {
	
	init {
		menubar {
			menu("Mod") {
				item("New") {
					isDisable = true
				}
				item("Open").action {
					if (workspace.dockedComponent !is ModListView) {
						workspace.dock<ModListView>()
					}
				}
				item("Save") {
					isDisable = true
				}
				// separator()
				// TODO recent mods
			}
		}
		
		with(bottomDrawer) {
			item("Logs") {
				textarea {
					addClass("consola")
					isWrapText = true
					val taos = TextAreaOutputStream(this)
					System.setErr(PrintStream(SplittingOutputStream(System.err, taos)))
					System.setOut(PrintStream(SplittingOutputStream(System.out, taos)))
				}
				
			}
		}
	}
}

class EditorApp : App(EditorWorkspace::class) {
	val controller: EditorController by inject()
	
	override fun onBeforeShow(view: UIComponent) {
		controller.loadModList()
		workspace.dock<ModListView>()
	}
	
	init {
		importStylesheet(Styles::class)
	}
	
	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			launch<EditorApp>(args)
		}
	}
}

