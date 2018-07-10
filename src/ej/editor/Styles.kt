package ej.editor

import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

/*
 * Created by aimozg on 26.06.2018.
 * Confidential until published on GitHub
 */
class Styles : Stylesheet() {
	companion object {
		val FONT_FACE_TEXT = "Lucida Fax"
		val FONT_FACE_SCRIPT = "Lucida Console"

		val wrapper by cssclass()
		val consola by cssclass()
		val monsterCombat by cssclass()
		
		val treeCell by cssclass()
		val treeGraphic by cssclass()

		val editorView by cssclass()
		
		val xstmt by cssclass()
		val xstmtEditor by cssclass()
		val xstmtSelected by cssclass()
		val xtext by cssclass()
		val xlogic by cssclass()
		val xcommand by cssclass()
		val xcomment by cssclass()
		val smallButton by cssclass()
		val smallButtonSpace by cssclass()
		
		val dragover by cssclass()
		val dragoverFromTop by cssclass()
		val dragoverFromBottom by cssclass()
		
		val toolbarGrid by cssclass()
		
		const val MAX_DEPTH = 10
	}
	
	init {
		root and editorView {
			prefWidth = 1200.px
			prefHeight = 800.px
		}
		textArea and consola {
			baseColor= Color.BLACK
			fontFamily = "Consolas"
			textFill = Color.LIGHTGRAY
		}
		
		form {
			spacing = 10.px
			"GridPane" {
				hgap = 2.px
				vgap = 5.px
			}
			label and legend {
				fontWeight = FontWeight.BOLD
				fontSize = 1.2.em
				padding = box(0.px, 0.px, 5.px, 0.px)
			}
			and(monsterCombat) {
				label {
					alignment = Pos.CENTER
				}
			}
		}
		smallButton {
			backgroundColor += Color.TRANSPARENT
			backgroundInsets += box(0.px)
			backgroundRadius += box(0.px)
			padding = box(0.px)
			minWidth = 16.px
			maxWidth = 16.px
			minHeight = 16.px
			maxHeight = 16.px
			alignment = Pos.CENTER
			fontSize = 0.8.em
			focusTraversable = false
			and(hover) {
				backgroundColor += Color.LIGHTGRAY
			}
			and(focused) {
				backgroundColor += Color.LIGHTGRAY
			}
			and(armed) {
				backgroundColor += Color.DARKGRAY
			}
		}
		smallButtonSpace {
			minWidth = 16.px
			minHeight = 16.px
		}
		xstmt {
			padding = box(4.px, 4.px, 4.px, 8.px)
			borderColor += box(Color.TRANSPARENT)
			borderWidth += box(1.px)
			and(xstmtSelected) {
				and(".depth-0") {
					backgroundColor += c("#f4f4f4")
					borderColor += box(c("#fff"))
				}
				and(".depth-1") {
					backgroundColor += c("#ddf")
					borderColor += box(c("#228"))
				}
				and(".depth-2") {
					backgroundColor += c("#fdf")
					borderColor += box(c("#828"))
				}
				and(".depth-3") {
					backgroundColor += c("#fdd")
					borderColor += box(c("#822"))
				}
				and(".depth-4") {
					backgroundColor += c("#fed")
					borderColor += box(c("#862"))
				}
				and(".depth-5") {
					backgroundColor += c("#ffd")
					borderColor += box(c("#882"))
				}
				and(".depth-6") {
					backgroundColor += c("#dfd")
					borderColor += box(c("#282"))
				}
				and(".depth-7") {
					backgroundColor += c("#dff")
					borderColor += box(c("#288"))
				}
				and(".depth-8") {
					backgroundColor += c("#ddd")
					borderColor += box(c("#222"))
				}
				and(".depth-9") {
					backgroundColor += c("#ccc")
					borderColor += box(c("#888"))
				}
			}
			xstmtEditor {
				alignment = Pos.BASELINE_LEFT // doesn't work =(
				spacing = 2.px
				padding = box(5.px)
			}
		}
		xtext {
			fontFamily = FONT_FACE_TEXT
			text {
				fontFamily = FONT_FACE_TEXT
			}
			and("TextFlow") {
				padding = box(0.px, 0.px, 15.px, 0.px)
			}
		}
		xlogic {
			text {
				fontFamily = FONT_FACE_SCRIPT
				fill = Color.NAVY
			}
		}
		xcommand {
			text {
				fontFamily = FONT_FACE_SCRIPT
				fill = Color.DARKRED
			}
		}
		xcomment {
			text {
				fontFamily = FONT_FACE_SCRIPT
				fill = c("#252")
			}
		}
		treeCell {
			and(dragover) {
				treeGraphic { borderColor += box(Color.GREEN) }
				and(dragoverFromTop) {
					treeGraphic { borderWidth += box(2.px, 0.px, 0.px, 0.px) }
				}
				and(dragoverFromBottom) {
					treeGraphic { borderWidth += box(0.px, 0.px, 2.px, 0.px) }
				}
			}
		}
		toolbarGrid contains button {
			prefWidth = 80.px
			padding = box(4.px)
		}
	}
}
