package com.github.titovart.involut.functional


fun String.collapseIdent() = this.trimIndent().split("\n").joinToString("") { it.trim() }
