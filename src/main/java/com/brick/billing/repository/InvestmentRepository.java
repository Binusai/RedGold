public interface InvestmentRepository extends JpaRepository<Investment, Long> {

    @Query("""
        SELECT i FROM Investment i
        LEFT JOIN FETCH i.items
        ORDER BY i.createdDate DESC
    """)
    List<Investment> findAllWithItems();

    @Query("""
        SELECT i FROM Investment i
        LEFT JOIN FETCH i.items
        WHERE i.id = :id
    """)
    Optional<Investment> findByIdWithItems(Long id);
}
