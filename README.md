# Data Structures and Algorithms

> Data Structures & Algorithms to solidify language skills in Python and Java

## Layout

- `Core/Data-Structure/` — data structures implemented from scratch (array, stack, queue, ...).
- `Core/Algorithms/Neetcode/<category>/<problem>/` — problems grouped by topic (e.g. `1-Arrays-Hashing`, `2-Two-Pointers`). The numeric prefix is a personal prerequisite ordering, not a difficulty or roadmap ranking — gaps are intentional. A few problems (`GameOfLife`, `RemoveComments`) sit outside any category because none fits yet.
- `Core/Algorithms/PairProgramming/<problem>/` — larger system-design-style exercises (CircuitBreaker, CurrencyConverter, TokenManager), each a standalone Maven project.
- `Core/Algorithms/coding-framework.md` — the 6-step live-coding interview process used across these solutions.

## Running things

**Python** solutions are standalone scripts — run directly with `python3 <file>.py`.

**Java** files under `Neetcode/` are reference ports with no build file — they're meant to be opened in IntelliJ (or your editor of choice) and run/tested ad hoc; add JUnit5 as a project library if a `*Test.java` doesn't resolve.

**Java** projects under `PairProgramming/` are real Maven modules — from the problem's folder, run `mvn test`.
