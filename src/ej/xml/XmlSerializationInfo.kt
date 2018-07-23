package ej.xml

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType
import java.lang.reflect.Array as JArray

/*
 * Created by aimozg on 20.07.2018.
 * Confidential until published on GitHub
 */
class XmlSerializationInfo<T : Any>(internal val klass: KClass<T>) {
	fun accepts(e: Any) = klass.isInstance(e)
	@Suppress("UNCHECKED_CAST")
	fun serializeIfAccepts(
			e: Any,
			tag: String,
			output: XmlBuilder,
			attrModifier: (Map<String,String>)->Map<String,String> ={it}
	): Boolean {
		if (accepts(e)) {
			serialize((e as T), tag, output,attrModifier)
			return true
		}
		return false
	}
	
	internal var name: String? = null
	val nameOrClass: String get() = name ?: klass.simpleName ?: klass.toString()
	internal val attri = HashMap<String, AttrConsumer<T>>()
	internal var texti: TextConsumer<T>? = null
	internal val elements = HashMap<String, ElementConsumer<T>>()
	internal val attro = ArrayList<AttrProducer<T>>()
	internal val producers = ArrayList<XmlProducer<T>>()
	internal var beforeSave: (T.() -> Unit)? = null
	internal var afterSave: (T.() -> Unit)? = null
	internal var beforeLoad: (T.(Any?) -> Unit)? = null
	internal var afterLoad: (T.(Any?) -> Unit)? = null
	internal val constructor = klass.constructors
			.find { it.parameters.none { p -> !p.isOptional && !p.isVararg } }
			?.apply { isAccessible = true }
	internal val createInstance: (() -> T)?
	
	init {
		createInstance = when {
			constructor == null -> null
			constructor.parameters.isEmpty() -> ({ constructor.call() })
			else -> {
				val varargParam = constructor.parameters.find { it.isVararg }
				val argmap: Map<KParameter, Any?> = if (varargParam == null) {
					emptyMap()
				} else {
					val javaClass = varargParam.type.javaType as Class<*>
					mapOf(varargParam to JArray.newInstance(javaClass.componentType,0))
				}
				{ constructor.callBy(argmap) }
			}
		}
	}
	
	internal var defaultAttrConsumer = object : AttrConsumer<T> {
		override fun consumeAttr(obj: T, key: String, value: String) {
			error("unknown attribute $nameOrClass@$key")
		}
	}
	internal var defaultTextConsumer = object : TextConsumer<T> {
		override fun consumeText(obj: T, data: String) {
			if (data.isNotBlank()) error("unexpected text $nameOrClass $data")
		}
	}
	internal var defaultElementConsumer = object : ElementConsumer<T> {
		override fun consumeElement(obj: T,
		                            tag: String,
		                            attrs: Map<String, String>,
		                            input: XmlExplorerController) {
			error("unexpected element $nameOrClass.$tag")
		}
		
	}
	
}

