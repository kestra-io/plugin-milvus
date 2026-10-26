package io.kestra.plugin.milvus;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Inlined equivalent of {@code org.testcontainers.milvus.MilvusContainer}. The official
 * {@code org.testcontainers:milvus} module has not been republished under the
 * {@code testcontainers-} prefix for the Testcontainers 2.0 line yet (still 1.21.4 max on
 * Maven Central as of this writing), so this reproduces its real behavior directly against
 * the published {@code org.testcontainers:testcontainers:2.0.5} core artifact that Kestra
 * core itself depends on.
 */
public class EmbeddedMilvusContainer extends GenericContainer<EmbeddedMilvusContainer> {

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("milvusdb/milvus");

    public EmbeddedMilvusContainer(String image) {
        this(DockerImageName.parse(image));
    }

    public EmbeddedMilvusContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
        withExposedPorts(9091, 19530);
        waitingFor(Wait.forHttp("/healthz").forPort(9091));
        withCommand("milvus", "run", "standalone");
        withCopyFileToContainer(
            MountableFile.forClasspathResource("testcontainers/embedEtcd.yaml"),
            "/milvus/configs/embedEtcd.yaml"
        );
        withEnv("COMMON_STORAGETYPE", "local");
        withEnv("ETCD_USE_EMBED", "true");
        withEnv("ETCD_DATA_DIR", "/var/lib/milvus/etcd");
        withEnv("ETCD_CONFIG_PATH", "/milvus/configs/embedEtcd.yaml");
    }

    public String getEndpoint() {
        return "http://" + getHost() + ":" + getMappedPort(19530);
    }
}
