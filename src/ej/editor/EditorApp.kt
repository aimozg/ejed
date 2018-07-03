package ej.editor

import ej.editor.utils.SplittingOutputStream
import ej.editor.utils.TextAreaOutputStream
import ej.editor.utils.onChangeAndNow
import ej.editor.views.ModListView
import ej.editor.views.ModView
import javafx.geometry.Side
import tornadofx.*
import java.io.PrintStream


/*
 * Created by aimozg on 25.06.2018.
 * Confidential until published on GitHub
 */


abstract class AModFragment(title: String? = null) : Fragment(title) {
	val controller: EditorController by inject()
	val modVM get() = controller.modVM
}

abstract class AModView (title: String? = "EJEd"): View(title) {
	
	val controller: EditorController by inject()
	val modVM get() = controller.modVM
}

class EditorView : AModView() {
//	val leftDrawer = Drawer(Side.LEFT,false,false)
	val rightDrawer = Drawer(Side.RIGHT,false,false)
	override val root = borderpane {
		top {
			menubar {
				menu("Mod") {
					item("New") {
						isDisable = true
					}
					item("Open...").action {
						if (center.uiComponent<ModListView>() != null) {
							controller.openMod()
						} else {
							center = find<ModListView>().root
						}
					}
					item("Save") {
						enableWhen(controller.modProperty.isNotNull)
					}.action {
						controller.saveMod()
					}
					item("Save As...") {
						enableWhen(controller.modProperty.isNotNull)
					}.action {
						controller.saveModAs()
					}
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

