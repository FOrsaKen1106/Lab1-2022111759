import pytest
from main import WordGraph

@pytest.fixture
def word_graph():
    wg = WordGraph()
    # Create a sample text file with known content for reproducible graph
    sample_text = """
    the quick brown fox jumps over the lazy dog.
    the fox jumps over the dog again.
    quick brown fox jumps.
    """
    with open("test.txt", "w", encoding="utf-8") as f:
        f.write(sample_text)
    wg.build_graph("test.txt")
    return wg

def test_both_words_absent(word_graph):
    """Test when neither word1 nor word2 exists in the graph."""
    result = word_graph.query_bridge_words("nonexistent1", "nonexistent2")
    expected = '图中不存在 "nonexistent1" 和 "nonexistent2"!'
    assert result == expected

def test_word1_absent(word_graph):
    """Test when word1 does not exist, but word2 exists."""
    result = word_graph.query_bridge_words("nonexistent", "quick")
    expected = '图中不存在 "nonexistent"!'
    assert result == expected

def test_word2_absent(word_graph):
    """Test when word1 exists, but word2 does not exist."""
    result = word_graph.query_bridge_words("quick", "nonexistent")
    expected = '图中不存在 "nonexistent"!'
    assert result == expected

def test_no_bridge_words(word_graph):
    """Test when both words exist, but no bridge words connect them."""
    result = word_graph.query_bridge_words("the", "fox")
    expected = '从 "the" 到 "fox" 无桥接词!'
    assert result == expected

def test_single_bridge_word(word_graph):
    """Test when exactly one bridge word exists."""
    result = word_graph.query_bridge_words("quick", "fox")
    expected = '从 "quick" 到 "fox" 的桥接词是: "brown"'
    assert result == expected

def test_multiple_bridge_words(word_graph):
    """Test when multiple bridge words exist."""
    # Add additional text to create multiple bridge words
    with open("test.txt", "a", encoding="utf-8") as f:
        f.write("quick red fox jumps.")
    word_graph.build_graph("test.txt")
    result = word_graph.query_bridge_words("quick", "fox")
    expected = '从 "quick" 到 "fox" 的桥接词为: "brown", "red"'
    assert result == expected

def test_empty_input():
    """Test boundary case with empty input strings."""
    wg = WordGraph()
    result = wg.query_bridge_words("", "")
    expected = '图中不存在 "" 和 ""!'
    assert result == expected

def test_case_insensitivity(word_graph):
    """Test case insensitivity of input words."""
    result = word_graph.query_bridge_words("QUICK", "FOX")
    expected = '从 "quick" 到 "fox" 的桥接词是: "brown"'
    assert result == expected

def test_single_edge_graph():
    """Test boundary case with a minimal graph (single edge)."""
    wg = WordGraph()
    with open("test_min.txt", "w", encoding="utf-8") as f:
        f.write("a b c")
    wg.build_graph("test_min.txt")
    result = wg.query_bridge_words("a", "c")
    expected = '从 "a" 到 "c" 的桥接词是: "b"'
    assert result == expected

def test_large_graph():
    """Test boundary case with a larger graph."""
    wg = WordGraph()
    with open("test_large.txt", "w", encoding="utf-8") as f:
        text = " ".join(["word" + str(i) for i in range(100)] + ["word0"])
        f.write(text)
    wg.build_graph("test_large.txt")
    result = wg.query_bridge_words("word0", "word2")
    expected = '从 "word0" 到 "word2" 的桥接词是: "word1"'
    assert result == expected