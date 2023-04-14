/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.prospero.actions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.wildfly.channel.Channel;
import org.wildfly.prospero.ProsperoLogger;
import org.wildfly.prospero.api.InstallationMetadata;
import org.wildfly.prospero.api.exceptions.MetadataException;
import org.wildfly.prospero.model.ProsperoConfig;

/**
 * Metadata related actions wrapper.
 */
public class MetadataAction implements AutoCloseable {
    private final InstallationMetadata installationMetadata;

    public MetadataAction(Path installation) throws MetadataException {
        this.installationMetadata = InstallationMetadata.loadInstallation(installation);
    }

    protected MetadataAction(InstallationMetadata installationMetadata) {
        this.installationMetadata = installationMetadata;
    }

    public void addChannel(Channel channel) throws MetadataException {
        ProsperoLogger.ROOT_LOGGER.addingChannel(channel.toString());
        final ProsperoConfig prosperoConfig = installationMetadata.getProsperoConfig();
        final List<Channel> channels = prosperoConfig.getChannels();

        if (channels.stream().filter(c->c.getName().equals(channel.getName())).findAny().isPresent()) {
            ProsperoLogger.ROOT_LOGGER.existingChannel(channel.getName());
            throw ProsperoLogger.ROOT_LOGGER.channelExists(channel.getName());
        }

        channels.add(channel);
        installationMetadata.updateProsperoConfig(prosperoConfig);
        ProsperoLogger.ROOT_LOGGER.channelAdded(channel.getName());
    }

    public void removeChannel(String channelName) throws MetadataException {
        ProsperoLogger.ROOT_LOGGER.removingChannel(channelName);
        final ProsperoConfig prosperoConfig = installationMetadata.getProsperoConfig();
        final List<Channel> channels = prosperoConfig.getChannels();
        final Optional<Channel> removedChannel = channels.stream().filter(c -> c.getName().equals(channelName)).findAny();
        if (removedChannel.isEmpty()) {
            final MetadataException ex = ProsperoLogger.ROOT_LOGGER.channelNotFound(channelName);
            ProsperoLogger.ROOT_LOGGER.warnf(ex, "");
            throw ex;
        }
        channels.remove(removedChannel.get());
        installationMetadata.updateProsperoConfig(prosperoConfig);
        ProsperoLogger.ROOT_LOGGER.channelRemoved(channelName);
    }

    public void changeChannel(String channelName, Channel newChannel) throws MetadataException {
        ProsperoLogger.ROOT_LOGGER.updatingChannel(newChannel.toString(), channelName);
        final ProsperoConfig prosperoConfig = installationMetadata.getProsperoConfig();
        final List<Channel> channels = prosperoConfig.getChannels();
        final Optional<Channel> modifiedChannel = channels.stream().filter(c -> c.getName().equals(channelName)).findAny();
        if (modifiedChannel.isEmpty()) {
            ProsperoLogger.ROOT_LOGGER.channelNotFound(channelName);
            throw ProsperoLogger.ROOT_LOGGER.channelNotFound(channelName);
        }
        channels.set(channels.indexOf(modifiedChannel.get()), newChannel);
        installationMetadata.updateProsperoConfig(prosperoConfig);
        ProsperoLogger.ROOT_LOGGER.channelUpdated(channelName);
    }

    public List<Channel> getChannels() throws MetadataException {
        ProsperoLogger.ROOT_LOGGER.listChannels();
        return new ArrayList<>(installationMetadata.getProsperoConfig().getChannels());
    }

    @Override
    public void close() {
        this.installationMetadata.close();
    }
}
