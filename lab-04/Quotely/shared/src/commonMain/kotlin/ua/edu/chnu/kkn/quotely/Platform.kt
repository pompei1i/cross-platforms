package ua.edu.chnu.kkn.quotely

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform