package br.com.setupshop.shared.pagination;

public record PageQuery(int page, int size) {
  public PageQuery {
    if (page < 0) {
      throw new IllegalArgumentException("Page number cannot be negative");
    }
    if (size < 1 || size > 100) {
      throw new IllegalArgumentException("Page size must be between 1 and 100");
    }
  }
}
