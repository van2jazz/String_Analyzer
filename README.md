# String Analyzer API (Java / Spring Boot)

Analyzes strings and stores computed properties (length, palindrome, unique characters, word count, SHA-256 hash, character frequency map).

## Features / Endpoints

1. **Create/Analyze String**
   - `POST /strings`
   - Body: `{ "value": "string to analyze" }`
   - 201 Created: returns JSON with `id` (sha256), `value`, `properties`, `created_at`
   - 409: string already exists
   - 400/422 for invalid input

2. **Get Specific String**
   - `GET /strings/{string_value}`
   - 200: returns same object
   - 404: not found

3. **Get All Strings with Filtering**
   - `GET /strings?is_palindrome=true&min_length=5&max_length=20&word_count=2&contains_character=a`
   - 200: `{ data: [...], count: N, filters_applied: { ... } }`
   - 400: invalid params

4. **Natural Language Filtering**
   - `GET /strings/filter-by-natural-language?query=all%20single%20word%20palindromic%20strings`
   - 200: `{ data, count, interpreted_query }`
   - 400 / 422 on parse errors/conflicts

5. **Delete String**
   - `DELETE /strings/{string_value}` → 204 No Content

---

## Implementation notes

- Palindrome check is **case-insensitive** but preserves spaces/punctuation (i.e., `"A man"` is not considered a palindrome).
- `unique_characters` counts distinct code points (characters as code units).
- `character_frequency_map` keys are strings representing each character.
- Storage is in-memory (ConcurrentHashMap). For production, swap with a DB (e.g., Postgres) easily.

---

## Requirements

- Java 17+
- Maven

---

## Run locally

1. Build:
   ```bash
   mvn clean package -DskipTests=false

---

## Run locally

1. Build:
   ```bash
   mvn clean package -DskipTests=false
****
