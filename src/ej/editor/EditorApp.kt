package ej.editor

import ej.editor.utils.SplittingOutputStream
import ej.editor.utils.TextAreaOutputStream
import ej.editor.utils.onChangeAndNow
import ej.editor.utils.textInputDialog
import ej.editor.views.ModListView
import ej.editor.views.ModView
import ej.mod.ModData
import ej.utils.replaceSome
import javafx.geometry.Side
import javafx.scene.input.KeyCombination
import tornadofx.*
import java.io.PrintStream


/*
 * Created by aimozg on 25.06.2018.
 * Confidential until published on GitHub
 */

const val VERSION = "v0.1.5"

abstract class AModView(title: String? = "EJEd $VERSION") : View(title) {
	val controller: EditorController by inject(DefaultScope)
	val mod get() = controller.mod!!
}

class EditorView : AModView() {
//	val leftDrawer = Drawer(Side.LEFT,false,false)
	val rightDrawer = Drawer(Side.RIGHT,false,false)
	override val root = borderpane {
		addClass(Styles.editorView)
		top {
			menubar {
				menu("Mod") {
					item("New").action {
						val name = textInputDialog("New mod","Enter new mod name","unnamed") ?: return@action
						controller.mod = ModData().apply {
							this.name = name
						}
					}
					item("Open...").action {
						controller.openMod()
					}
					item("Save", KeyCombination.valueOf("Ctrl+S")) {
						enableWhen(controller.modProperty.isNotNull)
					}.action {
						controller.saveMod()
					}
					item("Save As...", KeyCombination.valueOf("Ctrl+Shift+S")) {
						enableWhen(controller.modProperty.isNotNull)
					}.action {
						controller.saveModAs()
					}
				}
				menu("View") {
					item("Dark theme").action {
						FX.stylesheets.replaceSome {
							if (it == Styles.THEME_LIGHT) Styles.THEME_DARK
							else null
						}
					}
					item("Light theme").action {
						FX.stylesheets.replaceSome {
							if (it == Styles.THEME_DARK) Styles.THEME_LIGHT
							else null
						}
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
		currentStage?.scene?.focusOwnerProperty()?.onChange {
			println("focusOwner = $it")
		}
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
		FX.stylesheets += Styles.THEME_COMMON
		FX.stylesheets += Styles.THEME_DARK
	}
	
	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			launch<EditorApp>(args)
		}
	}
}

