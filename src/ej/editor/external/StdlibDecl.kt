package ej.editor.external

import ej.editor.expr.*
import ej.editor.expr.impl.ExternalEnumBuilder
import ej.editor.expr.impl.ExternalFunctionBuilder
import ej.mod.ModData
import ej.utils.Validable
import ej.utils.ValidationReport
import ej.xml.*
import java.io.File
import java.io.InputStream

/*
 * Created by aimozg on 18.07.2018.
 * Confidential unless published on GitHub
 */
@RootElement("stdlib")
class StdlibDecl : XmlAutoSerializable, PartialBuilderConverter<CallExpression>, Validable {
	@Elements("function")
	val functions = ArrayList<FunctionDecl>()

	@Elements("enum")
	val enums = ArrayList<EnumDecl>()
	
	@Elements("command")
	val commands = ArrayList<CommandDecl>()
	
	fun functionsReturning(rawType:String) = functions.filter {
		it.returnTypeRaw == rawType
	}
	
	fun commandBuilders(): List<ExpressionBuilder> = commands.map {
		ExternalFunctionBuilder(it)
	}
	
	fun buildersReturning(rawType:String):List<ExpressionBuilder> = listOfNotNull(
			enumByTypeName(rawType)?.let {
				ExternalEnumBuilder(it)
			}
	) + functionsReturning(rawType).map {
		ExternalFunctionBuilder(it)
	}
	fun functionByName(name:String) = functions.find { it.name == name }
	fun commandByName(name: String) = commands.find { it.name == name }
	fun enumByTypeName(typename:String) = enums.find { it.name == typename }
	
	fun tryConvertCommand(converter: BuilderConverter, expr: CallExpression): ExpressionBuilder? {
		val id = expr.function.asId?.value ?: return null
		val command = commandByName(id) ?: return null
		return tryConvert(expr, command, converter)
	}
	override fun tryConvert(converter: BuilderConverter, expr: CallExpression): ExpressionBuilder? {
		val id = expr.function.asId?.value ?: return null
		val function = functionByName(id) ?: return null
		return tryConvert(expr, function, converter)
	}
	
	private fun tryConvert(expr: CallExpression,
	                       decl: ExpressionDecl,
	                       converter: BuilderConverter): ExternalFunctionBuilder? {
		val arity = expr.arguments.size
		if (decl.arity != arity) return null
		val efb = ExternalFunctionBuilder(decl)
		for ((i, param) in decl.params.withIndex()) {
			efb.params[i].value = converter.convert(expr.arguments[i], param.type)
		}
		return efb
	}
	
	override fun validate() = ValidationReport.build {
		validateAll(functions,"functions")
	}
	fun removeInvalidElements() {
		functions.removeAll { !it.validate().isValid }
	}
}

val Stdlib: StdlibDecl by lazy<StdlibDecl> {
	var sd: StdlibDecl? = null
	try {
		val f = File("stdlib.xml")
		if (f.exists() && f.canRead()) sd = loadStdlib(f.inputStream())
	} catch (e:Exception) {
		e.printStackTrace()
	}
	if (sd == null) sd = loadStdlib(ModData::class.java.getResourceAsStream("stdlib.xml"))
	val vr = sd.validate()
	if (!vr.isValid) {
		vr.validationErrors().forEach {
			System.err.println(it)
		}
		sd.removeInvalidElements()
	}
	sd
}

fun loadStdlib(input: InputStream): StdlibDecl {
	return getSerializationInfo<StdlibDecl>().deserializeDocument(XmlExplorer(input))
}