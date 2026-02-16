public record InvestmentItemDto(
    String description,
    String packageSize,
    Double qty,
    Double rate,
    Double discount,
    Double total,
    String remarks
) {}
