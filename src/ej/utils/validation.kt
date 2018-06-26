package ej.utils

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

/*
 * Created by aimozg on 25.06.2018.
 * Confidential until published on GitHub
 */

@Target(AnnotationTarget.PROPERTY)
annotation class ValidateNonEmpty(val reason:String = "{1} cannot be empty")
@Target(AnnotationTarget.PROPERTY)
annotation class ValidateNonBlank(val reason:String = "{1} cannot be blank")
@Target(AnnotationTarget.PROPERTY)
annotation class ValidateUnique(val reason:String = "{1} must be unique")
@Target(AnnotationTarget.PROPERTY)
annotation class ValidateElements(val locator:String = "\$index", val reason:String = "element {1} is invalid ({2})")

class PropertyValidationResult(val propname:String, val errorReport:String, val isValid:Boolean) {
	constructor(propname:String,errorReport:String):this(propname,errorReport,false)
	constructor(propname:String,isValid:Boolean):this(propname,"",isValid)
}

open class PropertyValidator<K:Any,R:Any?>(val prop:KProperty1<K,R>) {
	val nonEmpty: ValidateNonEmpty? = prop.findAnnotation()
	val nonBlank: ValidateNonBlank? = prop.findAnnotation()
	val unique: ValidateUnique? = prop.findAnnotation()
	
	val propname = prop.name
	
	private fun fail(reason:String) =
			PropertyValidationResult(propname,reason
					.replace("{1}",propname))
	protected fun ValidateNonEmpty.errorOrNull(value:R):PropertyValidationResult? {
		if (value == null) return fail(reason)
		if ((value as? Collection<*>)?.isEmpty() == true) return fail(reason)
		if ((value as? Array<*>)?.isEmpty() == true) return fail(reason)
		if (value.toString().isEmpty()) return fail(reason)
		return null
	}
	protected fun ValidateNonBlank.errorOrNull(value:R):PropertyValidationResult? {
		if (value?.toString().isNullOrBlank()) return fail(reason)
		return null
	}
	protected fun ValidateUnique.errorOrNull(value:R, receiver:K, context:Collection<K?>?):PropertyValidationResult? {
		if (context == null) return null
		if (context.any { it != null && it != receiver && prop.get(it) == value }) return fail(reason)
		return null
	}
	
	open fun validate(receiver:K, context: Collection<K?>? = null):PropertyValidationResult {
		val value = prop.get(receiver)
		return validateValue(value)
				?: unique?.errorOrNull(value, receiver, context)
				?: PropertyValidationResult(propname, true)
	}
	
	open fun validateValue(value: R): PropertyValidationResult? {
		return nonEmpty?.errorOrNull(value)
				?: nonBlank?.errorOrNull(value)
	}
}
open class CollectionPropertyValidator<K:Any,R:Collection<E?>?,E:Any>(prop:KProperty1<K,R>) : PropertyValidator<K,R>(prop) {
	val elements: ValidateElements? = prop.findAnnotation()
	private fun ValidateElements.fail(locatorValue:String,elementReason:String) =
			PropertyValidationResult(propname,reason
					.replace("{1}",locatorValue)
					.replace("{2}",elementReason))
	
	protected fun ValidateElements.locatorValue(i:Int,e:E):String {
		if (locator == "\$index") return i.toString()
		return propertyValue(e,locator).toString()
	}
	protected fun ValidateElements.errorOrNull(value:R):PropertyValidationResult? {
		if (value == null) return null
		val valueList = value.toList<E?>()
		for ((i,e) in valueList.withIndex()) {
			if (e != null) {
				val r = classValidatorFor(e::class).validate(e, valueList)
				if (!r.valid) return fail(locatorValue(i,e), r.errorReport)
			}
		}
		return null
	}
	
	override fun validateValue(value: R): PropertyValidationResult? {
		return elements?.errorOrNull(value)
				?: super.validateValue(value)
	}
}


class ValidationResult(properties:Collection<PropertyValidationResult>) {
	val valid:Boolean
	val errorReport:String
	init {
		val errors = properties.mapNotNull {
			if (it.isValid) null else it.errorReport
		}
		valid = errors.isEmpty()
		errorReport = errors.joinToString(", ")
	}
	
	override fun toString(): String =
			if (valid) "ValidationResult(valid)"
			else "ValidationResult(invalid, $errorReport)"
}

class ClassValidator<K:Any> internal constructor(val klass: KClass<K>) {
	private val properties = ArrayList<PropertyValidator<K,*>>()
	
	init {
		klass.declaredMemberProperties.mapTo(properties) { propertyValidatorFor(it) }
	}
	
	fun validate(receiver:K, context: Collection<K?>? = null):ValidationResult {
		return ValidationResult(properties.map { it.validate(receiver, context) })
	}
}
private val VALIDATORS = HashMap<Class<*>,ClassValidator<*>>()

internal fun<K:Any,R:Any?> propertyValidatorFor(property:KProperty1<K,R>):
		PropertyValidator<K,R> {
	if (property.returnType.isSubtypeOf(Collection::class.starProjectedType)) {
		@Suppress("UNCHECKED_CAST")
		return CollectionPropertyValidator(property as KProperty1<K,Collection<*>>) as PropertyValidator<K,R>
	} else {
		return PropertyValidator(property)
	}
}

@Suppress("UNCHECKED_CAST")
fun<K:Any> classValidatorFor(klass:KClass<out K>) = VALIDATORS.getOrPut(klass.java) {
	ClassValidator(klass)
} as ClassValidator<K>

inline fun<reified K:Any> classValidatorFor() = classValidatorFor(K::class)


internal fun<K:Any> propertyValue(e:K,name:String):Any? {
	@Suppress("UNCHECKED_CAST")
	val property: KProperty1<K, Any?> = e::class.memberProperties.first { it.name == name } as KProperty1<K, Any?>
	return property.get(e)
}