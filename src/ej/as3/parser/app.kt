package ej.as3.parser

import java.io.File

/*
 * Created by aimozg on 10.12.2018.
 * Confidential until published on GitHub
 */
fun main(args: Array<String>) {
	println(ActionScriptParser().parseFile(File(args[0]).readText()))
}