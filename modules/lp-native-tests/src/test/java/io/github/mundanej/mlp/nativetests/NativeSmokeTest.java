package io.github.mundanej.mlp.nativetests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.mundanej.mlp.nativeapi.NativeApiStatus;
import org.junit.jupiter.api.Test;

final class NativeSmokeTest {
    @Test
    void nativeApiStatusIsPresent() {
        assertTrue(NativeApiStatus.status().contains("native"));
    }
}
