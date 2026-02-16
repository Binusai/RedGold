public record InvestmentRequest(
    Long id,
    String createdDate,
    String remarks,
    List<InvestmentItemDto> items
) {}
