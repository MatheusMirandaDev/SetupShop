package br.com.setupshop.shared.pagination;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageQueryTest {

  @Test
  void shouldCreatePageQueryWithValidValues() {
    PageQuery query = new PageQuery(0, 20);

    assertEquals(0, query.page());
    assertEquals(20, query.size());
  }

  @Test
  void shouldRejectNegativePage() {
    assertThrows(IllegalArgumentException.class, () -> new PageQuery(-1, 20));
  }

  @Test
  void shouldRejectPageSizeBelowMinimum() {
    assertThrows(IllegalArgumentException.class, () -> new PageQuery(0, 0));
  }

  @Test
  void shouldRejectPageSizeAboveMaximum() {
    assertThrows(IllegalArgumentException.class, () -> new PageQuery(0, 101));
  }
}
