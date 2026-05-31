package io.github.mundanej.mlp.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
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
    List<Path> offenders =
        javaFiles()
            .filter(path -> contains(path, "ProcessBuilder"))
            .filter(path -> !path.toString().contains("lp-adapter-highs-cli"))
            .filter(path -> !path.toString().contains("lp-adapter-clp-cli"))
            .filter(path -> !path.toString().contains("lp-adapter-glpk-cli"))
            .filter(path -> !path.toString().contains("lp-architecture-tests"))
            .toList();
    assertTrue(offenders.isEmpty(), () -> "ProcessBuilder outside CLI adapters: " + offenders);
  }

  @Test
  void inProjectSolversDoNotUseExternalProcesses() throws IOException {
    List<String> forbidden = List.of("ProcessBuilder", "Runtime.getRuntime", "ProcessHandle");
    List<Path> offenders =
        javaFiles()
            .filter(path -> path.toString().contains("lp-solver-"))
            .filter(path -> !path.toString().contains("lp-solver-spi"))
            .filter(path -> !path.toString().contains("lp-architecture-tests"))
            .filter(path -> forbidden.stream().anyMatch(token -> contains(path, token)))
            .toList();
    assertTrue(offenders.isEmpty(), () -> "In-project solver external process use: " + offenders);
  }

  @Test
  void simpleSolverDependsOnlyOnSolverSpi() throws IOException {
    Path buildFile = ROOT.resolve("modules/lp-solver-simple/build.gradle");
    Set<String> dependencies = productionProjectDependencies(buildFile);
    assertEquals(
        Set.of(":modules:lp-solver-spi"),
        dependencies,
        () -> "Unexpected simple solver project dependencies: " + dependencies);
  }

  @Test
  void nativeTargetedCodeAvoidsReflectionTokens() throws IOException {
    List<String> forbidden =
        List.of(
            "Class.forName",
            "ClassLoader",
            "URLClassLoader",
            "java.lang.reflect",
            "Proxy.newProxyInstance",
            "ServiceLoader",
            "MethodHandles.lookup");
    List<Path> offenders =
        nativeTargetedMainJavaFiles()
            .filter(path -> forbidden.stream().anyMatch(token -> contains(path, token)))
            .toList();
    assertTrue(offenders.isEmpty(), () -> "Native-targeted forbidden token use: " + offenders);
  }

  @Test
  void nativeTargetedCodeAvoidsSerializationTokens() throws IOException {
    List<String> forbidden =
        List.of(
            "java.io.Serializable",
            "Externalizable",
            "ObjectInputStream",
            "ObjectOutputStream",
            "readObject(",
            "writeObject(");
    List<Path> offenders =
        nativeTargetedMainJavaFiles()
            .filter(path -> forbidden.stream().anyMatch(token -> contains(path, token)))
            .toList();
    assertTrue(offenders.isEmpty(), () -> "Native-targeted serialization token use: " + offenders);
  }

  @Test
  void nativeTargetedCodeAvoidsUnsafeNativeAndInternalJdkApis() throws IOException {
    List<String> forbidden =
        List.of("sun.misc.Unsafe", "jdk.internal.", "System.loadLibrary", "System.load(", "JNIEnv");
    List<Path> tokenOffenders =
        nativeTargetedMainJavaFiles()
            .filter(path -> forbidden.stream().anyMatch(token -> contains(path, token)))
            .toList();
    List<String> nativeMethodOffenders = nativeMethodOffenders();
    assertTrue(
        tokenOffenders.isEmpty() && nativeMethodOffenders.isEmpty(),
        () ->
            "Native-targeted unsafe/native/internal API use: "
                + tokenOffenders
                + " "
                + nativeMethodOffenders);
  }

  @Test
  void nativeTargetedModulesDoNotShipNativeImageMetadataWorkarounds() throws IOException {
    List<Path> offenders =
        Files.walk(ROOT.resolve("modules"))
            .filter(ProjectArchitectureTest::isNativeTargeted)
            .filter(path -> path.toString().contains("/src/main/resources/META-INF/native-image"))
            .toList();
    assertTrue(
        offenders.isEmpty(),
        () -> "Native Image metadata workarounds require documentation: " + offenders);
  }

  @Test
  void publicMainApiDeclarationsHaveJavadocs() throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertTrue(compiler != null, "JDK compiler must be available for architecture tests");
    List<String> offenders = new ArrayList<>();
    List<Path> sources = mainJavaFiles().toList();
    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
      JavacTask task =
          (JavacTask)
              compiler.getTask(
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
    return sourceTreeFiles(ROOT.resolve("modules"), false);
  }

  private static Stream<Path> mainJavaFiles() throws IOException {
    return Stream.concat(
        sourceTreeFiles(ROOT.resolve("modules"), true),
        sourceTreeFiles(ROOT.resolve("examples"), true));
  }

  private static Stream<Path> nativeTargetedMainJavaFiles() throws IOException {
    return sourceTreeFiles(ROOT.resolve("modules"), true)
        .filter(ProjectArchitectureTest::isNativeTargeted);
  }

  private static Stream<Path> sourceTreeFiles(final Path root, final boolean mainOnly)
      throws IOException {
    if (!Files.exists(root)) {
      return Stream.empty();
    }
    List<Path> sourceRoots;
    try (Stream<Path> paths = Files.walk(root, 4)) {
      sourceRoots =
          paths
              .filter(Files::isDirectory)
              .filter(path -> isJavaSourceRoot(path, mainOnly))
              .toList();
    }
    return sourceRoots.stream().flatMap(ProjectArchitectureTest::walkJavaSourceRoot);
  }

  private static boolean isJavaSourceRoot(final Path path, final boolean mainOnly) {
    String normalized = path.toString().replace('\\', '/');
    if (mainOnly) {
      return normalized.endsWith("/src/main/java");
    }
    return normalized.endsWith("/src/main/java") || normalized.endsWith("/src/test/java");
  }

  private static Stream<Path> walkJavaSourceRoot(final Path sourceRoot) {
    try {
      return Files.walk(sourceRoot).filter(path -> path.toString().endsWith(".java"));
    } catch (IOException exception) {
      throw new IllegalStateException(exception);
    }
  }

  private static boolean contains(final Path path, final String token) {
    try {
      return Files.readString(path).contains(token);
    } catch (IOException exception) {
      throw new IllegalStateException(exception);
    }
  }

  private static Set<String> productionProjectDependencies(final Path buildFile)
      throws IOException {
    Set<String> dependencies = new HashSet<>();
    for (String line : Files.readAllLines(buildFile)) {
      String trimmed = line.trim();
      if (isProductionDependencyDeclaration(trimmed)) {
        int start = trimmed.indexOf("project('");
        int dependencyStart = start + "project('".length();
        int dependencyEnd = trimmed.indexOf("')", dependencyStart);
        if (dependencyEnd > dependencyStart) {
          dependencies.add(trimmed.substring(dependencyStart, dependencyEnd));
        }
      }
    }
    return dependencies;
  }

  private static boolean isProductionDependencyDeclaration(final String line) {
    return line.startsWith("api project('")
        || line.startsWith("implementation project('")
        || line.startsWith("compileOnly project('")
        || line.startsWith("runtimeOnly project('");
  }

  private static List<String> nativeMethodOffenders() throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertTrue(compiler != null, "JDK compiler must be available for architecture tests");
    List<String> offenders = new ArrayList<>();
    List<Path> sources = nativeTargetedMainJavaFiles().toList();
    try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
      JavacTask task =
          (JavacTask)
              compiler.getTask(
                  null,
                  fileManager,
                  null,
                  List.of("-proc:none"),
                  null,
                  fileManager.getJavaFileObjectsFromPaths(sources));
      NativeMethodScanner scanner = new NativeMethodScanner(offenders);
      for (CompilationUnitTree unit : task.parse()) {
        scanner.scan(unit, null);
      }
    }
    return offenders;
  }

  private static final class NativeMethodScanner extends TreePathScanner<Void, Void> {
    private final List<String> offenders;

    NativeMethodScanner(final List<String> offenders) {
      this.offenders = offenders;
    }

    @Override
    public Void visitMethod(final MethodTree tree, final Void unused) {
      if (tree.getModifiers().getFlags().contains(Modifier.NATIVE)) {
        offenders.add("native method " + tree.getName());
      }
      return super.visitMethod(tree, unused);
    }
  }

  private static final class ApiJavadocScanner extends TreePathScanner<Void, Void> {
    private final DocTrees docTrees;
    private final List<String> offenders;
    private final Deque<ClassTree> classStack = new ArrayDeque<>();
    private final Deque<Boolean> publicApiClassStack = new ArrayDeque<>();

    ApiJavadocScanner(final DocTrees docTrees, final List<String> offenders) {
      this.docTrees = docTrees;
      this.offenders = offenders;
    }

    @Override
    public Void visitClass(final ClassTree tree, final Void unused) {
      boolean publicApiType = isPublicApiType(tree);
      if (publicApiType) {
        DocCommentTree javadoc = requireJavadoc(tree, "public type " + tree.getSimpleName());
        requireRecordComponentTags(tree, javadoc);
      }
      classStack.push(tree);
      publicApiClassStack.push(publicApiType);
      try {
        return super.visitClass(tree, unused);
      } finally {
        publicApiClassStack.pop();
        classStack.pop();
      }
    }

    private boolean isPublicApiType(final ClassTree tree) {
      if (classStack.isEmpty()) {
        return tree.getModifiers().getFlags().contains(Modifier.PUBLIC);
      }
      return isInPublicApiType() && tree.getModifiers().getFlags().contains(Modifier.PUBLIC);
    }

    @Override
    public Void visitMethod(final MethodTree tree, final Void unused) {
      if (isInPublicApiType() && isPublicApiMethod(tree)) {
        DocCommentTree javadoc = requireJavadoc(tree, "public method " + tree.getName());
        requireParameterTags(tree, javadoc);
      }
      return super.visitMethod(tree, unused);
    }

    @Override
    public Void visitVariable(final VariableTree tree, final Void unused) {
      if (isInPublicApiType() && isClassMember() && isPublicApiField(tree)) {
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
            offenders.add(
                sourceLocation(getCurrentPath()) + " missing record @param " + componentName);
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

    private boolean isInPublicApiType() {
      return !publicApiClassStack.isEmpty() && publicApiClassStack.peek();
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
