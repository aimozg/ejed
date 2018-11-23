package ej.editor.utils

import ej.utils.maxOf
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.css.*
import javafx.geometry.HPos
import javafx.geometry.Orientation
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.Skin
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.text.TextAlignment
import tornadofx.*

/*
 * Created by aimozg on 24.09.2018.
 * Confidential until published on GitHub
 */
open class SimpleListView<T : Any> : Region() {
	val itemsProperty: Property<ObservableList<T>> = object : SimpleObjectProperty<ObservableList<T>>() {
		override fun invalidated() {
			super.invalidated()
			if (scene != null && value != null) bindTo(value)
		}
	}
	var items: ObservableList<T> by itemsProperty
	
	val spacingProperty: StyleableDoubleProperty = object : SimpleStyleableDoubleProperty(StyleableProperties.SPACING,
	                                                                                      this,
	                                                                                      "spacing",
	                                                                                      0.0) {
		override fun invalidated() {
			super.invalidated()
			requestLayout()
		}
	}
	var spacing: Double by spacingProperty
	
	
	private val itemsListener: ListChangeListener<T> = ListChangeListener { change ->
		if (items != change.list) return@ListChangeListener
		while (change.next()) {
			val from = change.from
			val to = change.to
			if (change.wasPermutated()) {
				val copy = cells.subList(from, to)
				for (oldIndex in from until to) {
					val newIndex = change.getPermutation(oldIndex)
					copy[newIndex - from] = cells[oldIndex]
				}
				println("Reordering cells $from .. $to")
				cells.subList(from, to).clear()
				cells.addAll(from, copy)
			}
			if (change.wasUpdated()) {
				// do nothing
			}
			if (change.wasRemoved()) {
				val removed = change.removedSize
				println("Removing cells $from .. ${from + removed}")
				cells.remove(from, from + removed)
			}
			if (change.wasAdded()) {
				val added = change.addedSubList.map(::createCell)
				println("Adding cells $from .. ${from + added.size}")
				cells.addAll(from, added)
			}
		}
		togglePseudoClass("empty", change.list.isEmpty())
		requestLayout()
	}
	
	protected open fun cellFactory(item: T): SimpleListCell<T> {
		return SimpleListCell(this, item)
	}
	
	protected fun createCell(item: T): SimpleListCell<T> = cellFactory(item).also { cell ->
		cell.managedProperty().addListener { _ ->
			requestLayout()
		}
	}
	
	var graphicFactory: (SimpleListCell<T>) -> Node = { cell ->
		Label(cell.item.toString()).apply {
			textAlignment = TextAlignment.LEFT
		}
	}
	
	fun graphicFactory(gf: (T) -> Node) {
		graphicFactory = { gf(it.item) }
	}
	
	fun graphicFactory(gf: (SimpleListCell<T>, T) -> Node) {
		graphicFactory = { gf(it, it.item) }
	}
	
	override fun getContentBias(): Orientation {
		return Orientation.HORIZONTAL
	}
	
	override fun computeMinWidth(height: Double) =
			totalPaddingHoriz + maxOf(
					beforeCells_managed.maxOf(0.0) { it.minWidth(-1.0) },
					cells_managed.maxOf(0.0) { it.minWidth(-1.0) },
					afterCells_managed.maxOf(0.0) { it.minWidth(-1.0) }
			)
	
	protected open fun extraNodesMinHeight(width: Double): Double {
		return maxOf(
				beforeCells_managed.maxOf(0.0) { it.minHeight(width) },
				afterCells_managed.maxOf(0.0) { it.minHeight(width) }
		)
	}
	
	override fun computeMinHeight(width: Double) =
			totalPaddingVert + cells_managed.sumByDouble { it.minHeight(width) } + extraNodesMinHeight(width) + (cells_managed.size - 1) * spacing
	
	override fun computePrefWidth(height: Double) =
			totalPaddingHoriz + maxOf(
					beforeCells_managed.maxOf(0.0) { it.prefWidth(-1.0) },
					cells_managed.maxOf(0.0) { it.prefWidth(-1.0) },
					afterCells_managed.maxOf(0.0) { it.prefWidth(-1.0) }
			)
	
	protected open fun extraNodesPrefHeight(width: Double): Double {
		return maxOf(
				beforeCells_managed.maxOf(0.0) { it.prefHeight(width) },
				afterCells_managed.maxOf(0.0) { it.prefHeight(width) }
		)
	}
	
	override fun isResizable(): Boolean {
		return true
	}
	
	override fun computePrefHeight(width: Double) =
			totalPaddingVert + cells_managed.sumByDouble { it.prefHeight(width) } + extraNodesPrefHeight(width) + (cells_managed.size - 1) * spacing
	
