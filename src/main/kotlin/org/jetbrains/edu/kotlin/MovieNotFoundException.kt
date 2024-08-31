package org.jetbrains.edu.kotlin

class MovieNotFoundException(movieName: String) : RuntimeException("Could not find movie $movieName")
