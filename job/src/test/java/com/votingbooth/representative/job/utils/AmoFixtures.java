package com.votingbooth.representative.job.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

// Real files from the AMO30 archive, in src/test/resources/amo/<entity>/<uid>.json.
public final class AmoFixtures {

    private AmoFixtures() {
    }

    public static byte[] bytes(String path) {
        try (InputStream in = AmoFixtures.class.getResourceAsStream("/amo/" + path)) {
            if (in == null) {
                throw new IllegalArgumentException("No fixture " + path);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
