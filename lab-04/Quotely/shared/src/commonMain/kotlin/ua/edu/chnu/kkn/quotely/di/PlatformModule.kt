package ua.edu.chnu.kkn.quotely.di

import org.koin.core.module.Module

/** Platform-specific dependencies: the KSafe secure storage and the ktor HTTP engine. */
expect val platformModule: Module
