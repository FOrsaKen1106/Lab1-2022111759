import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;



public class GraphGui extends JFrame {

  private Map<String, Map<String, Integer>> graph;
  private List<String> words;
  private JTextArea outputArea;
  private JTextField word1Field;
  private JTextField word2Field;
  private JTextField inputTextField;
  private File selectedFile = null;
  private JLabel statusLabel;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  /**
   * 初始化图形用户界面并配置所有UI组件.
   *
   * <p>该构造函数执行以下主要操作：
   * <ol>
   *   <li>初始化数据存储结构：创建邻接表({@link #graph})和单词列表({@link #words})</li>
   *   <li>配置主窗口属性：设置标题为"Graph Analysis Tool"，窗口尺寸800x600，居中显示，
   *       并定义关闭操作行为</li>
   *   <li>构建UI组件层级：
   *     <ul>
   *       <li>使用{@link BorderLayout}创建主面板，包含10像素边距</li>
   *       <li>创建7行垂直排列的按钮面板({@link GridLayout})，包含文件加载和所有功能按钮</li>
   *       <li>初始化带滚动条的文本输出区域，启用自动换行</li>
   *       <li>添加底部状态栏显示操作状态</li>
   *     </ul>
   *   </li>
   *   <li>配置事件监听：为每个功能按钮绑定对应的动作事件处理器</li>
   * </ol>
   *
   * <p>UI组件布局结构：
   * <pre>
   * +-------------------------------+
   * | [West]       | [Center]       |
   * | Button Panel | Output Area    |
   * |              |                |
   * +-------------------------------+
   * | [South] Status Label          |
   * +-------------------------------+</pre>
   *
   * @see #selectFile() 文件选择按钮绑定的方法
   * @see #showDirectedGraph(Map) 图形显示功能实现
   * @see JFrame#setDefaultCloseOperation(int) 窗口关闭行为配置
   */
  public GraphGui() {
    // 实例字段初始化
    this.graph = new HashMap<>();
    this.words = new ArrayList<>();

    // Set up the JFrame
    setTitle("Graph Analysis Tool");
    setSize(800, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    // Create main panel with BorderLayout
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
    // Create button panel
    JPanel buttonPanel = new JPanel(new GridLayout(7, 1, 0, 5));

    // Add file selection button
    JButton loadFileButton = new JButton("Load Text File");
    loadFileButton.addActionListener(e -> selectFile());
    buttonPanel.add(loadFileButton);

    // Add function buttons
    JButton showGraphButton = new JButton("Display Graph");
    showGraphButton.addActionListener(e -> showDirectedGraph(graph));
    buttonPanel.add(showGraphButton);

    JButton bridgeWordsButton = new JButton("Show Bridge Words");
    bridgeWordsButton.addActionListener(e -> showBridgeWordsGui());
    buttonPanel.add(bridgeWordsButton);

    JButton generateTextButton = new JButton("Generate New Text");
    generateTextButton.addActionListener(e -> generateNewTextGui());
    buttonPanel.add(generateTextButton);

    JButton shortestPathButton = new JButton("Calculate Shortest Path");
    shortestPathButton.addActionListener(e -> calculateShortestPathGui());
    buttonPanel.add(shortestPathButton);

    JButton pageRankButton = new JButton("Calculate PageRank");
    pageRankButton.addActionListener(e -> calculatePageRankGui());
    buttonPanel.add(pageRankButton);

    JButton randomWalkButton = new JButton("Random Walk");
    randomWalkButton.addActionListener(e -> performRandomWalk());
    buttonPanel.add(randomWalkButton);

    JButton visualizeButton = new JButton("Save Graph as Image");
    visualizeButton.addActionListener(e -> saveGraphImage());
    buttonPanel.add(visualizeButton);

    // Create output area
    outputArea = new JTextArea();
    outputArea.setEditable(false);
    outputArea.setLineWrap(true);
    outputArea.setWrapStyleWord(true);
    JScrollPane scrollPane = new JScrollPane(outputArea);

    // Create status label
    statusLabel = new JLabel("Status: Ready");
    statusLabel.setBorder(new EmptyBorder(5, 0, 0, 0));

    // Add components to main panel
    mainPanel.add(scrollPane, BorderLayout.CENTER);
    mainPanel.add(buttonPanel, BorderLayout.WEST);
    mainPanel.add(statusLabel, BorderLayout.SOUTH);

    // Add main panel to frame
    add(mainPanel);
  }

  private void selectFile() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Select Text File");
    fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      selectedFile = fileChooser.getSelectedFile();
      try {
        processTextFile(selectedFile.getAbsolutePath());
        statusLabel.setText("Status: File loaded - " + selectedFile.getName());
        outputArea.setText("File loaded successfully: " + selectedFile.getName() + "\n");
        outputArea.append("Total words processed: " + words.size());
      } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(),
            "File Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Status: Error loading file");
      }
    }
  }


  private void showBridgeWordsGui() {
    if (graph.isEmpty()) {
      showNoGraphError();
      return;
    }

    // Create dialog for input
    JDialog dialog = new JDialog(this, "Bridge Words", true);
    dialog.setLayout(new BorderLayout(10, 10));

    JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
    inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    inputPanel.add(new JLabel("Word 1:"));
    word1Field = new JTextField();
    inputPanel.add(word1Field);

    inputPanel.add(new JLabel("Word 2:"));
    word2Field = new JTextField();
    inputPanel.add(word2Field);

    JButton submitButton = new JButton("Find Bridge Words");
    submitButton.addActionListener(e -> {
      String word1 = word1Field.getText().trim();
      String word2 = word2Field.getText().trim();

      if (word1.isEmpty() || word2.isEmpty()) {
        JOptionPane.showMessageDialog(dialog, "Please enter both words",
            "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      String bridgeWords = showBridgeWords(word1, word2);
      if (bridgeWords.equals("err1")) {
        outputArea.setText("No \"" + word1 + "\" in the graph!");
      } else if (bridgeWords.equals("err2")) {
        outputArea.setText("No \"" + word2 + "\" in the graph!");
      } else if (bridgeWords.equals("err3")) {
        outputArea.setText("No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!");
      } else {
        outputArea.setText("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are:\n\n"
            + bridgeWords);
      }

      dialog.dispose();
    });

    dialog.add(inputPanel, BorderLayout.CENTER);
    dialog.add(submitButton, BorderLayout.SOUTH);
    dialog.setSize(300, 150);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

  private void generateNewTextGui() {
    if (graph.isEmpty()) {
      showNoGraphError();
      return;
    }

    // Create dialog for input
    JDialog dialog = new JDialog(this, "Generate Text with Bridge Words", true);
    dialog.setLayout(new BorderLayout(10, 10));

    JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
    inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    inputPanel.add(new JLabel("Input Text:"), BorderLayout.NORTH);
    inputTextField = new JTextField();
    inputPanel.add(inputTextField, BorderLayout.CENTER);

    JButton submitButton = new JButton("Generate Text");
    submitButton.addActionListener(e -> {
      String inputText = inputTextField.getText().trim();

      if (inputText.isEmpty()) {
        JOptionPane.showMessageDialog(dialog, "Please enter some text",
            "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      String newText = generateNewText(inputText);
      outputArea.setText("Original text:\n" + inputText + "\n\nGenerated text with bridge words:\n"
          + newText);
      statusLabel.setText("Status: New text generated");
      dialog.dispose();
    });

    dialog.add(inputPanel, BorderLayout.CENTER);
    dialog.add(submitButton, BorderLayout.SOUTH);
    dialog.setSize(400, 150);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

  private void calculateShortestPathGui() {
    if (graph.isEmpty()) {
      showNoGraphError();
      return;
    }

    // Create dialog for input
    JDialog dialog = new JDialog(this, "Calculate Shortest Path", true);
    dialog.setLayout(new BorderLayout(10, 10));

    JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
    inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    inputPanel.add(new JLabel("From word:"));
    word1Field = new JTextField();
    inputPanel.add(word1Field);

    inputPanel.add(new JLabel("To word (optional):"));
    word2Field = new JTextField();
    inputPanel.add(word2Field);

    JButton submitButton = new JButton("Calculate Path");
    submitButton.addActionListener(e -> {
      String word1 = word1Field.getText().trim();

      if (word1.isEmpty()) {
        JOptionPane.showMessageDialog(dialog, "Please enter at least the starting word",
            "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      String word2 = word2Field.getText().trim();
      // word2 may be empty, that's valid for our new functionality

      String result = calcShortestPath(word1, word2);
      outputArea.setText(result);
      statusLabel.setText("Status: Shortest path(s) calculated");
      dialog.dispose();
    });

    dialog.add(inputPanel, BorderLayout.CENTER);
    dialog.add(submitButton, BorderLayout.SOUTH);
    dialog.setSize(300, 150);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }


  private void calculatePageRankGui() {
    if (graph.isEmpty()) {
      showNoGraphError();
      return;
    }

    // Create dialog for input
    JDialog dialog = new JDialog(this, "Calculate PageRank", true);
    dialog.setLayout(new BorderLayout(10, 10));

    JPanel inputPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    inputPanel.add(new JLabel("Word:"));
    word1Field = new JTextField();
    inputPanel.add(word1Field);

    JButton submitButton = new JButton("Calculate PageRank");
    submitButton.addActionListener(e -> {
      String word = word1Field.getText().trim();

      if (word.isEmpty()) {
        JOptionPane.showMessageDialog(dialog, "Please enter a word",
            "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      Double prValue = calPageRank(word);
      outputArea.setText("PageRank of \"" + word + "\": " + String.format("%.6f", prValue));
      statusLabel.setText("Status: PageRank calculated");
      dialog.dispose();
    });

    dialog.add(inputPanel, BorderLayout.CENTER);
    dialog.add(submitButton, BorderLayout.SOUTH);
    dialog.setSize(300, 120);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

  @SuppressFBWarnings("PATH_TRAVERSAL_IN")
  private void performRandomWalk() {
    if (graph.isEmpty()) {
      showNoGraphError();
      return;
    }
    String result = randomWalk();
    outputArea.setText(result);
    statusLabel.setText("Status: Random walk completed");
    // 添加文件保存功能
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Save Random Walk Result");
    fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
    int userChoice = fileChooser.showSaveDialog(this);
    if (userChoice == JFileChooser.APPROVE_OPTION) {
      File fileToSave = fileChooser.getSelectedFile();

      // 确保文件扩展名为.txt
      String filePath = fileToSave.getAbsolutePath();
      if (!filePath.toLowerCase().endsWith(".txt")) {
        fileToSave = new File(filePath + ".txt");
      }

      // 修改后的代码段
      try (BufferedWriter writer = Files.newBufferedWriter(fileToSave.toPath(),
          StandardCharsets.UTF_8)) {
        writer.write(result);
        statusLabel.setText("Status: Random walk saved to " + fileToSave.getName());
      } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(),
            "Save Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Status: Error saving random walk");
      }
    }
  }

  private void saveGraphImage() {
    if (graph.isEmpty()) {
      showNoGraphError();
      return;
    }

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Save Graph Image");
    fileChooser.setFileFilter(new FileNameExtensionFilter("PNG images", "png"));

    int result = fileChooser.showSaveDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      String filePath = file.getAbsolutePath();

      // Ensure filename ends with .png
      if (!filePath.toLowerCase().endsWith(".png")) {
        filePath += ".png";
      }

      try {
        String imagePath = GraphVisualizer.visualizeDirectedGraph(
            graph,
            filePath.substring(0, filePath.lastIndexOf('.')),
            "png"
        );
        outputArea.setText("Graph image saved to: " + imagePath);
        statusLabel.setText("Status: Graph image saved");
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error saving graph image: " + e.getMessage(),
            "Save Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText("Status: Error saving graph image");
      }
    }
  }

  private void showNoGraphError() {
    JOptionPane.showMessageDialog(this, "Please load a text file first",
        "No Graph", JOptionPane.WARNING_MESSAGE);
  }

  /**
   * 处理文本文件，提取文本内容，去除换行符，保留字母.
   *
   * @param filePath 文件路径
   */
  @SuppressFBWarnings("PATH_TRAVERSAL_IN")
  public void processTextFile(String filePath) throws IOException {
    StringBuilder content = new StringBuilder();
    // 修改后的读取代码
    try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath),
        StandardCharsets.UTF_8)) {
      String line;
      while ((line = reader.readLine()) != null) {
        // 保留原始换行处理逻辑
        content.append(line).append(" ");
      }
    } catch (IOException e) {
      // 保持原有的异常处理逻辑
      JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(),
          "Read Error", JOptionPane.ERROR_MESSAGE);
      statusLabel.setText("Status: Error loading file");
    }

    // 处理文本：去除标点，只保留字母，转换为小写
    String processedText = content.toString().replaceAll("[^a-zA-Z]", " ").toLowerCase();
    String[] wordArray = processedText.split("\\s+");

    words = new ArrayList<>(Arrays.asList(wordArray));
    words.removeIf(String::isEmpty); // 移除空字符串

    buildDirectedWeightedGraph();
  }

  /**
   * 构建加权有向图.
   */
  public void buildDirectedWeightedGraph() {
    graph.clear();

    for (int i = 0; i < words.size() - 1; i++) {
      String current = words.get(i);
      String next = words.get(i + 1);

      // 更新或创建边及其权重
      graph.putIfAbsent(current, new HashMap<>());
      Map<String, Integer> edges = graph.get(current);
      edges.put(next, edges.getOrDefault(next, 0) + 1);
    }
  }

  /**
   * 要求函数1：展示有向图.
   *
   *
   * @param g 处理文件得到的有向图
   */
  private void showDirectedGraph(Map<String, Map<String, Integer>> g) {
    if (g.isEmpty()) {
      showNoGraphError();
      return;
    }

    outputArea.setText("");
    outputArea.append("Directed Weighted Graph Structure:\n\n");

    // Sort the entries alphabetically for better readability
    List<Map.Entry<String, Map<String, Integer>>> sortedEntries =
        new ArrayList<>(g.entrySet());
    Collections.sort(sortedEntries,
        Comparator.comparing(Map.Entry::getKey));

    for (Map.Entry<String, Map<String, Integer>> entry : sortedEntries) {
      outputArea.append(entry.getKey() + " -> ");
      List<String> edges = new ArrayList<>();

      // Sort edges alphabetically
      List<Map.Entry<String, Integer>> sortedEdges =
          new ArrayList<>(entry.getValue().entrySet());
      Collections.sort(sortedEdges,
          Comparator.comparing(Map.Entry::getKey));

      for (Map.Entry<String, Integer> edge : sortedEdges) {
        edges.add(edge.getKey() + "(" + edge.getValue() + ")");
      }
      outputArea.append(String.join(", ", edges) + "\n");
    }

    statusLabel.setText("Status: Graph displayed");
  }

  /**
   * 要求函数2：查询桥接词.
   *
   * @param word1 带查询的词语1
   * @param word2 带查询的词语2
   * @return bridgeWords 形如桥接词1, 桥接词2, ... 的桥接词列表
   */
  public String showBridgeWords(String word1, String word2) {
    word1 = word1.toLowerCase();  // 将单词转换为小写以统一比较
    word2 = word2.toLowerCase();

    // 创建一个包含所有唯一单词的集合，用于快速存在性检查
    // this.words 包含文件中的所有单词（可能有重复）
    // 使用HashSet可高效判断单词是否存在
    Set<String> allUniqueWordsInText = new HashSet<>(this.words);

    // 检查1：确认word1存在于文本中
    if (!allUniqueWordsInText.contains(word1)) {
      return "err1";  // "未找到词1 \"" + word1 + "\" 在图中！"
    }

    // 检查2：确认word2存在于文本中
    if (!allUniqueWordsInText.contains(word2)) {
      return "err2";  // "未找到词2 \"" + word2 + "\" 在图中！"
    }

    // 检查3：确认word1有出边（即存在后续连接词）
    // 若word1在图中但没有出边，则无法形成路径
    if (!graph.containsKey(word1)) {
      return "err3";  // "未找到从 \"" + word1 + "\" 到 \"" + word2 + "\" 的桥梁词！"
    }

    // 收集候选桥梁词
    Set<String> bridgeWords = new HashSet<>();
    // 获取word1的直接后续词（可能的桥梁词）
    Map<String, Integer> neighborsOfWord1 = graph.get(word1);

    // 遍历每个候选桥梁词
    for (String potentialBridge : neighborsOfWord1.keySet()) {
      // 检查候选词是否指向word2：
      // 1. 候选词必须存在于图中（即有出边）
      // 2. 其出边必须包含word2
      if (graph.containsKey(potentialBridge)) {
        Map<String, Integer> nextLevel = graph.get(potentialBridge);
        if (nextLevel.containsKey(word2)) {
          bridgeWords.add(potentialBridge);  // 确认符合条件的桥梁词
        }
      }
    }

    if (bridgeWords.isEmpty()) {
      return "err3";
    } else {
      return String.join(", ", bridgeWords);
    }
  }

  /**
   * 要求函数3：根据桥接词生成新文本.
   *
   * @param inputText 输入文本
   * @return 输入文本和输出文本拼接结果
   */
  public String generateNewText(String inputText) {
    String[] inputWords = inputText.toLowerCase().split("\\s+");
    List<String> result = new ArrayList<>();

    for (int i = 0; i < inputWords.length - 1; i++) {
      result.add(inputWords[i]);

      String word1 = inputWords[i];
      String word2 = inputWords[i + 1];

      if (graph.containsKey(word1) && graph.containsKey(word2)) {
        Set<String> bridgeWords = new HashSet<>();
        Map<String, Integer> neighbors1 = graph.get(word1);

        for (String neighbor : neighbors1.keySet()) {
          if (graph.containsKey(neighbor) && graph.get(neighbor).containsKey(word2)) {
            bridgeWords.add(neighbor);
          }
        }

        if (!bridgeWords.isEmpty()) {
          // 随机选择桥接词
          String[] bridges = bridgeWords.toArray(new String[0]);

          int randomIndex = SECURE_RANDOM.nextInt(bridges.length);
          String selected = bridges[randomIndex];

          result.add(selected);
        }
      }
    }

    result.add(inputWords[inputWords.length - 1]);
    return String.join(" ", result);
  }

  /**
   * 要求函数4：计算最短路径.
   *
   * @param word1 用于查找最短路径的词1
   * @param word2 用于查找最短路径的词2（如果为空，则计算word1到所有其他单词的最短路径）
   * @return 最短路径，可以直接用于输出
   */
  public String calcShortestPath(String word1, String word2) {

    // 1. 确定图中需要考虑的所有节点集合
    // 包括有边的节点和可能来自输入文本的孤立单词
    Set<String> allNodesInGraph = new HashSet<>();
    if (!graph.isEmpty()) {
      allNodesInGraph.addAll(graph.keySet()); // Nodes with outgoing edges
      for (Map<String, Integer> neighborMap : graph.values()) {
        allNodesInGraph.addAll(neighborMap.keySet()); // Nodes with incoming edges
      }
    }

    // 如果图(边结构)为空但文件中有加载单词
    // 将所有唯一单词视为(孤立的)节点
    if (allNodesInGraph.isEmpty() && !this.words.isEmpty()) {
      allNodesInGraph.addAll(new HashSet<>(this.words));
    }

    if (allNodesInGraph.isEmpty()) {
      return "The graph is effectively empty. No words to calculate paths for.";
    }

    // 2. 验证word1和word2是否在所有节点集合中
    if (!allNodesInGraph.contains(word1)) {
      return "Word \"" + word1 + "\" not in the graph!";
    }
    boolean findAllPaths = word2 == null || word2.trim().isEmpty();
    if (!findAllPaths && !allNodesInGraph.contains(word2)) {
      return "Word \"" + word2 + "\" not in the graph!";
    }

    // 3. 初始化Dijkstra算法数据结构
    Map<String, Integer> distances = new HashMap<>();           // 存储到每个节点的最短距离
    Map<String, List<String>> predecessors = new HashMap<>();   // 存储前驱节点
    for (String node : allNodesInGraph) {
      distances.put(node, Integer.MAX_VALUE);                   // 初始距离设为无穷大
      predecessors.put(node, new ArrayList<>());                // 初始化前驱列表
    }
    distances.put(word1, 0);    // 起点到自身的距离为0

    // 使用优先队列(按距离排序)来选择下一个处理的节点
    PriorityQueue<String> queue = new PriorityQueue<>(
        Comparator.comparingInt(node -> distances.getOrDefault(node, Integer.MAX_VALUE)));
    queue.add(word1);
    Set<String> processedNodes = new HashSet<>();

    // 4. 运行Dijkstra算法
    while (!queue.isEmpty()) {
      String current = queue.poll(); // 取出当前距离最小的节点
      if (processedNodes.contains(current)) {
        continue; // 跳过已处理的节点
      }
      processedNodes.add(current);
      if (!findAllPaths && current.equals(word2)) {
        break;  // 如果找到目标节点且不需要所有路径，提前退出
      }
      Integer currentDistance = distances.get(current);
      if (currentDistance == Integer.MAX_VALUE) {
        continue; // 不可达节点跳过
      }

      // 从图结构中获取当前节点的邻居(有出边的节点)
      Map<String, Integer> neighbors = graph.get(current);
      if (neighbors == null || neighbors.isEmpty()) {
        continue; // 当前节点没有出边则跳过
      }

      // 遍历所有邻居节点
      for (Map.Entry<String, Integer> neighborEntry : neighbors.entrySet()) {
        String neighbor = neighborEntry.getKey();
        Integer weight = neighborEntry.getValue();

        // 只考虑在我们定义的节点集合中的邻居
        if (!allNodesInGraph.contains(neighbor)) {
          continue;
        }

        int newDistToNeighbor = currentDistance + weight;   // 计算新距离
        Integer knownDistToNeighbor = distances.get(neighbor);

        // 如果找到更短的路径
        if (newDistToNeighbor < knownDistToNeighbor) {
          distances.put(neighbor, newDistToNeighbor);   // 更新距离
          predecessors.get(neighbor).clear();           // 清除旧前驱
          predecessors.get(neighbor).add(current);      // 添加新前驱
          queue.remove(neighbor);                       // 更新优先级(通过重新添加)
          queue.add(neighbor);
        } else if (newDistToNeighbor == knownDistToNeighbor
            && newDistToNeighbor != Integer.MAX_VALUE) {
          // 如果找到相同距离的路径
          // 添加额外前驱(多路径)
          predecessors.get(neighbor).add(current);
        }
      }
    }

    // 5. 构建并返回结果字符串
    StringBuilder resultBuilder = new StringBuilder();
    if (findAllPaths) {
      // 输出从word1到所有其他可达单词的最短路径
      resultBuilder.append("Shortest paths from \"").append(word1)
          .append("\" to all other reachable words:\n\n");
      List<Map.Entry<String, Integer>> sortedPaths = new ArrayList<>();

      // 收集所有可达路径(排除起点自身和不可达节点)
      for (Map.Entry<String, Integer> entry : distances.entrySet()) {
        if (!entry.getKey().equals(word1) && entry.getValue() != Integer.MAX_VALUE) {
          sortedPaths.add(entry);
        }
      }

      // 按距离和单词字母顺序排序
      sortedPaths.sort((e1, e2) -> {
        int distComp = e1.getValue().compareTo(e2.getValue());
        return (distComp != 0) ? distComp : e1.getKey().compareTo(e2.getKey());
      });
      if (sortedPaths.isEmpty()) {
        resultBuilder.append("No other words are reachable from \"").append(word1).append("\".\n");
      } else {
        // 输出每条路径的详细信息
        for (Map.Entry<String, Integer> entry : sortedPaths) {
          String target = entry.getKey();
          resultBuilder.append("To \"").append(target).append("\" (distance: ")
              .append(entry.getValue()).append("):\n");
          List<List<String>> allShortestPathsToTarget = getAllPaths(predecessors, word1, target);
          for (int i = 0; i < allShortestPathsToTarget.size(); i++) {
            resultBuilder.append("  Path ").append(i + 1).append(": ")
                .append(String.join(" -> ", allShortestPathsToTarget.get(i)))
                .append("\n");
          }
          resultBuilder.append("\n");
        }
      }
    } else {
      // 输出到特定word2的路径
      Integer targetDistance = distances.get(word2);
      if (targetDistance == Integer.MAX_VALUE) {
        return "No path from \"" + word1 + "\" to \"" + word2 + "\"!";
      }
      if (word1.equals(word2)) {
        // 处理起点和终点相同的情况
        resultBuilder.append("Shortest path from \"").append(word1).append("\" to itself:\n");
        resultBuilder.append("Distance: 0\n\nPath 1: ").append(word1).append("\n");
      } else {
        // 输出到word2的路径信息
        resultBuilder.append("Shortest path from \"").append(word1).append("\" to \"")
            .append(word2).append("\":\n");
        resultBuilder.append("Distance: ").append(targetDistance).append("\n\n");
        List<List<String>> allShortestPathsToTarget = getAllPaths(predecessors, word1, word2);
        // 输出所有最短路径
        for (int i = 0; i < allShortestPathsToTarget.size(); i++) {
          resultBuilder.append("Path ").append(i + 1).append(": ")
              .append(String.join(" -> ", allShortestPathsToTarget.get(i))).append("\n");
        }
      }
    }
    return resultBuilder.toString();
  }

  /**
   * 使用回溯法获取所有可能的最短路径.
   */
  private static List<List<String>> getAllPaths(Map<String, List<String>> predecessors,
                                                String start, String end) {
    List<List<String>> result = new ArrayList<>();

    // 使用DFS递归构建所有路径
    findAllPaths(predecessors, result, new ArrayList<>(), end, start);

    return result;
  }

  /**
   * 递归辅助方法 - 回溯法找出所有路径.
   */
  private static void findAllPaths(Map<String, List<String>> predecessors,
                                   List<List<String>> result,
                                   List<String> currentPath,
                                   String current,
                                   String start) {
    // 添加当前节点
    currentPath.add(0, current);

    // 基本情况：如果当前节点是起点
    if (current.equals(start)) {
      result.add(new ArrayList<>(currentPath));
    } else {
      // 获取前驱列表，确保不为null
      List<String> predecessorList = predecessors.get(current);
      if (predecessorList != null) {
        // 递归检查所有前驱节点
        for (String predecessor : predecessorList) {
          findAllPaths(predecessors, result, currentPath, predecessor, start);
        }
      }
    }

    // 回溯
    currentPath.remove(0);
  }

  /**
   * 要求函数5：计算单词的PageRank值，计算全部节点.
   *
   * @param word 要查询的单词
   * @return 该单词的PageRank值
   */
  public Double calPageRank(String word) {
    word = word.toLowerCase();

    // 首先收集图中所有节点，包括只作为目标节点的节点
    // 添加所有作为源节点的节点
    Set<String> allNodes = new HashSet<>(graph.keySet());

    // 添加所有作为目标节点的节点
    for (Map<String, Integer> edges : graph.values()) {
      allNodes.addAll(edges.keySet());
    }

    // 检查要查询的词是否存在于图中
    if (!allNodes.contains(word)) {
      return 0.0; // 单词不在图中
    }

    int numNodes = allNodes.size();

    // 参数设置
    final int maxIterations = 100; // 最大迭代次数
    final double tolerance = 1e-6; // 收敛阈值

    // 进行随机游走统计访问频次
    Map<String, Integer> visitCounts = new HashMap<>();

    List<String> nodes = new ArrayList<>(graph.keySet());
    String currentNode = nodes.get(SECURE_RANDOM.nextInt(nodes.size()));
    Map<String, Double> prValues = new HashMap<>();

    for (int i = 0; i < 10000; i++) {
      visitCounts.put(currentNode, visitCounts.getOrDefault(currentNode, 0) + 1);

      // 检查当前节点是否在图中，以及该节点是否有出边
      if (!graph.containsKey(currentNode) || graph.get(currentNode).isEmpty()) {
        // 如果当前节点不在图中或没有出边，随机选择一个新节点
        currentNode = nodes.get(SECURE_RANDOM.nextInt(nodes.size()));
        continue;
      }

      // 安全地获取邻居节点
      Map<String, Integer> currentNeighbors = graph.get(currentNode);
      List<String> neighbors = new ArrayList<>(currentNeighbors.keySet());

      if (neighbors.isEmpty()) {
        // 如果没有邻居，随机选择一个新节点
        currentNode = nodes.get(SECURE_RANDOM.nextInt(nodes.size()));
        continue;
      }

      currentNode = neighbors.get(SECURE_RANDOM.nextInt(neighbors.size()));
    }

    // 归一化为初始PR值
    double sumPre = visitCounts.values().stream().mapToInt(Integer::intValue).sum();
    for (String node : allNodes) {

      prValues.put(node, visitCounts.getOrDefault(node, 0) / sumPre);
    }

    // PageRank迭代计算
    for (int i = 0; i < maxIterations; i++) {
      Map<String, Double> newPrValues = new HashMap<>();

      // 初始化新的PR值
      for (String node : allNodes) {
        newPrValues.put(node, (1 - 0.85) / numNodes);
      }

      // 计算每个节点的PR值贡献
      for (String node : allNodes) {

        double pr = prValues.get(node);

        if (graph.containsKey(node) && !graph.get(node).isEmpty()) {
          // 有出边的节点
          Map<String, Integer> outEdges = graph.get(node);
          int totalWeight = outEdges.values().stream().mapToInt(Integer::intValue).sum();

          for (Map.Entry<String, Integer> edge : outEdges.entrySet()) {
            String target = edge.getKey();
            double weight = edge.getValue() / (double) totalWeight;
            newPrValues.put(target, newPrValues.get(target) + 0.85 * pr * weight);
          }
        } else {
          // 出度为0的节点或不在图中作为源节点的节点
          // 将PR值均匀分配给所有节点
          for (String target : allNodes) {
            newPrValues.put(target, newPrValues.get(target) + 0.85 * pr / numNodes);
          }
        }
      }

      // 检查是否收敛
      double diff = 0.0;
      for (String node : allNodes) {
        diff += Math.abs(newPrValues.get(node) - prValues.get(node));
      }

      // 更新PR值
      prValues = newPrValues;

      if (diff < tolerance) {
        break; // 收敛
      }
    }

    // 归一化PR值，确保总和为1
    double sum = prValues.values().stream().mapToDouble(Double::doubleValue).sum();
    if (sum > 0) {
      for (Map.Entry<String, Double> entry : prValues.entrySet()) {
        prValues.put(entry.getKey(), entry.getValue() / sum);
      }
    }

    return prValues.getOrDefault(word, 0.0);
  }



  /**
   * 要求函数6：随机游走.
   *
   * @return 随机游走路径信息
   */
  public String randomWalk() {
    if (graph.isEmpty()) {
      return "Graph is empty!";
    }

    List<String> nodes = new ArrayList<>(graph.keySet());
    String current = nodes.get(SECURE_RANDOM.nextInt(nodes.size()));
    Set<String> visitedEdges = new HashSet<>();
    List<String> path = new ArrayList<>();
    path.add(current);

    while (true) {
      Map<String, Integer> neighbors = graph.get(current);
      if (neighbors == null || neighbors.isEmpty()) {
        break;
      }

      // 根据权重选择下一个节点
      int totalWeight = neighbors.values().stream().mapToInt(Integer::intValue).sum();
      int randomValue = SECURE_RANDOM.nextInt(totalWeight);
      int cumulativeWeight = 0;
      String next = null;

      for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {
        cumulativeWeight += entry.getValue();
        if (randomValue < cumulativeWeight) {
          next = entry.getKey();
          break;
        }
      }

      if (next == null) {
        break;
      }

      String edge = current + "->" + next;


      path.add(next);
      current = next;

      if (visitedEdges.contains(edge)) {
        break;
      }
      visitedEdges.add(edge);


    }

    return "Random walk: " + String.join(" -> ", path);
  }

  /**
   * 启动应用程序入口点.
   *
   * <p>示例用法：
   * <pre>{@code
   * public static void main(String[] args) {
   *     // 确保GUI创建在事件分派线程中
   *     SwingUtilities.invokeLater(() -> {
   *         new GraphGUI().setVisible(true);
   *     });
   * }
   * }</pre>
   */
  public static void main(String[] args) {

    // Set Nimbus look and feel if available for better appearance
    // 修改后的外观设置代码
    try {
      for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
      // 精确捕获Swing可能抛出的异常类型
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        // 建议使用日志框架替代直接打印
        ex.printStackTrace();
      }
    }


    // Create and display the Gui
    SwingUtilities.invokeLater(() -> {
      GraphGui gui = new GraphGui();
      gui.setVisible(true);
    });
  }
}
