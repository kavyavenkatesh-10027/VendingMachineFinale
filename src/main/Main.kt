package main

import ui.AdminUI
import ui.ConsumerUI

fun main() {
    SampleDataGenerator.load()

    val adminUI    = AdminUI()
    val consumerUI = ConsumerUI()

    println("""
        --------------------------
        Welcome to Vending Machine
        --------------------------
    """.trimIndent())

    var running = true
    while (running) {
        println("""
  (1) Enter as Admin
  (2) Enter as Customer
  (0) Exit
        """.trimIndent())
        when (readln().trim()) {
            "1"  -> if (validateAdmin()) adminUI.show() else println("Wrong passcode!")
            "2"  -> consumerUI.show()
            "0"  -> running = false
            else -> println("Invalid choice.")
        }
    }
}

fun validateAdmin(): Boolean {
    print("Enter passcode: ")
    return readln().trim() == "Aloha"
}