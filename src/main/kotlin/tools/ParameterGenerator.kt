package ar.edu.itba.pf.tools

import java.io.File

class ParameterGenerator(
    usernamesPath: String = "resources/usernames.txt",
    namesPath: String = "resources/names.txt",
    surnamesPath: String = "resources/surnames.txt",
    passwordsPath: String = "resources/password.txt",
    emailsPath: String = "resources/emails.txt",
) {

    private val usernames = File(usernamesPath).readLines().toHashSet()
    private val names = File(namesPath).readLines().toHashSet()
    private val surnames = File(surnamesPath).readLines().toHashSet()
    private val passwords = File(passwordsPath).readLines().toHashSet()
    private val emailSuffixes = File(emailsPath).readLines().toHashSet()

    fun generateUsername(): String = usernames.random().plus((0..1000000).random())

    fun generatePassword(): String = passwords.random()

    fun generateName(): String = names.random()

    fun generateSurname(): String = surnames.random()

    fun generateEmail(): String = names.random() + (0..100000).random() + emailSuffixes.random()
}