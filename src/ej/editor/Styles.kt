package ej.editor

import javafx.scene.paint.Color
import tornadofx.*

/*
 * Created by aimozg on 26.06.2018.
 * Confidential until published on GitHub
 */
class Styles : Stylesheet() {
	companion object {
		val wrapper by cssclass()
		val consola by cssclass()
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
	}
}
