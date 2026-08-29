# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```
I would refactor the database access layer to follow a more consistent approach.

Currently, the code uses different strategies: some entities use Panache Active Record style, while the Warehouse module uses a repository/port approach. This inconsistency makes the code harder to understand and maintain.

For a small application, the Active Record approach is simple and reduces boilerplate. However, for a larger application I would prefer the Repository pattern with interfaces in the domain layer and implementations in the infrastructure layer, as used by the Warehouse module.

This provides better separation of concerns, makes the domain logic independent of the persistence framework, and makes unit testing easier because database access can be mocked through the repository interface.

I would gradually refactor the Product and Store modules toward the same repository-based approach rather than changing everything at once.

```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```
The OpenAPI generated approach provides a contract-first API design. The API contract is defined in one place, and the generated interfaces/models help keep the implementation consistent with the specification. It is especially useful when multiple teams or clients depend on the API contract.

The main disadvantages are the additional build/code-generation complexity and the fact that developers have less direct control over the generated classes.

Manually coding the endpoints is simpler and easier to understand initially, but the API contract can drift from the actual implementation and there is more possibility of inconsistencies.

For this project, I would prefer the OpenAPI contract-first approach for public or externally consumed APIs, especially the Warehouse API. For smaller internal APIs, manually coded endpoints can be acceptable if the API is simple and well tested.

The most important point would be to choose one approach consistently for APIs with similar requirements rather than mixing approaches without a clear reason.

```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```
I would prioritize tests based on business risk and the importance of the functionality.

First, I would create unit tests for the domain use cases and business rules because they contain important validations such as duplicate business unit codes, location validation, warehouse capacity, stock validation, and warehouse replacement rules. These tests are fast and provide good coverage of the core business logic.

Next, I would add integration/API tests for the important REST endpoints to verify the complete flow between the API, application logic, and database. The existing Warehouse integration tests are useful for this purpose.

I would also test important transaction-related behavior, such as ensuring that Store changes are propagated to the legacy system only after a successful database transaction.

For test coverage over time, I would run the tests as part of the CI/CD pipeline and require new business rules and bug fixes to include corresponding tests. I would focus on meaningful coverage of business-critical paths rather than trying to achieve 100% code coverage.

```