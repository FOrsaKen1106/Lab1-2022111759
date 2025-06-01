import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class GraphGuiBlackTest {

  private GraphGui graphGui;
  private String testText = "The scientist carefully analyzed the data, wrote a detailed report, and shared the report with the team, but the team requested more data, so the scientist analyzed it again";

  @Before
  public void setUp() throws Exception {
    graphGui = new GraphGui();
    graphGui.processTextFile("C:\\Users\\29329\\Desktop\\lab1-2022111759\\test\\Easy Test.txt");
  }

  // 测试用例1：唯一最短路径
  @Test
  public void testUniqueShortestPath() {
    String result = graphGui.calcShortestPath("scientist", "analyzed");
    String expected = "Shortest path from \"scientist\" to \"analyzed\":\n"
        + "Distance: 1\n\n"
        + "Path 1: scientist -> analyzed\n";
    assertEquals(expected.trim(), result.trim());
  }

  // 测试用例2：直接边权重最低
  @Test
  public void testDirectEdgeWithMinWeight() {
    String result = graphGui.calcShortestPath("the", "team");
    assertTrue(result.contains("Distance: 2"));
    assertTrue(result.contains("Path 1: the -> team"));
  }

  // 测试用例3：计算所有可达路径
  @Test
  public void testAllReachablePaths() {
    String result = graphGui.calcShortestPath("the", "");


    // 验证关键路径存在
    assertTrue(result.contains("To \"data\" (distance: 1)"));
    assertTrue(result.contains("To \"team\" (distance: 2)"));
    assertTrue(result.contains("To \"scientist\" (distance: 2)"));
    assertTrue(result.contains("To \"analyzed\" (distance: 3)"));
  }

  // 测试用例4：无效输入(word1不存在)
  @Test
  public void testInvalidSourceWord() {
    String result = graphGui.calcShortestPath("x", "team");
    assertEquals("Word \"x\" not in the graph!", result.trim());
  }

  // 测试用例5：无效输入(word2不存在)
  @Test
  public void testInvalidTargetWord() {
    String result = graphGui.calcShortestPath("the", "x");
    assertEquals("Word \"x\" not in the graph!", result.trim());
  }

  // 测试用例6：不可达路径
  @Test
  public void testUnreachablePath() {
    String result = graphGui.calcShortestPath("again", "but");
    assertEquals("No path from \"again\" to \"but\"!", result.trim());
  }

  // 测试用例7：长路径验证
  @Test
  public void testLongComplexPath() {
    String result = graphGui.calcShortestPath("data", "again");
    assertTrue(result.contains("Distance: 7"));
    assertTrue(result.contains("Path 1: data -> so -> the -> scientist -> analyzed -> it -> again"));
  }

  // 测试用例8：双单词全部失效情况
  @Test
  public void testWeightAccumulationPath() {
    String result = graphGui.calcShortestPath("", "");

    assertTrue(result.contains("Word \"\" not in the graph!"));
  }
}