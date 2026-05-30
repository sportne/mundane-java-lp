package io.github.mundanej.mlp.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

final class ProjectArchitectureTest {
    private static final Path ROOT = Path.of(System.getProperty("user.dir")).getParent().getParent();

    @Test
    void onlyCliAdaptersUseProcessBuilder() throws IOException {
        List<Path> offenders = javaFiles()
                .filter(path -> contains(path, "ProcessBuilder"))
                .filter(path -> !path.toString().contains("lp-adapter-highs-cli"))
                .filter(path -> !path.toString().contains("lp-adapter-clp-cli"))
                .filter(path -> !path.toString().contains("lp-adapter-glpk-cli"))
                .filter(path -> !path.toString().contains("lp-architecture-tests"))
                .toList();
        assertTrue(offenders.isEmpty(), () -> "ProcessBuilder outside CLI adapters: " + offenders);
    }

    @Test
    void nativeTargetedCodeAvoidsReflectionTokens() throws IOException {
        List<String> forbidden = List.of("Class.forName", "java.lang.reflect", "Proxy.newProxyInstance", "Unsafe");
        List<Path> offenders = javaFiles()
                .filter(path -> isNativeTargeted(path))
                .filter(path -> forbidden.stream().anyMatch(token -> contains(path, token)))
                .toList();
        assertTrue(offenders.isEmpty(), () -> "Native-targeted forbidden token use: " + offenders);
    }

