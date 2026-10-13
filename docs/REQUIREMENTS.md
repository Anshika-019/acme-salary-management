# ACME Salary Management — Requirements

## 1. Goal
Replace the Excel-based workflow ACME's HR team uses to manage salary data for ~10,000 employees across multiple countries with a web application that lets an **HR Manager** (a) maintain employee salary records reliably and (b) answer questions about how the organization pays people, without exporting data into spreadsheets.

**Success looks like:** the HR Manager can find any employee in seconds, update a salary with validation, and answer questions such as *"What is the median salary of Software Engineers in India?"*, *"How does Engineering pay compare across countries?"* or *"Who is paid well outside the norm for their role?"* directly in the app.

## 2. Persona
**HR Manager**: comfortable with Excel, not technical. Cares about accuracy, speed of lookup, and being able to defend numbers to leadership. Today's pain: slow, error-prone spreadsheets with no validation and no consistent cross-country view.

## 3. In Scope

| # | Feature | Why it matters |
|---|---------|----------------|
| F1 | **Employee directory**: paginated, sortable table with search (name / employee ID) and filters (country, department, job title) | Core replacement for the spreadsheet; must stay fast at 10k rows (server-side pagination) |
| F2 | **Create / edit / delete employee** with validation (required fields, positive salary, valid country & currency) | Prevents the data-quality issues common in Excel |
| F3 | **Salary insights dashboard**: headcount, min, max, average and **median** salary grouped by country, department and job title | Directly answers "how does the org pay people?" Median is used because salary data is skewed and averages mislead |
| F4 | **Cross-country comparison in a common currency (USD)** using a stored exchange-rate table | Salaries are stored in local currency (source of truth) but cannot be compared without normalization |
| F5 | **Outlier view**: employees whose salary is far from the median for their job title + country | Surfaces potential pay-equity or data-entry issues, a high-value question HR can't easily answer in Excel |
| F6 | **Seed script** generating 10,000 realistic, deterministic employees | Realistic demo data; deterministic seed makes tests and demos reproducible |

## 4. Key Product & Data Decisions
- **Salary stored as annual gross in local currency** plus an ISO currency code. Conversion to USD happens only at read time for analytics, so the original number HR entered is never altered by rounding or FX changes.
- **Exchange rates are a static, versioned table** (seeded), not a live feed. Analytics must be reproducible, and a number shouldn't change between two views on the same day.
- **Aggregations run in the database** (SQL `GROUP BY`, `percentile_cont`), not in application memory, so insights stay fast as data grows.

## 5. Deliberately Out of Scope

| Left out | Reasoning |
|----------|-----------|
| Authentication & role-based access | Single persona in this exercise; the right solution (SSO/OIDC) depends on ACME's identity provider. **First item for a v2**, since salary data is highly sensitive. |
| Salary history / audit trail | Valuable, but it doubles the data-model complexity. Captured as the most important v2 feature after auth. |
| Payroll processing, tax, deductions | A different system with legal and compliance requirements per country; this tool is for *managing and analysing* salary data, not paying it. |
| Bonuses, equity, benefits | Keeps the "total compensation" question out of scope. Base salary covers the stated problem. |
| Bulk Excel/CSV import | The real migration path, but robust import needs dedupe, error reporting and preview UX. The seed script stands in for migration in this version. |
| Live FX rates | Adds an external dependency and makes analytics non-reproducible (see §4). |
| Approval workflows, notifications, multi-tenancy, i18n | No evidence they're needed for a single HR Manager. They would add complexity without serving the core goal. |

## 6. Non-Functional Requirements
- **Performance:** directory queries and insight aggregations respond in < 500 ms on 10k employees (indexed on country, department, job title).
- **Quality:** core business logic (validation, currency conversion, outlier detection, aggregation) covered by fast, deterministic unit tests, plus integration tests for the API.
- **Usability:** no training needed for an Excel user. Familiar table interactions and clear empty and error states.
