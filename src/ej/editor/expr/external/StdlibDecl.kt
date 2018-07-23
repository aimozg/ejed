package ej.editor.expr.external

import ej.editor.expr.*
import ej.mod.ModData
import ej.xml.*
import java.io.File
import java.io.InputStream

/*
 * Created by aimozg on 18.07.2018.
 * Confidential unless published on GitHub
 */
@RootElement("stdlib")
class StdlibDecl : XmlAutoSerializable, PartialBuilderConverter<CallExpression> {
	@Elements("function")
	val functions = ArrayList<FunctionDecl>()

	@Elements("enum")
	val enums = ArrayList<EnumDecl>()
	
	@Elements("command")
	val commands = ArrayList<CommandDecl>()
	
	fun functionsReturning(rawType:String) = functions.filter {
		it.returnTypeRaw == rawType
	}
	fun buildersReturning(rawType:String):List<ExpressionBuilder> = listOfNotNull(
			enumByTypeName(rawType)?.let {
				ExternalEnumBuilder(it)
			}
	) + functionsReturning(rawType).map {
		ExternalFunctionBuilder(it)
	}
	fun functionByName(name:String) = functions.find { it.name == name }
	fun enumByTypeName(typename:String) = enums.find { it.name == typename }
	
	override fun tryConvert(converter: BuilderConverter, expr: CallExpression): ExpressionBuilder? {
		val id = expr.function.asId?.value ?: return null
		val arity = expr.arguments.size
		val function = functionByName(id) ?: return null
		if (function.arity != arity) return null
		val efb = ExternalFunctionBuilder(function)
		for ((argument,param) in expr.arguments.zip(efb.params)) {
			param.value = converter.convert(argument)
		}
		return efb
	}
}

val Stdlib:StdlibDecl by lazy {
	try {
		val f = File("stdlib.xml")
		if (f.exists() && f.canRead()) return@lazy loadStdlib(f.inputStream())
	} catch (e:Exception) {
		e.printStackTrace()
	}
	loadStdlib(ModData::class.java.getResourceAsStream("stdlib.xml"))
}

fun loadStdlib(input: InputStream):StdlibDecl {
	return getSerializationInfo<StdlibDecl>().deserializeDocument(XmlExplorer(input))
}