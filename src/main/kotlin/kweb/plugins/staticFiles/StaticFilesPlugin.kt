package kweb.plugins.staticFiles

import io.ktor.server.application.install
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.http.CacheControl
import io.ktor.http.content.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import kweb.plugins.KwebPlugin
import java.io.File

/**
 * @author rpanic
 *
 * This Plugin serves static files to be used in the frontend
 *
 * @property rootFolder The root folder, where the static assets are saved
 * @property resourceFolder For serving resources, the path to the folder which will be served
 * @property servedRoute The route where these assets are being served
 */
class StaticFilesPlugin private constructor(private val servedRoute: String, private val maxCacheAgeSeconds: Int = 60 * 60) : KwebPlugin() {

    private lateinit var datasource: (Route) -> Unit

    constructor(rootFolder: File, servedRoute: String) : this(servedRoute) {
        datasource = {
            it.staticRootFolder = rootFolder
        }
    }

    constructor(resourceFolder: ResourceFolder, servedRoute: String) : this(servedRoute) {
        datasource = {
            it.resources(resourceFolder.resourceFolder)
        }
    }

    override fun appServerConfigurator(routeHandler: Routing) {
        routeHandler.static(servedRoute) {
            install(CachingHeaders) {
                /*
                TODO: Ideally the asset path would contain a hash of the file content
                TODO: so that we can set a very long cache time (> 1 year).  For now it defaults
                TODO: to one hour.
                 */
                options { call, content ->
                    CachingOptions(CacheControl.MaxAge(maxAgeSeconds = maxCacheAgeSeconds))
                }
            }

            datasource(this)
            files(".")
        }

    }

}

data class ResourceFolder(val resourceFolder: String)