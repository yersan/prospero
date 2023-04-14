/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.wildfly.prospero;

import org.jboss.galleon.ProvisioningException;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.wildfly.channel.InvalidChannelMetadataException;
import org.wildfly.prospero.api.exceptions.ArtifactPromoteException;
import org.wildfly.prospero.api.exceptions.ChannelDefinitionException;
import org.wildfly.prospero.api.exceptions.InvalidUpdateCandidateException;
import org.wildfly.prospero.api.exceptions.MetadataException;
import org.wildfly.prospero.api.exceptions.NoChannelException;
import org.wildfly.prospero.api.exceptions.ProvisioningRuntimeException;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

@MessageLogger(projectCode = "PRSP")
public interface ProsperoLogger extends BasicLogger {
    ProsperoLogger ROOT_LOGGER = Logger.getMessageLogger(ProsperoLogger.class, "org.wildfly.prospero");

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1, value = "Getting details of %s of %s")
    void historyDetails(String savedStateName, Path path);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 2, value = "Listing history of %s")
    void listHistory(Path path);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 3, value = "Reverting %s to %s")
    void revertStarted(Path installationPath, String savedStateName);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 4, value = "Reverted %s to %s")
    void revertCompleted(Path installationPath, String savedStateName);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 5, value = "Building revert candidate for %s")
    void revertCandidateStarted(Path installationPath);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 6, value = "Revert candidate generated in %s")
    void revertCandidateCompleted(Path installationPath);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 7, value = "Created temporary candidate folder %s")
    void temporaryCandidateFolder(Path tempPath);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 8, value = "Performing update of %s")
    void performUpdateStarted(Path installaPath);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 9, value = "Aborting update - no updates found for %s")
    void noUpdatesFound(Path installationPath);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 10, value = "Building update candidate for %s")
    void updateCandidateStarted(Path installationPath);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 11, value = "Update candidate generated in %s")
    void updateCandidateCompleted(Path installationPath);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 12, value = "Checking available updates")
    void checkingUpdates();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 13, value = "Found %d updates")
    void updatesFound(int count);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 14, value = "Adding channel %s")
    void addingChannel(String channel);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 15, value = "Channel %s already exists")
    void existingChannel(String channelName);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 16, value = "Channel %s added")
    void channelAdded(String channelName);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 17, value = "Removing channel %s")
    void removingChannel(String channelName);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 19, value = "Channel removed %s")
    void channelRemoved(String channelName);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 20, value = "Updating channel %s as %s")
    void updatingChannel(String channelDesc, String channelName);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 21, value = "Channel %s updated")
    void channelUpdated(String channelName);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 22, value = "Listing subscribed channels")
    void listChannels();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 23, value = "Starting phase: [%s] %s")
    void startedPhase(String name, String count);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 24, value = "Completed phase: [%s] %s")
    void completedPhase(String name, String count);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 25, value = "Applying %s candidate from %s")
    void applyingCandidate(String operationName, Path updateDir);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 26, value = "Changed artifacts [%s]")
    void candidateChanges(String changes);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 27, value = "No conflicts found.")
    void noCandidateConflicts();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 28, value = "File conflicts found: [%s]")
    void candidateConflicts(String conflicts);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 29, value = "%s candidate applied to %s")
    void candidateApplied(String operation, Path installationDir);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 30, value = "Starting provisioning into %s")
    void startingProvision(Path installDir);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 31, value = "Server provisioned into %s")
    void provisioningComplete(Path installDir);


    // 200+ - errors
    @Message(id = 200, value = "Aborting update - the running server marker is present (%s or %s)")
    ProvisioningException serverRunningError(Path standalone, Path domain);

    @Message(id = 201, value = "Given path '%s' is a regular file. An empty directory or a non-existing path must be given.")
    IllegalArgumentException dirMustBeDirectory(Path path);

    @Message(id = 202, value = "Invalid channel manifest definition")
    ChannelDefinitionException invalidManifest(@Cause InvalidChannelMetadataException e);

    @Message(id = 203, value = "Can't install into a non empty directory '%s'. Use `update` command if you want to modify existing installation.")
    IllegalArgumentException cannotInstallIntoNonEmptyDirectory(Path path);

    @Message(id = 204, value = "Installation dir '%s' doesn't exist")
    IllegalArgumentException installationDirDoesNotExist(Path path);

    @Message(id = 205, value = "Installation dir '%s' already exists")
    ProvisioningException installationDirAlreadyExists(Path installDir);

    @Message(id = 206, value = "Given configuration doesn't reference any channel or channel manifest.")
    NoChannelException noChannelReference();

    @Message(id = 207, value = "Invalid channel: Channel '%s' doesn't reference a manifest.")
    NoChannelException noChannelManifestReference(String name);

    @Message(id = 208, value = "Pre-defined FPL [%s] doesn't specify any channels and no explicit channels were given.")
    NoChannelException fplDefinitionDoesntContainChannel(String fpl);

    @Message(id = 209, value = "Channel '%s' is already present.")
    MetadataException channelExists(String channelName);

    @Message(id = 210, value = "Channel with name [%s] cannot be found.")
    MetadataException channelNotFound(String channelName);

    @Message(id = 211, value = "Channel name cannot be empty.")
    MetadataException emptyChannelName();

    @Message(id = 212, value = "Promoting artifacts to %s:")
    String promotingArtifacts(URL targetRepository);

    @Message(id = 213, value = "Unable to parse server configuration at '%s'")
    MetadataException unableToParseConfiguration(Path path, @Cause Throwable e);

    @Message(id = 214, value = "Unable to parse server configuration at '%s'")
    MetadataException unableToParseConfigurationUri(URI uri, @Cause Throwable e);

    @Message(id = 215, value = "Unable to read file at [%s]")
    MetadataException unableToReadFile(Path path, @Cause Exception e);

    @Message(id = 216, value = "The installation at %s is not a valid update for %s")
    InvalidUpdateCandidateException invalidUpdateCandidate(Path update, Path installation);

    @Message(id = 217, value = "Unable to save server configuration at '%s'")
    MetadataException unableToSaveConfiguration(Path path, @Cause Exception e);

    @Message(id = 218, value = "Unable to close the update store.")
    MetadataException unableToCloseStore(@Cause Exception e);

    @Message(id = 219, value = "Unable to create history store at [%s]")
    MetadataException unableToCreateHistoryStorage(Path path, @Cause Exception e);

    @Message(id = 220, value = "Provided metadata bundle [%s] is missing one or more entries")
    IllegalArgumentException incompleteMetadataBundle(Path path);

    @Message(id = 221, value = "Unable to write file at [%s]")
    MetadataException unableToWriteFile(Path path, @Cause Exception e);

    @Message(id = 222, value = "Path `%s` does not contain a server installation provisioned by prospero.")
    IllegalArgumentException invalidInstallationDir(Path path);

    @Message(id = 223, value = "Unable to access history store at [%s]")
    MetadataException unableToAccessHistoryStorage(Path path, @Cause Exception e);

    @Message(id = 224, value = "Unable to download file")
    MetadataException unableToDownloadFile(@Cause Exception e);

    // provisioning errors
    @Message(id = 225, value = "Unable to create temporary cache for provisioning cache folder.")
    ProvisioningException unableToCreateCache(@Cause Exception e);

    @Message(id = 226, value = "Failed to initiate maven repository system")
    ProvisioningRuntimeException failedToInitMaven(@Cause Throwable exception);

    @Message(id = 227, value = "Unable to find required stream definitions")
    String streamNotFound();

    @Message(id = 228, value = "Unable to resolve artifact")
    String unableToResolve();

    @Message(id = 229, value = "Invalid URL [%s]")
    IllegalArgumentException invalidUrl(String text, @Cause Exception e);

    @Message(id = 230, value = "Incomplete configuration: If the FPL is not one of predefined names (%s) a channel must be given.")
    IllegalArgumentException predefinedFplOrChannelRequired(String availableFpls);

    @Message(id = 231, value = "Incomplete configuration: neither FPL nor Galleon provisioning config was given.")
    IllegalArgumentException fplNorGalleonConfigWereSet();

    @Message(id = 232, value = "Found unexpected artifact [%s]")
    ProvisioningRuntimeException unexpectedArtifact(String gav);

    @Message(id = 233, value = "File already exists [%s]")
    IllegalArgumentException fileAlreadyExists(Path path);

    @Message(id = 234, value = "Promoting to non-file repositories is not currently supported")
    IllegalArgumentException unsupportedPromotionTarget();

    @Message(id = 235, value = "Wrong format of custom channel version [%s]")
    IllegalArgumentException wrongVersionFormat(String baseVersion);

    @Message(id = 236, value = "Custom channel version exceeded limit [%s]")
    IllegalArgumentException versionLimitExceeded(String baseVersion);

    @Message(id = 237, value = "Cannot create bundle without artifacts.")
    IllegalArgumentException noArtifactsToPackage();

    @Message(id = 238, value = "Channel reference has to use Maven GA.")
    IllegalArgumentException nonMavenChannelRef();

    @Message(id = 239, value = "Unable to promote artifacts to [%s].")
    ArtifactPromoteException unableToPromote(URL target, @Cause Exception e);

    @Message(id = 240, value = "Unable to parse the customization bundle [%s].")
    ArtifactPromoteException unableToParseCustomizationBundle(Path path, @Cause Exception e);

    @Message(id = 241, value = "At least one repository must be set when using the manifest option.")
    IllegalArgumentException repositoriesMustBeSetWithManifest();

    @Message(id = 242, value = "Out file [%s] exists already!")
    IllegalArgumentException outFileExists(Path outPath);

    @Message(id = 243, value = "Installation metadata is exported to [%s].")
    String installationExported(Path outPath);

    @Message(id = 244, value = "Malformed URL in substituted value : %s from %s")
    MetadataException invalidPropertySubstitutionValue(String substituted, String url);

    @Message(id = 245, value = "The requested state %s does not exist in server's history.")
    MetadataException savedStateNotFound(String name);

    @Message(id = 246, value = "Unable to create temporary directory")
    ProvisioningException unableToCreateTemporaryDirectory(@Cause Throwable t);

    @Message(id = 247, value = "Invalid channel definition")
    ChannelDefinitionException invalidChannel(@Cause InvalidChannelMetadataException e);

    @Message(id = 248, value = "Provided FPL has invalid format `%s`.")
    String invalidFpl(String fplText);
}
