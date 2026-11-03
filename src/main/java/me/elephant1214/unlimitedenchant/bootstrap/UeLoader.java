package me.elephant1214.unlimitedenchant.bootstrap;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import static io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class UeLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", MAVEN_CENTRAL_DEFAULT_MIRROR).build());

        VersionCatalog.KOTLIN.forEach(resolver::addDependency);

        classpathBuilder.addLibrary(resolver);
    }
}
