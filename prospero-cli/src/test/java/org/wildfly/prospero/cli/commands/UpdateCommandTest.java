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

package org.wildfly.prospero.cli.commands;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.wildfly.prospero.api.ArtifactChange;
import org.wildfly.prospero.cli.ActionFactory;
import org.wildfly.prospero.cli.CliMessages;
import org.wildfly.prospero.cli.ReturnCodes;
import org.wildfly.prospero.updates.UpdateSet;
import org.wildfly.prospero.wfchannel.MavenSessionManager;
import org.wildfly.prospero.test.MetadataTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import org.wildfly.prospero.actions.ApplyUpdateAction;
import org.wildfly.prospero.actions.BuildUpdateAction;

@RunWith(MockitoJUnitRunner.class)
public class UpdateCommandTest extends AbstractMavenCommandTest {

    public static final String A_PROSPERO_FP = UpdateCommand.PROSPERO_FP_GA + ":1.0.0";
    public static final String OTHER_FP = "com.another:galleon-pack:1.0.0";
    public static final String MODULES_DIR = "modules";

    @Mock
    private BuildUpdateAction buildUpdateAction;

    @Mock
    private ApplyUpdateAction applyUpdateAction;

    @Mock
    private ActionFactory actionFactory;

    @Captor
    private ArgumentCaptor<MavenSessionManager> mavenSessionManager;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Path installationDir;

    @Override
    protected ActionFactory createActionFactory() {
        return actionFactory;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(actionFactory.buildUpdate(any(), any(), any(), any(), any())).thenReturn(buildUpdateAction);
        when(actionFactory.applyUpdate(any(), any(), any(), any(), any())).thenReturn(applyUpdateAction);
        installationDir = tempFolder.newFolder().toPath();

        MetadataTestUtils.createInstallationMetadata(installationDir);
        MetadataTestUtils.createGalleonProvisionedState(installationDir, A_PROSPERO_FP);
    }

    @After
    public void tearDown() {
        System.clearProperty(UpdateCommand.JBOSS_MODULE_PATH);
    }

    @Test
    public void currentDirNotValidInstallation() {
        int exitCode = commandLine.execute(CliConstants.Commands.UPDATE);

        Assert.assertEquals(ReturnCodes.INVALID_ARGUMENTS, exitCode);
        assertTrue(getErrorOutput().contains(CliMessages.MESSAGES.invalidInstallationDir(UpdateCommand.currentDir().toAbsolutePath())
                .getMessage()));
    }

    @Test
    public void callUpdate() throws Exception {
        when(buildUpdateAction.findUpdates()).thenReturn(new UpdateSet(List.of(change("1.0.0", "1.0.1"))));
        int exitCode = commandLine.execute(CliConstants.Commands.UPDATE, CliConstants.DIR, installationDir.toString());

        assertEquals(ReturnCodes.SUCCESS, exitCode);
        Mockito.verify(applyUpdateAction).applyUpdate();
    }

    @Test
    public void selfUpdateRequiresModulePathProp() {
        int exitCode = commandLine.execute(CliConstants.Commands.UPDATE, CliConstants.SELF);

        assertEquals(ReturnCodes.INVALID_ARGUMENTS, exitCode);
        assertTrue(getErrorOutput().contains(CliMessages.MESSAGES.unableToLocateProsperoInstallation().getMessage()));
    }

    @Test
    public void selfUpdatePassesModulePathAsDir() throws Exception {
        System.setProperty(UpdateCommand.JBOSS_MODULE_PATH, installationDir.resolve(MODULES_DIR).toString());
        when(buildUpdateAction.findUpdates()).thenReturn(new UpdateSet(List.of(change("1.0.0", "1.0.1"))));

        int exitCode = commandLine.execute(CliConstants.Commands.UPDATE, CliConstants.SELF);

        assertEquals(ReturnCodes.SUCCESS, exitCode);
        Mockito.verify(actionFactory).buildUpdate(eq(installationDir.toAbsolutePath()), any(), any(), any(), any());
        Mockito.verify(applyUpdateAction).applyUpdate();
    }