    @Test
    void publicMainApiDeclarationsHaveJavadocs() throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertTrue(compiler != null, "JDK compiler must be available for architecture tests");
        List<String> offenders = new ArrayList<>();
        List<Path> sources = mainJavaFiles().toList();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            JavacTask task = (JavacTask) compiler.getTask(
                    null,
                    fileManager,
                    null,
                    List.of("-proc:none"),
                    null,
                    fileManager.getJavaFileObjectsFromPaths(sources));
            DocTrees docTrees = DocTrees.instance(task);
            ApiJavadocScanner scanner = new ApiJavadocScanner(docTrees, offenders);
            for (CompilationUnitTree unit : task.parse()) {
                scanner.scan(unit, null);
            }
        }
        assertTrue(offenders.isEmpty(), () -> "Public API declarations without Javadoc: " + offenders);
    }

    private static boolean isNativeTargeted(final Path path) {
        String value = path.toString();
        return value.contains("lp-model")
                || value.contains("lp-sparse")
                || value.contains("lp-validation")
                || value.contains("lp-native-api");
    }

    private static Stream<Path> javaFiles() throws IOException {
        return Files.walk(ROOT.resolve("modules"))
                .filter(path -> path.toString().endsWith(".java"));
    }

    private static Stream<Path> mainJavaFiles() throws IOException {
        return Stream.concat(
                        Files.walk(ROOT.resolve("modules")),
                        Files.walk(ROOT.resolve("examples")))
                .filter(path -> path.toString().contains("/src/main/java/"))
                .filter(path -> path.toString().endsWith(".java"));
    }

    private static boolean contains(final Path path, final String token) {
        try {
            return Files.readString(path).contains(token);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static final class ApiJavadocScanner extends TreePathScanner<Void, Void> {
        private final DocTrees docTrees;
        private final List<String> offenders;
        private final Deque<ClassTree> classStack = new ArrayDeque<>();

        ApiJavadocScanner(final DocTrees docTrees, final List<String> offenders) {
            this.docTrees = docTrees;
            this.offenders = offenders;
        }

        @Override
        public Void visitClass(final ClassTree tree, final Void unused) {
            if (tree.getModifiers().getFlags().contains(Modifier.PUBLIC)) {
                DocCommentTree javadoc = requireJavadoc(tree, "public type " + tree.getSimpleName());
                requireRecordComponentTags(tree, javadoc);
            }
            classStack.push(tree);
            try {
                return super.visitClass(tree, unused);
            } finally {
                classStack.pop();
            }
        }

        @Override
        public Void visitMethod(final MethodTree tree, final Void unused) {
            if (isPublicApiMethod(tree)) {
                DocCommentTree javadoc = requireJavadoc(tree, "public method " + tree.getName());
                requireParameterTags(tree, javadoc);
            }
            return super.visitMethod(tree, unused);
        }

        @Override
        public Void visitVariable(final VariableTree tree, final Void unused) {
            if (isClassMember() && isPublicApiField(tree)) {
                requireJavadoc(tree, "public field " + tree.getName());
            }
            return super.visitVariable(tree, unused);
        }

        private DocCommentTree requireJavadoc(final Tree tree, final String description) {
            TreePath path = getCurrentPath();
            DocCommentTree javadoc = docTrees.getDocCommentTree(path);
            if (javadoc == null) {
                offenders.add(sourceLocation(path) + " " + description);
            }
            return javadoc;
        }

        private void requireParameterTags(final MethodTree tree, final DocCommentTree javadoc) {
            if (javadoc == null || tree.getParameters().isEmpty() || containsInheritDoc(javadoc)) {
                return;
            }
            Set<String> documentedParameters = documentedParameters(javadoc);
            for (VariableTree parameter : tree.getParameters()) {
                String parameterName = parameter.getName().toString();
                if (!documentedParameters.contains(parameterName)) {
                    offenders.add(sourceLocation(getCurrentPath()) + " missing @param " + parameterName);
                }
            }
        }

        private void requireRecordComponentTags(final ClassTree tree, final DocCommentTree javadoc) {
            if (javadoc == null || tree.getKind() != Tree.Kind.RECORD) {
                return;
            }
            Set<String> documentedParameters = documentedParameters(javadoc);
            for (Tree member : tree.getMembers()) {
                if (member instanceof VariableTree variable && isRecordComponent(variable)) {
                    String componentName = variable.getName().toString();
                    if (!documentedParameters.contains(componentName)) {
                        offenders.add(sourceLocation(getCurrentPath()) + " missing record @param " + componentName);
                    }
                }
            }
        }

        private Set<String> documentedParameters(final DocCommentTree javadoc) {
            return javadoc.getBlockTags().stream()
                    .filter(tag -> tag.getKind() == DocTree.Kind.PARAM)
                    .map(ParamTree.class::cast)
                    .filter(tag -> !tag.isTypeParameter())
                    .map(tag -> tag.getName().getName().toString())
                    .collect(java.util.stream.Collectors.toSet());
        }

        private boolean isRecordComponent(final VariableTree variable) {
            Set<Modifier> modifiers = variable.getModifiers().getFlags();
            return modifiers.contains(Modifier.PRIVATE)
                    && modifiers.contains(Modifier.FINAL)
                    && variable.getInitializer() == null;
        }

        private boolean isPublicApiMethod(final MethodTree tree) {
            if (tree.getName().contentEquals("main")) {
                return false;
            }
            Set<Modifier> modifiers = tree.getModifiers().getFlags();
            return modifiers.contains(Modifier.PUBLIC)
                    || (isInInterface() && !modifiers.contains(Modifier.PRIVATE));
        }

        private boolean isPublicApiField(final VariableTree tree) {
            ModifiersTree modifiers = tree.getModifiers();
            return modifiers.getFlags().contains(Modifier.PUBLIC) || isEnumConstant(tree);
        }

        private boolean isClassMember() {
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            return parent instanceof ClassTree;
        }

        private boolean isInInterface() {
            return !classStack.isEmpty() && classStack.peek().getKind() == Tree.Kind.INTERFACE;
        }

        private boolean isEnumConstant(final VariableTree tree) {
            return !classStack.isEmpty()
                    && classStack.peek().getKind() == Tree.Kind.ENUM
                    && tree.getInitializer() instanceof NewClassTree;
        }

        private boolean containsInheritDoc(final DocCommentTree javadoc) {
            return Stream.concat(javadoc.getFirstSentence().stream(), javadoc.getBody().stream())
                    .anyMatch(tree -> tree.getKind() == DocTree.Kind.INHERIT_DOC);
        }

        private String sourceLocation(final TreePath path) {
            CompilationUnitTree unit = path.getCompilationUnit();
            long position = docTrees.getSourcePositions().getStartPosition(unit, path.getLeaf());
            long line = unit.getLineMap().getLineNumber(position);
            return ROOT.relativize(Path.of(unit.getSourceFile().toUri())) + ":" + line;
        }
    }
}
