-- Reference data: a fixed exchange-rate snapshot used for USD normalisation.
-- Lives in a migration (not the demo seeder) because every environment needs it:
-- an employee cannot be saved without a rate for their currency.
-- Rates are an illustrative snapshot, deliberately static (REQUIREMENTS.md §4).
INSERT INTO exchange_rate (currency_code, rate_to_usd, as_of) VALUES
    ('USD', 1.00000000, DATE '2026-01-01'),
    ('GBP', 1.27000000, DATE '2026-01-01'),
    ('EUR', 1.10000000, DATE '2026-01-01'),
    ('CAD', 0.73000000, DATE '2026-01-01'),
    ('AUD', 0.66000000, DATE '2026-01-01'),
    ('SGD', 0.74000000, DATE '2026-01-01'),
    ('JPY', 0.00670000, DATE '2026-01-01'),
    ('INR', 0.01200000, DATE '2026-01-01');
