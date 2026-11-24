package ua.edu.chnu.kkn.quotely.di

import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val platformModule: Module = module {
    single {
        KSafe(
            baseDir = File(System.getProperty("user.home"), ".quotely"),
            config = KSafeConfig(appNamespace = "ua.edu.chnu.kkn.quotely"),
        )
    }
    single<HttpClientEngine> { CIO.create() }
}
