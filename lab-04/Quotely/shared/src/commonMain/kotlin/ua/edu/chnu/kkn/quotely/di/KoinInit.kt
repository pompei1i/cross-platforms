package ua.edu.chnu.kkn.quotely.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatform

/** Starts Koin once per process; platforms add their own setup through [config] (e.g. the Android context). */
fun initKoin(config: KoinAppDeclaration? = null): KoinApplication = startKoin {
    config?.invoke(this)
    modules(appModules)
}

/** For entry points that may be created more than once (the iOS view controller). */
fun initKoinIfNeeded() {
    if (KoinPlatform.getKoinOrNull() == null) initKoin()
}