    @Test
    public void dirParameterOverridesModulePathInSelfUpdate() throws Exception {
        System.setProperty(UpdateCommand.JBOSS_MODULE_PATH, installationDir.toString());
        when(buildUpdateAction.findUpdates()).thenReturn(new UpdateSet(List.of(change("1.0.0", "1.0.1"))));

        int exitCode = commandLine.execute(CliConstants.Commands.UPDATE, CliConstants.SELF,
                CliConstants.DIR, installationDir.toAbsolutePath().toString());

        assertEquals(ReturnCodes.SUCCESS, exitCode);
        Mockito.verify(actionFactory).buildUpdate(eq(installationDir.toAbsolutePath()), any(), any(), any(), any());
        Mockito.verify(applyUpdateAction).applyUpdate();
    }

    @Test
    public void selfUpdateFailsIfMultipleFPsDetected() throws Exception {
        MetadataTestUtils.createGalleonProvisionedState(installationDir, A_PROSPERO_FP, OTHER_FP);
        int exitCode = commandLine.execute(CliConstants.Commands.UPDATE, CliConstants.SELF,
                CliConstants.DIR, installationDir.toAbsolutePath().toString());

        assertEquals(ReturnCodes.INVALID_ARGUMENTS, exitCode);
        assertTrue(getErrorOutput().contains(
                CliMessages.MESSAGES.unexpectedPackageInSelfUpdate(installationDir.toAbsolutePath().toString()).getMessage()));
    }

    @Test
    public void selfUpdateFailsIfProsperoFPNotDetected() throws Exception {
        MetadataTestUtils.createGalleonProvisionedState(installationDir, OTHER_FP);
        int exitCode = commandLine.execute(CliConstants.Commands.UPDATE, CliConstants.SELF,
                CliConstants.DIR, installationDir.toString());

        assertEquals(ReturnCodes.INVALID_ARGUMENTS, exitCode);
        assertTrue(getErrorOutput().contains(
                CliMessages.MESSAGES.unexpectedPackageInSelfUpdate(installationDir.toAbsolutePath().toString()).getMessage()));
    }

    @Test
    public void testAskForConfirmation() throws Exception {
        System.setProperty(UpdateCommand.JBOSS_MODULE_PATH, installationDir.toString());
        when(buildUpdateAction.findUpdates()).thenReturn(new UpdateSet(List.of(change("1.0.0", "1.0.1"))));
        this.setDenyConfirm(true);

        int exitCode = commandLine.execute(CliConstants.Commands.UPDATE, CliConstants.SELF,
                CliConstants.DIR, installationDir.toAbsolutePath().toString());

        assertEquals(ReturnCodes.SUCCESS, exitCode);
        Mockito.verify(actionFactory).buildUpdate(eq(installationDir.toAbsolutePath()), any(), any(), any(), any());
        assertEquals(1, getAskedConfirmation());
        Mockito.verify(applyUpdateAction, never()).applyUpdate();
    }

    @Test
    public void testConfirmedConfirmation() throws Exception {
        System.setProperty(UpdateCommand.JBOSS_MODULE_PATH, installationDir.toString());
        when(buildUpdateAction.findUpdates()).thenReturn(new UpdateSet(List.of(change("1.0.0", "1.0.1"))));

        int exitCode = commandLine.execute(CliConstants.Commands.UPDATE, CliConstants.SELF,
                CliConstants.DIR, installationDir.toAbsolutePath().toString());

        assertEquals(ReturnCodes.SUCCESS, exitCode);
        Mockito.verify(actionFactory).buildUpdate(eq(installationDir.toAbsolutePath()), any(), any(), any(), any());
        assertEquals(1, getAskedConfirmation());
        Mockito.verify(applyUpdateAction).applyUpdate();
    }

    private ArtifactChange change(String oldVersion, String newVersion) {
        return new ArtifactChange(new DefaultArtifact("org.foo", "bar", null, oldVersion),
                new DefaultArtifact("org.foo", "bar", null, newVersion));
    }

    @Override
    protected void doLocalMock() throws Exception {
        when(buildUpdateAction.findUpdates()).thenReturn(new UpdateSet(List.of(change("1.0.0", "1.0.1"))));
    }

    @Override
    protected MavenSessionManager getCapturedSessionManager() throws Exception {
        Mockito.verify(actionFactory).buildUpdate(any(), any(), mavenSessionManager.capture(), any(), any());
        MavenSessionManager msm = mavenSessionManager.getValue();
        return msm;
    }

    @Override
    protected String[] getDefaultArguments() {
        return new String[] {CliConstants.Commands.UPDATE, CliConstants.DIR, installationDir.toString()};
    }

}
