import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GraphGuiWhiteTest {
  private GraphGui graphBuilder;

  @Before
  public void setUp() throws Exception {
    graphBuilder = new GraphGui();
    graphBuilder.processTextFile("C:\\Users\\29329\\Desktop\\lab1-2022111759\\test\\Easy Test.txtA");
  }

  // 测试路径1: word1不在图中
  @Test
  public void testShowBridgeWords_Word1NotInGraph() {
    String result = graphBuilder.showBridgeWords("apple", "data");
    assertEquals("err1", result);
  }
  // 测试路径2: word2不在图中
  @Test
  public void testShowBridgeWords_Word2NotInGraph() {
    String result = graphBuilder.showBridgeWords("scientist", "apple");
    assertEquals("err2", result);
  }
  // 测试路径3: word1无邻居，bridgeWords为空
  @Test
  public void testShowBridgeWords_NoNeighbors() {
    // 假设"again"在图中但没有出边
    String result = graphBuilder.showBridgeWords("again", "data");
    assertEquals("err3", result);
  }
  // 测试路径5: 循环执行但未找到桥接词
  @Test
  public void testShowBridgeWords_NoBridgeWordsFound() {
    String result = graphBuilder.showBridgeWords("scientist", "report");
    assertEquals("err3", result);
  }
  // 测试路径7: 存在单个桥接词
  @Test
  public void testShowBridgeWords_SingleBridgeWord() {
    String result = graphBuilder.showBridgeWords("scientist", "it");
    assertEquals("analyzed", result);
  }
}