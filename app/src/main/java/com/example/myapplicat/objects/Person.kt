package com.example.myapplicat.objects

data class Person(var name: String = "", var surname: String = "", var fathname: String = "",
                  var email: String = "", var password: String = "", var role: String = "-1")
    :java.io.Serializable