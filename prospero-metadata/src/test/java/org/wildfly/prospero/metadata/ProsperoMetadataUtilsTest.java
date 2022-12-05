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

package org.wildfly.prospero.metadata;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.channel.Channel;
import org.wildfly.channel.ChannelManifest;
import org.wildfly.channel.ChannelManifestCoordinate;
import org.wildfly.channel.ChannelManifestMapper;
import org.wildfly.channel.ChannelMapper;
import org.wildfly.channel.Repository;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.wildfly.prospero.metadata.ProsperoMetadataUtils.MANIFEST_FILE_NAME;
import static org.wildfly.prospero.metadata.ProsperoMetadataUtils.METADATA_DIR;
import static org.wildfly.prospero.metadata.ProsperoMetadataUtils.PROSPERO_CONFIG_FILE_NAME;

public class ProsperoMetadataUtilsTest {

    private static final Channel A_CHANNEL = new Channel("test-channel", null, null, null,
            List.of(new Repository("test-repo", "http://test.te")),
            new ChannelManifestCoordinate("foo", "bar"));
    private static final ChannelManifest A_MANIFEST = new ChannelManifest("test-manifest", null, null);
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
    private Path server;
    private Path manifestPath;
    private Path channelPath;

    @Before
    public void setUp() throws Exception {
        server = temp.newFolder().toPath();
        manifestPath = server.resolve(METADATA_DIR).resolve(MANIFEST_FILE_NAME);
        channelPath = server.resolve(METADATA_DIR).resolve(PROSPERO_CONFIG_FILE_NAME);
    }

    @Test
    public void createWhenMetadatDirDoesntExist() throws Exception {
        ProsperoMetadataUtils.generate(server, List.of(A_CHANNEL), A_MANIFEST);

        assertTrue(Files.exists(manifestPath));
        assertTrue(Files.exists(channelPath));

        assertMetadataWritten();
    }

    @Test
    public void createWhenMetadatDirDoesExist() throws Exception {
        Files.createDirectory(server.resolve(METADATA_DIR));
        ProsperoMetadataUtils.generate(server, List.of(A_CHANNEL), A_MANIFEST);

        assertTrue(Files.exists(manifestPath));
        assertTrue(Files.exists(channelPath));

        assertMetadataWritten();
    }

    @Test
    public void throwErrorIfManifestFilesExist() throws Exception {
        Files.createDirectory(server.resolve(METADATA_DIR));
        Files.createFile(manifestPath);

        assertThrows(IllegalArgumentException.class, ()->ProsperoMetadataUtils.generate(server, List.of(A_CHANNEL), A_MANIFEST));

        assertEquals("", Files.readString(manifestPath));
        assertFalse(Files.exists(channelPath));
    }

    @Test
    public void throwErrorIfChannelsFilesExist() throws Exception {
        Files.createDirectory(server.resolve(METADATA_DIR));
        Files.createFile(channelPath);

        assertThrows(IllegalArgumentException.class, ()->ProsperoMetadataUtils.generate(server, List.of(A_CHANNEL), A_MANIFEST));

        assertEquals("", Files.readString(channelPath));
        assertFalse(Files.exists(manifestPath));
    }

    @Test
    public void throwErrorIfMetadataFolderIsFile() throws Exception {
        Files.createFile(server.resolve(METADATA_DIR));

        assertThrows(IllegalArgumentException.class, ()->ProsperoMetadataUtils.generate(server, List.of(A_CHANNEL), A_MANIFEST));

        assertFalse(Files.exists(manifestPath));
        assertFalse(Files.exists(channelPath));
    }

    private void assertMetadataWritten() throws MalformedURLException {
        final Channel channel = ChannelMapper.from(channelPath.toUri().toURL());
        final ChannelManifest manifest = ChannelManifestMapper.from(manifestPath.toUri().toURL());
        assertEquals("test-channel", channel.getName());
        assertEquals("test-manifest", manifest.getName());
    }


}