	private var performingLayout = false
	override fun layoutChildren() {
		performingLayout = true
		val x0 = snapSpace(insets.left)
		var y0 = snapSpace(insets.top)
		val contentWidth = width - totalPaddingHoriz
		y0 = layoutExtraBeforeCells(x0, y0, contentWidth)
		y0 = layoutCells(x0, y0, contentWidth)
		layoutExtraAfterCells(x0, y0, contentWidth)
		performingLayout = false
	}
	
	override fun requestLayout() {
		if (performingLayout) return
		super.requestLayout()
	}
	
	protected open fun layoutExtraBeforeCells(x0: Double, y0: Double, contentWidth: Double): Double {
		var y1 = y0
		for (c in beforeCells_managed) {
			val cellheight = c.prefHeight(contentWidth)
			layoutInArea(c, x0, y0, contentWidth, cellheight, cellheight, HPos.LEFT, VPos.TOP)
			y1 = maxOf(y1, y0 + cellheight)
		}
		return y1
	}
	
	protected open fun layoutExtraAfterCells(x0: Double, y0: Double, contentWidth: Double) {
		for (c in afterCells_managed) {
			val cellheight = c.prefHeight(contentWidth)
			layoutInArea(c, x0, y0, contentWidth, cellheight, cellheight, HPos.LEFT, VPos.TOP)
		}
	}
	
	protected fun layoutCells(x0: Double, y0: Double, contentWidth: Double): Double {
		val spacing = spacing
		var y = y0
		for (c in cells_managed) {
			val cellheight = c.prefHeight(contentWidth)
			layoutInArea(c, x0, y, contentWidth, cellheight, cellheight, HPos.LEFT, VPos.TOP)
			y += spacing + cellheight
		}
		return y
	}
	
	protected val beforeCells = ArrayList<Node>().observable()
	protected val beforeCells_managed get() = beforeCells.filter { it.isManaged }
	protected val cells = ArrayList<Node>().observable()
	protected val cells_managed get() = cells.filter { it.isManaged }
	protected val afterCells = ArrayList<Node>().observable()
	protected val afterCells_managed get() = afterCells.filter { it.isManaged }
	
	init {
		vgrow = Priority.NEVER
		addClass("simple-list-view")
		
		itemsProperty.addListener { _, old, _ ->
			if (old != null) unbindFrom(old)
		}
		sceneProperty().onChange {
			unbindFrom(items)
			if (it != null) bindTo(items)
		}
		var beforeCellsSize = beforeCells.size
		var cellsSize = cells.size
		var afterCellsSize = afterCells.size
		beforeCells.onChange {
			it.reapplyTo(children.subList(0, beforeCellsSize))
			beforeCellsSize = beforeCells.size
			requestLayout()
		}
		cells.onChange {
			it.reapplyTo(children.subList(beforeCellsSize, beforeCellsSize + cellsSize))
			cellsSize = cells.size
			requestLayout()
		}
		afterCells.onChange {
			it.reapplyTo(children.subList(beforeCellsSize + cellsSize, beforeCellsSize + cellsSize + afterCellsSize))
			afterCellsSize = afterCells.size
			requestLayout()
		}
	}
	
	private fun unbindFrom(list: ObservableList<T>?) {
		list?.removeListener(itemsListener)
	}
	
	private fun bindTo(list: ObservableList<T>?) {
		list?.addListener(itemsListener)
		cells.clear()
		cells.addAll(list?.map(::createCell) ?: emptyList())
		togglePseudoClass("empty", list?.isEmpty() ?: true)
		requestLayout()
	}
	
	open class SimpleListCell<T : Any>(open val list: SimpleListView<T>, val item: T) : Control() {
		//		val itemProperty = SimpleObjectProperty<T>(item)
//		val item: T by itemProperty
		val index get() = list.items.indexOf(item)
		
		init {
			isFocusTraversable = false
			addClass("simple-list-cell")
		}
		
		override fun getContentBias(): Orientation {
			return Orientation.HORIZONTAL
		}
		
		override fun createDefaultSkin(): Skin<out SimpleListCell<T>> =
				SingleElementSkinBase(
						this,
						list.graphicFactory(this)
						/*NodeBinding(
								itemProperty.objectBinding {
									list.graphicFactory(this)
								}
						)*/
				)
	}
	
	companion object {
		private val FACTORY = StyleablePropertyFactory<SimpleListView<*>>(Region.getClassCssMetaData())
		
		private object StyleableProperties {
			val SPACING = FACTORY.createSizeCssMetaData("-fx-spacing", { it.spacingProperty }, 0.0, false)
		}
		
		val classCssMetaData: List<CssMetaData<out Styleable, *>> get() = FACTORY.cssMetaData
	}
	
	override fun getCssMetaData() = classCssMetaData
}