public record InvestmentViewDto(
    Long id,
    String createdDate,
    Double grandTotal,
    String remarks,
    List<InvestmentItemDto> items
) {}
