package ej.editor.utils

import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Menu

/*
 * Created by aimozg on 27.10.2018.
 * Confidential until published on GitHub
 */
interface ContextMenuContainer {
	val menus: List<Menu>
}

fun Node.myContextMenus() =
		(this as? ContextMenuContainer)?.menus ?: ((this as? Control)?.skin as? ContextMenuContainer)?.menus