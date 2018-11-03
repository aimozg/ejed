package ej.utils

import java.util.*

/*
 * Created by aimozg on 14.10.2018.
 * Confidential until published on GitHub
 *
 * https://www.codeproject.com/Articles/25172/Simple-Random-Number-Generation
 * https://en.wikipedia.org/wiki/Multiply-with-carry
 */
class RandomMwc(seed: Long) : Random(seed) {
	private var mz = 1
	private var mw = 1
	
	constructor(other: RandomMwc) : this(other.toSeed())
	constructor() : this(Random().nextLong())
	
	fun toSeed(): Long = (mz.toLong() shl 32) or mw.toLong().and(0xffff_ffffL)
	
	fun fork() = RandomMwc(this)
	
	override fun setSeed(seed: Long) {
		mz = (seed shr 32).toInt()
		mw = seed.toInt()
	}
	
	override fun next(bits: Int): Int {
		mz = 36969 * (mz and 65535) + (mz ushr 16)
		mw = 18000 * (mw and 65535) + (mw ushr 16)
		return ((mz shl 16) + mw) ushr (32 - bits)
	}
	
	override fun nextDouble(): Double {
		// 0 <= u < 2^32
		val u = next(32).toLong().and(0xffff_ffffL)
		// The result is strictly between 0 and 1.
		return (u + 1.0) * (1.0 / ((2 shl 32) + 2.0))
	}
	
	fun nextInt(range: IntRange) = range.first + nextInt(range.endInclusive + 1 - range.first)
	fun <T> nextObject(vararg objects: T) = objects[nextInt(objects.indices)]
}