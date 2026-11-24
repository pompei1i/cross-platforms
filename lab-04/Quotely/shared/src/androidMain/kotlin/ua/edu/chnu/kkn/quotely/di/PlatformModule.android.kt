package ua.edu.chnu.kkn.quotely.di

import eu.anifantakis.lib.ksafe.KSafe
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { KSafe(androidContext()) }
    single<HttpClientEngine> { OkHttp.create() }
}
