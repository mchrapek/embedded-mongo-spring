/*
 * The MIT License
 *
 * Copyright 2020- Marek Chrapek <marek.chrapek@hotmail.com>
 * Copyright 2013-2014 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pl.mchrapek.spring.embedmongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.IFeatureAwareVersion;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.distribution.Versions;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.distribution.GenericVersion;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.embed.process.store.Downloader;
import de.flapdoodle.embed.process.store.IArtifactStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.mchrapek.spring.embedmongo.slf4j.Slf4jLevel;
import pl.mchrapek.spring.embedmongo.slf4j.Slf4jProgressListener;
import pl.mchrapek.spring.embedmongo.slf4j.Slf4jStreamProcessor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;

import static de.flapdoodle.embed.mongo.distribution.Version.Main.PRODUCTION;

public class EmbeddedMongoBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedMongoBuilder.class);

    private IFeatureAwareVersion version = PRODUCTION;
    private Integer port;
    private String bindIp = InetAddress.getLoopbackAddress().getHostAddress();


    public MongoClient build() throws IOException {
        LOG.info("Initializing embedded MongoDB instance");
        MongodStarter runtime = MongodStarter.getInstance(buildRuntimeConfig());
        MongodExecutable mongodExe = runtime.prepare(buildMongodConfig());

        LOG.info("Starting embedded MongoDB instance");
        mongodExe.start();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Collections.singletonList(new ServerAddress(bindIp, getPort()))))
                .build();

        return MongoClients.create(settings);
    }


    /**
     * The version of MongoDB to run. When no version is provided, then
     * {@link Version.Main#PRODUCTION PRODUCTION} is used by default.
     * The value must not be null.
     */
    public EmbeddedMongoBuilder version(Version version) {
        if (version == null) {
            throw new IllegalArgumentException("Version must not be null");
        }
        this.version = version;
        return this;
    }

    public EmbeddedMongoBuilder version(String version) {
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Version must not be null or empty");
        }
        this.version = parseVersion(version);
        return this;
    }

    public EmbeddedMongoBuilder port(int port) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port number must be between 0 and 65535");
        }
        this.port = port;
        return this;
    }

    public EmbeddedMongoBuilder bindIp(String bindIp) {
        if (bindIp == null || bindIp.isEmpty()) {
            throw new IllegalArgumentException("BindIp must not be null or empty");
        }
        this.bindIp = bindIp;
        return this;
    }

    private int getPort() {
        if (port == null) {
            try {
                port = Network.getFreeServerPort();
            } catch (IOException ex) {
                LOG.error("Could not get free server port");
            }
        }
        return port;
    }

    private ProcessOutput buildOutputConfig() {
        Logger logger = LoggerFactory.getLogger(MongodProcess.class);

        return new ProcessOutput(
                new Slf4jStreamProcessor(logger, Slf4jLevel.TRACE),
                new Slf4jStreamProcessor(logger, Slf4jLevel.WARN),
                new Slf4jStreamProcessor(logger, Slf4jLevel.INFO));
    }

    private IRuntimeConfig buildRuntimeConfig() {
        return new RuntimeConfigBuilder()
                .defaults(Command.MongoD)
                .processOutput(buildOutputConfig())
                .artifactStore(buildArtifactStore())
                .build();
    }

    private IArtifactStore buildArtifactStore() {
        Logger logger = LoggerFactory.getLogger(Downloader.class);

        return new ExtractedArtifactStoreBuilder()
                .defaults(Command.MongoD)
                .download(new DownloadConfigBuilder()
                        .defaultsForCommand(Command.MongoD)
                        .progressListener(new Slf4jProgressListener(logger))
                        .build())
                .build();
    }

    private IMongodConfig buildMongodConfig() throws IOException {
        return new MongodConfigBuilder()
                .version(version)
                .net(new Net(bindIp, getPort(), Network.localhostIsIPv6()))
                .build();
    }

    private IFeatureAwareVersion parseVersion(String version) {
        String versionEnumName = version.toUpperCase().replaceAll("\\.", "_");
        if (!versionEnumName.startsWith("V")) {
            versionEnumName = "V" + versionEnumName;
        }
        try {
            return Version.valueOf(versionEnumName);
        } catch (IllegalArgumentException ex) {
            LOG.warn("Unrecognised MongoDB version '{}', this might be a new version that we don't yet know about. " +
                    "Attempting download anyway...", version);
            return Versions.withFeatures(new GenericVersion(version));
        }
    }
}
