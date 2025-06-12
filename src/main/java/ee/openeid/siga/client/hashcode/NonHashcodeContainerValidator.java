package ee.openeid.siga.client.hashcode;

import ee.openeid.siga.client.exception.InvalidContainerException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static ee.openeid.siga.client.hashcode.HashcodeContainerWriter.ZIP_ENTRY_MIMETYPE;
import static ee.openeid.siga.client.hashcode.HashcodesDataFilesWriter.HASHCODES_PREFIX;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NonHashcodeContainerValidator {

    private static final String META_INF_DIRECTORY = "META-INF";

    public static void assertNonHashcodeContainer(byte[] container) {

        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(container))) {
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            if (zipEntry == null) {
                throw new InvalidContainerException("Invalid uploaded container");
            }

            do {
                if (!isValidEntry(zipEntry)) {
                    break;
                }
            } while ((zipEntry = zipInputStream.getNextEntry()) != null);
        } catch (IOException e) {
            throw new InvalidContainerException("Unable to open uploaded container");
        }
    }

    private static boolean isValidEntry(ZipEntry entry) {
        String entryName = entry.getName();

        if (entryName.startsWith(HASHCODES_PREFIX)) {
            throw new InvalidContainerException("Hashcode containers are not allowed");
        } else {
            return entryName.startsWith(META_INF_DIRECTORY) || entryName.equals(ZIP_ENTRY_MIMETYPE);
        }
    }
}
