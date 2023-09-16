package fr.krishenk.castel.dependencies;

import fr.krishenk.castel.dependencies.relocation.SimpleRelocation;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public enum Dependency {
     ASM("org.ow2.asm", "asm", "9.3", "EmM2m1ninJQ5GN4R1tYVLi7GCFzmPlcQUW+MZ9No5Lw="),
     ASM_COMMONS("org.ow2.asm", "asm-commons", "9.3", "o0fCRzLbKurRBrblmWoBWwaj74bnkKT3W2F2Hw0vfzk="),
     CAFFEINE("com{}github{}ben-manes{}caffeine", "caffeine", "2.9.2", "/wJFhkxtOMISmYG18O/IFGBX/kpVSXwjRa7e1GolE7k=", SimpleRelocation.of("caffeine", "com{}github{}benmanes{}caffeine")),
     BYTEBUDDY("net{}bytebuddy", "byte-buddy", "1.10.22", "+TGtxDkxd6+lJExHJXqDlV4n/gR8QJN4xu2gkPsHSoQ=", SimpleRelocation.of("bytebuddy", "net{}bytebuddy")),
//     MARIADB_DRIVER("org{}mariadb{}jdbc", "mariadb-java-client", "3.1.2", "quwa00jQMKZbJck8Zc2vRyv4tLazFLll5boTrsgbxiI=", SimpleRelocation.of("mariadb", "org{}mariadb{}jdbc")),
//     MYSQL_DRIVER("com{}mysql", "mysql-connector-j", "8.0.32", "UiMp/pJZgPAuXribWdInJF00VBX/DAiTKmjJdlwTrMU=", SimpleRelocation.of("mysql", "com{}mysql")),
//     POSTGRESQL_DRIVER("org{}postgresql", "postgresql", "42.5.4", "9I/LC2lZvItHhlf1e6Q8Gsykyt5qvKXxe/m8nDY8tm8=", SimpleRelocation.of("postgresql", "org{}postgresql")),
     H2_DRIVER_LEGACY("com.h2database", "h2", "1.4.199", "MSWhZ0O8a0z7thq7p4MgPx+2gjCqD9yXiY95b5ml1C4="),
     H2_DRIVER("com.h2database", "h2", "2.1.214", "1iPNwPYdIYz1SajQnxw5H/kQlhFrIuJHVHX85PvnK9A="),
//     SLF4J_SIMPLE("org.slf4j", "slf4j-simple", "2.0.6", "VH8GImz8vMoZhmnnSY9qbSpVqE3l3W60ZXnxH8QPxdg="),
//     SQLITE_DRIVER("org.xerial", "sqlite-jdbc", "3.41.0.0", "5eyxRDAweZMwg2WUqdyrVv97I/9UIoNDVGRIppYQo2w="),
//     MONGODB_DRIVER_CORE("org{}mongodb", "mongodb-driver-core", "4.9.0", "gcQH79wKxUrqh+NiXIaOr328HQTRmaAjPzV/PiVshuo=", SimpleRelocation.of("mongodb", "com{}mongodb"), SimpleRelocation.of("bson", "org{}bson")),
//     MONGODB_DRIVER_SYNC("org{}mongodb", "mongodb-driver-sync", "4.9.0", "iep4j1wNl4nxlPUULGsmwLXfVOqBXhO8KKJW9bUnSco=", SimpleRelocation.of("mongodb", "com{}mongodb"), SimpleRelocation.of("bson", "org{}bson")),
//     MONGODB_DRIVER_BSON("org{}mongodb", "bson", "4.9.0", "D0DW6T8+Ie24lvoBDltaLYDgE9kMxLX4Qp++rHGmEQ8=", SimpleRelocation.of("mongodb", "com{}mongodb"), SimpleRelocation.of("bson", "org{}bson")),
//     KOTLIN_STDLIB("org{}jetbrains{}" + HashedNames.access$000(), HashedNames.access$000() + "-stdlib", "1.8.10", "F+EHYTHNB8lYqUL/igh8+GWx7z3lhGPh5d1v11FUBrA=", SimpleRelocation.of(HashedNames.access$000(), HashedNames.access$000())),
//     HIKARI("com{}zaxxer", "HikariCP", "5.0.1", "JtSSOX5ndbQpZzeokZvwQEev5YJ/3SwItFV1lUNrOis=", SimpleRelocation.of("hikari", "com{}zaxxer{}hikari")),
     CONFIGURATE_CORE("org{}spongepowered", "configurate-core", "3.7.2", "XF2LzWLkSV0wyQRDt33I+gDlf3t2WzxH1h8JCZZgPp4=", SimpleRelocation.of("configurate", "ninja{}leaping{}configurate"));
    private String version;
    private byte[] checksum;
    private final List<SimpleRelocation> relocations;
    private static final String MAVEN_FORMAT = "%s/%s/%s/%s-%s.jar";
    private final String groupId;
    private final String artifactId;

    private Dependency(String groupId, String artifactId, String version, String checksum) {
        this(groupId, artifactId, version, checksum, new SimpleRelocation[0]);
    }

    private Dependency(String groupId, String artifactId, String version, String checksum, SimpleRelocation ... relocations) {
        this.groupId = Dependency.rewriteEscaping(groupId);
        this.artifactId = Dependency.rewriteEscaping(artifactId);
        this.version = version;
        this.checksum = Base64.getDecoder().decode(checksum);
        this.relocations = Arrays.asList(relocations);
    }

    public String getVersion() {
        return this.version;
    }

    private static String rewriteEscaping(String s) {
        return s.replace("{}", ".");
    }

    public String getGroupId() {
        return this.groupId;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public void setChecksum(String checksum) {
        this.checksum = Base64.getDecoder().decode(checksum);
    }

    public String getFileName(String classifier) {
        String name = this.name().toLowerCase(Locale.ENGLISH).replace('_', '-');
        String extra = classifier == null || classifier.isEmpty() ? "" : '-' + classifier;
        return name + '-' + this.version + extra + ".jar";
    }

    public void setVersion(String version) {
        this.version = version;
    }

    String getMavenRepoPath() {
        return String.format(MAVEN_FORMAT, this.groupId.replace(".", "/"), this.artifactId, this.version, this.artifactId, this.version);
    }

    public byte[] getChecksum() {
        return this.checksum;
    }

    public boolean checksumMatches(byte[] hash) {
        return Arrays.equals(this.checksum, hash);
    }

    public List<SimpleRelocation> getRelocations() {
        return this.relocations;
    }

    public static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class HashedNames {
        private static final String KOTLIN = new String(Base64.getDecoder().decode("a290bGlu"));

        private HashedNames() {
        }

        static /* synthetic */ String access$000() {
            return KOTLIN;
        }
    }
}
