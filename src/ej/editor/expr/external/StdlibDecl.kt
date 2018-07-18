package ej.editor.expr.external

import tornadofx.*
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

/*
 * Created by aimozg on 18.07.2018.
 * Confidential unless published on GitHub
 */
@XmlRootElement(name="stdlib")
class StdlibDecl {
	@get:XmlElement(name="function")
	val functions = ArrayList<FunctionDecl>().observable()

	@get:XmlElement(name="enum")
	val enums = ArrayList<EnumDecl>().observable()
}