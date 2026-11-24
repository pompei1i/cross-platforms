package ua.edu.chnu.kkn.quotely

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import ua.edu.chnu.kkn.quotely.di.initKoin

class QuotelyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@QuotelyApplication)
            androidLogger()
        }
    }
}
