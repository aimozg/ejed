package ej.editor.views

import ej.editor.AModView
import ej.editor.external.TagAlias
import ej.editor.external.TagDecl
import ej.editor.external.TagLib
import ej.editor.utils.onChangeAndNow
import ej.utils.iAmEitherLeft
import ej.utils.iAmEitherRight
import javafx.geometry.Orientation
import javafx.scene.control.ListView
import javafx.scene.layout.VBox
import javafx.scene.text.FontPosture
import org.funktionale.either.Either
import tornadofx.*

/*
 * Created by aimozg on 04.11.2018.
 * Confidential until published on GitHub
 */

class TagHelper : AModView("Tags") {
	
	val tags = ArrayList<Either<TagDecl, TagAlias>>().observable()
	lateinit var taglist: ListView<Either<TagDecl, TagAlias>> private set
	lateinit var tagdesc: VBox private set
	
	override val root = splitpane(Orientation.VERTICAL) {
		taglist = listview(tags) {
			cellFormat { _ ->
				itemProperty().onChangeAndNow { lr ->
					val name = lr?.fold({ it.name }, { it.name })
					val td = lr?.fold({ it }, { TagLib.tags[it.tag] })
					if (td != null) {
						text = if (td.sample.startsWith("(")) {
							"[$name] ${td.sample}"
						} else {
							"[$name] (${td.sample})"
						}
						style {
							fontStyle = if (lr.isRight()) FontPosture.ITALIC else FontPosture.REGULAR
						}
					}
				}
			}
		}
		tagdesc = vbox {
			addClass("tag-help")
		}
		taglist.selectionModel.selectedItems.onChange { change ->
			tagdesc.clear()
			while (change.next()) tagdesc.apply {
				for (lr in change.addedSubList) {
					val name = lr?.fold({ it.name }, { it.name })
					val td = lr?.fold({ it }, { TagLib.tags[it.tag] })
					if (td != null) {
						val alias = lr.component2()
						textflow {
							text("[$name]").addClass("bold")
							if (alias != null) {
								text(" (alias for [${alias.tag}])").addClass("italic")
							}
						}
						textflow {
							text("Sample: ").addClass("bold")
							text(td.sample)
						}
						val desc = td.description
						if (desc != null) {
							textflow {
								// text("Description: ").addClass("bold")
								text(desc)
							}
						}
					}
				}
			}
		}
	}
	
	fun recreate() {
		val _tags =
				TagLib.taglist.map { it.iAmEitherLeft() } +
						TagLib.aliasList.map { it.iAmEitherRight() }
		tags.setAll(
				_tags.sortedBy {
					it.fold({ l -> l.name }, { r -> r.name })
				}
		)
	}
	
	init {
		recreate()
	}
}