package ej.editor

import ej.editor.utils.SplittingOutputStream
import ej.editor.utils.TextAreaOutputStream
import ej.editor.utils.onChangeAndNow
import ej.editor.views.ModListView
import ej.editor.views.ModView
import ej.mod.ModData
import ej.mod.MonsterData
import ej.utils.affixNonEmpty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Side
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
	
	val modProperty = SimpleObjectProperty<ModData>(null)
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
//		ModData.jaxbContext.createMarshaller().marshal(mod, System.out)
//		println()
	}
	
}

abstract class AModFragment(title: String? = null) : Fragment(title) {
	val controller: EditorController by inject()
	val modVM get() = controller.modVM
}

abstract class AModView : View("EJEd") {
	
	override fun onDock() {
		super.onDock()
		this.title = "EJEd" + modVM.name.value.affixNonEmpty(" - ")
		modVM.name.onChangeAndNow {
			this.title = "EJEd" + it.affixNonEmpty(" - ")
		}
	}
	
	val controller: EditorController by inject()
	val modVM get() = controller.modVM
}

class EditorView : AModView() {
//	val leftDrawer = Drawer(Side.LEFT,false,false)
	val rightDrawer = Drawer(Side.RIGHT,false,false)
	override val root = borderpane {
		menubar {
			menu("Mod") {
				item("New") {
					isDisable = true
				}
				item("Open").action {
					center = find<ModListView>().root
				}
				item("Save") {
					isDisable = true
				}
			}
		}
		right = rightDrawer
		bottom = drawer {
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
	
	override fun onDock() {
		controller.loadModList()
		this.controller.modProperty.onChangeAndNow {
			if (it != null) {
				root.center = find<ModView>().root
			} else {
				root.center = find<ModListView>().root
				
			}
		}
	}
}

class EditorApp : App(EditorView::class) {
	
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

