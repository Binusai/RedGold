@RestController
@RequestMapping("/api/investments")
@Transactional
public class InvestmentController {

    private final InvestmentRepository repo;

    public InvestmentController(InvestmentRepository repo) {
        this.repo = repo;
    }

    // SAVE OR UPDATE (same API)
    @PostMapping("/save")
    public Long save(@RequestBody InvestmentRequest req) {

        Investment inv = (req.id() != null)
                ? repo.findByIdWithItems(req.id()).orElse(new Investment())
                : new Investment();

        if (inv.getCreatedDate() == null) {
            inv.setCreatedDate(LocalDate.parse(req.createdDate()));
        }

        inv.setRemarks(req.remarks());

        inv.getItems().clear();

        double grandTotal = 0;

        for (InvestmentItemDto dto : req.items()) {

            if (dto.description() == null || dto.description().isBlank()) continue;

            InvestmentItem item = new InvestmentItem();
            item.setInvestment(inv);
            item.setDescription(dto.description());
            item.setPackageSize(dto.packageSize());
            item.setQty(dto.qty());
            item.setRate(dto.rate());
            item.setDiscount(dto.discount());
            item.setTotal(dto.total());
            item.setRemarks(dto.remarks());

            grandTotal += dto.total();
            inv.getItems().add(item);
        }

        inv.setGrandTotal(grandTotal);

        repo.save(inv);
        return inv.getId();
    }

    // LIST PAGE
    @GetMapping("/all")
    public List<InvestmentViewDto> list() {
        return repo.findAllWithItems().stream().map(i ->
            new InvestmentViewDto(
                i.getId(),
                i.getCreatedDate().toString(),
                i.getGrandTotal(),
                i.getRemarks(),
                i.getItems().stream().map(it ->
                    new InvestmentItemDto(
                        it.getDescription(),
                        it.getPackageSize(),
                        it.getQty(),
                        it.getRate(),
                        it.getDiscount(),
                        it.getTotal(),
                        it.getRemarks()
                    )
                ).toList()
            )
        ).toList();
    }

    // LOAD FOR EDIT
    @GetMapping("/{id}")
    public InvestmentViewDto get(@PathVariable Long id) {

        Investment i = repo.findByIdWithItems(id).orElseThrow();

        return new InvestmentViewDto(
            i.getId(),
            i.getCreatedDate().toString(),
            i.getGrandTotal(),
            i.getRemarks(),
            i.getItems().stream().map(it ->
                new InvestmentItemDto(
                    it.getDescription(),
                    it.getPackageSize(),
                    it.getQty(),
                    it.getRate(),
                    it.getDiscount(),
                    it.getTotal(),
                    it.getRemarks()
                )
            ).toList()
        );
    }
}
