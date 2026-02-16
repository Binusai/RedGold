@Entity
public class InvestmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private String packageSize;
    private Double qty;
    private Double rate;
    private Double discount;
    private Double total;
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investment_id")
    private Investment investment;

    // getters & setters
}
