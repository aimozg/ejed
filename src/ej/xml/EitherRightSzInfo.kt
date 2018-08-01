package ej.xml

import org.funktionale.either.Either
import kotlin.reflect.KClass

/*
 * Created by aimozg on 01.08.2018.
 * Confidential until published on GitHub
 */

class EitherRightSzInfo<E:XmlSerializable>(
		klass: KClass<E>
): AXmlSerializationInfo<Either<String, E>>() {
	val wrapped = getSerializationInfo(klass)
	
	override val name: String? = wrapped.name
	
	override fun accepts(e: Any): Boolean {
		return (e as? Either<Any?,Any?>)?.fold(
				{false},
				{it != null && wrapped.accepts(it)}
		)?:false
	}
	
	override fun deserialize(input: XmlExplorerController,
	                         myAttrs: Map<String, String>,
	                         parent: Any?): Either<String, E> {
		return Either.right(wrapped.deserialize(input,myAttrs, parent))
	}
	
	override fun serialize(obj: Either<String, E>,
	                       tag: String,
	                       output: XmlBuilder,
	                       attrModifier: (Map<String, String>) -> Map<String, String>) {
		obj.fold({
			output.text(it)
		         },{
			wrapped.serialize(it,tag,output,attrModifier)
		})
	}
	
}