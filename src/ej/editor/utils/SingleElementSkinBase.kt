package ej.editor.utils

import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.SkinBase

/*
 * Created by aimozg on 24.09.2018.
 * Confidential until published on GitHub
 */
open class SingleElementSkinBase<C : Control>(control: C, val main: Node) : SkinBase<C>(control) {
	init {
		children += main
	}
	
	override fun computeMinWidth(height: Double,
	                             topInset: Double,
	                             rightInset: Double,
	                             bottomInset: Double,
	                             leftInset: Double): Double {
		return main.minWidth(height)
	}
	
	override fun computeMaxWidth(height: Double,
	                             topInset: Double,
	                             rightInset: Double,
	                             bottomInset: Double,
	                             leftInset: Double): Double {
		return main.maxWidth(height)
	}
	
	override fun computeMinHeight(width: Double,
	                              topInset: Double,
	                              rightInset: Double,
	                              bottomInset: Double,
	                              leftInset: Double): Double {
		return main.minHeight(width)
	}
	
	override fun computeMaxHeight(width: Double,
	                              topInset: Double,
	                              rightInset: Double,
	                              bottomInset: Double,
	                              leftInset: Double): Double {
		return main.maxHeight(width)
	}
	
	override fun computePrefHeight(width: Double,
	                               topInset: Double,
	                               rightInset: Double,
	                               bottomInset: Double,
	                               leftInset: Double): Double {
		return main.prefHeight(width)
	}
	
	override fun computePrefWidth(height: Double,
	                              topInset: Double,
	                              rightInset: Double,
	                              bottomInset: Double,
	                              leftInset: Double): Double {
		return main.prefWidth(height)
	}
}