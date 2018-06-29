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
		val wrapper by cssclass()
		val consola by cssclass()
		val monsterCombat by cssclass()
		val gridPane by cssclass()
		
		val smallButton by cssclass()
	}
	
	init {
		root {
			prefHeight = 600.px
			prefWidth = 800.px
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
//		println(render())
	}
}
