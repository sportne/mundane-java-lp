package io.github.mundanej.mlp.nativeapi;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class NativeApiStatusTest {
    @Test
    void statusMentionsPlaceholder() {
        assertTrue(NativeApiStatus.status().contains("placeholder"));
    }
}
