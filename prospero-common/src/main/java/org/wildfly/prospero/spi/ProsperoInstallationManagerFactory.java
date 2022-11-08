package org.wildfly.prospero.spi;


import org.wildfly.installationmanager.MavenOptions;
import org.wildfly.installationmanager.spi.InstallationManager;
import org.wildfly.installationmanager.spi.InstallationManagerFactory;

import java.nio.file.Path;

public class ProsperoInstallationManagerFactory implements InstallationManagerFactory {

    @Override
    public InstallationManager create(Path installationDir, MavenOptions mavenOptions) throws Exception {
        return new ProsperoInstallationManager(installationDir, mavenOptions);
    }

    @Override
    public String getName() {
        return "prospero";
    }
}
