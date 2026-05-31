package io.github.mundanej.mlp.examples.nativecli;

import io.github.mundanej.mlp.generators.GeneratedLpInstance;
import io.github.mundanej.mlp.generators.NetworkFlowGenerator;
import io.github.mundanej.mlp.nativeapi.NativeApiStatus;
import io.github.mundanej.mlp.validation.ExpectedValidationResult;
import io.github.mundanej.mlp.validation.LpSolutionValidator;
import io.github.mundanej.mlp.validation.ToleranceProfile;
import io.github.mundanej.mlp.validation.ValidationEvidence;
import io.github.mundanej.mlp.validation.ValidationReport;
import io.github.mundanej.mlp.validation.ValidationStatus;
import java.util.Optional;

/** Native Image CLI smoke entrypoint. */
public final class NativeCliSmokeMain {
    private NativeCliSmokeMain() {
    }

    /** Runs a tiny generated LP through the validation engine. */
    public static void main(final String[] args) {
        GeneratedLpInstance instance = new NetworkFlowGenerator().threeNode(7L);
        double objective = instance.fixture().evidence().objectiveValue().orElseThrow();
        ValidationReport report = new LpSolutionValidator().validate(
                instance.fixture().problem(),
                instance.fixture().matrix(),
                new ExpectedValidationResult(Optional.of(ValidationStatus.OPTIMAL),
                        instance.fixture().evidence().objectiveValue()),
                ValidationEvidence.optimal(objective, instance.fixture().evidence().primal()),
                ToleranceProfile.STANDARD);
        if (!report.accepted()) {
            throw new IllegalStateException("native smoke validation failed: " + report.findings());
        }
        System.out.println("native cli smoke");
        System.out.println("nativeApi=" + NativeApiStatus.status());
        System.out.println("instance=" + instance.id());
        System.out.println("rows=" + instance.fixture().problem().stats().rows());
        System.out.println("columns=" + instance.fixture().problem().stats().columns());
        System.out.println("nonzeros=" + instance.fixture().matrix().nonzeros());
        System.out.println("objective=" + objective);
        System.out.println("validation=accepted");
    }
}
