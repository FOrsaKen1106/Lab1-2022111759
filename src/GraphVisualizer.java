import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 提供基于Graphviz的有向图可视化功能.
 *
 * <p>本类实现将图结构转换为DOT格式文件，并调用Graphviz命令行工具生成图像文件。
 * 支持多种输出格式包括PNG、PDF、SVG等。</p>
 *
 * <p><b>依赖要求：</b>
 * <ul>
 *   <li>需要系统已安装Graphviz工具集，并确保'dot'命令在PATH环境变量中</li>
 *   <li>推荐Graphviz 2.40+版本</li>
 * </ul>
 *
 * <p><b>典型使用流程：</b>
 * <ol>
 *   <li>调用{@link #visualizeDirectedGraph(Map, String, String)}主方法</li>
 *   <li>中间生成DOT描述文件（可保留用于调试）</li>
 *   <li>通过系统调用执行Graphviz渲染引擎</li>
 *   <li>返回生成图像文件的绝对路径</li>
 * </ol>
 *
 * @see <a href="https://graphviz.org/">Graphviz官方网站</a>
 */
public class GraphVisualizer {

  /**
   * 使用Graphviz生成有向图图形文件.
   *
   * @param graph 有向图结构
   * @param outputPath 输出文件路径(不含扩展名)
   * @param format 图像格式(png, pdf, svg等)
   * @return 生成的图像文件路径
   */
  public static String visualizeDirectedGraph(
      Map<String, Map<String, Integer>> graph,
      String outputPath,
      String format) throws IOException, InterruptedException {

    // 1. 生成DOT文件
    String dotFilePath = outputPath + ".dot";
    generateDotFile(graph, dotFilePath);

    // 2. 调用Graphviz生成图像
    String imageFilePath = outputPath + "." + format;
    generateImage(dotFilePath, imageFilePath, format);

    return imageFilePath;
  }

  @SuppressFBWarnings("PATH_TRAVERSAL_OUT")
  private static void generateDotFile(
      Map<String, Map<String, Integer>> graph,
      String dotFilePath
  ) throws IOException {

    try (BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(dotFilePath), StandardCharsets.UTF_8))) {
      writer.write("digraph G {\n");
      writer.write("  rankdir=LR;\n"); // 从左到右布局
      writer.write("  node [shape=circle];\n\n");
      // 添加所有节点
      for (String node : graph.keySet()) {
        writer.write("  \"" + node + "\";\n");
      }
      writer.write("\n");
      // 添加所有边
      for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
        String source = entry.getKey();
        for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
          String target = edge.getKey();
          int weight = edge.getValue();
          writer.write("  \"" + source + "\" -> \"" + target
              + "\" [label=\"" + weight + "\"];\n");
        }
      }
      writer.write("}\n");
    }
  }

  @SuppressFBWarnings({"COMMAND_INJECTION"})
  private static void generateImage(
      String dotFilePath,
      String outputImagePath,
      String format
  ) throws IOException, InterruptedException {

    // 检查Graphviz是否安装
    String graphvizPath = "dot"; // 默认在PATH中
    try {
      Process process = Runtime.getRuntime().exec(graphvizPath + " -V");
      process.waitFor();
    } catch (Exception e) {
      throw new IOException("Graphviz (dot) not found. Please install Graphviz first.");
    }

    // 执行dot命令生成图像
    String command = String.format("%s -T%s %s -o %s",
        graphvizPath, format, dotFilePath, outputImagePath);

    Process process = Runtime.getRuntime().exec(command);
    int exitCode = process.waitFor();

    if (exitCode != 0) {
      throw new IOException("Graphviz execution failed with exit code: " + exitCode);
    }
  }
}

