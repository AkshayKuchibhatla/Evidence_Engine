# Elicited Requirements

These requirements come from the [notes](notes/website-comments.md),
[recordings](recordings/youtube-videos.md), and [images](images/). The
**Source** column links each one back to its evidence.

## Users

- **Primary user:** a GMU undergraduate or graduate student writing an
  argumentative paper. They have a thesis and need scholarly quotes that
  support it.
- **Secondary users:** GMU librarians (helping students) and instructors
  (checking that sources are real).

## Functional requirements

| ID | Requirement | Priority | Source |
|----|-------------|----------|--------|
| FR1 | The user can type a thesis in plain language. Keywords and Boolean are not required. | Must | A1, A5, R2, R3, R8 |
| FR2 | The system turns the thesis into database queries and **shows the query**, which the user can edit. | Must | A1, R3 |
| FR3 | The system searches several GMU-licensed databases at once (e.g. ProQuest, JSTOR, EBSCO). | Must | A6, B5, R6, R9 |
| FR4 | The system pulls **word-for-word quotes** from full text, each with its **page number**. | Must | B2, B3, B8 |
| FR5 | Results are ranked by how directly the quote supports the thesis. | Must | B4, R11 |
| FR6 | Each quote is labeled **Supports / Contrasts / Background**. | Should | R12, R13, B7 |
| FR7 | Each result shows peer-review status, year, citation count, and database. | Must | B6, R12 |
| FR8 | One click copies the quote with a formatted citation (APA / MLA / Chicago). | Must | R14, R4 |
| FR9 | One click opens the full text through GMU access. | Must | A4, R1, R5 |
| FR10 | The user can save quotes into a project for each thesis and add notes. | Should | B9, R4, R10 |
| FR11 | The user can export saved quotes and citations (CSV, BibTeX, Zotero) for free. | Should | B1 |
| FR12 | Optional filters: peer-reviewed only, date range, discipline, database. | Should | R2 |
| FR13 | The user can mark a result "not relevant" to improve the ranking. | Could | R11, B5 |
| FR14 | "Find more like this" / citation chaining from a chosen source. | Could | R7 |
| FR15 | "Show context" shows the paragraphs around a quote. | Should | B3, B4 |

## Non-functional requirements

| ID | Requirement | Source |
|----|-------------|--------|
| NFR1 | **Usability:** a first-time user gets results without training, about as easily as with Google. | A2, A7, A8, A9 |
| NFR2 | **Performance:** first results within about 30 seconds, and a citable quote within 5 minutes end to end. | A3, `current-workflow.png` |
| NFR3 | **Trust:** the system never makes up or paraphrases a quote. Every quote can be traced to a source and page. | B3, B8 |
| NFR4 | **Access:** sign in with GMU NetID, and respect library license terms. | A4, B2 |
| NFR5 | **Cost:** free to GMU students. | B1, B10 |
| NFR6 | **Learning:** the generated queries and labels help students build their own research skills. | A7, R3 |

## Open questions for stakeholders

1. Which GMU databases allow programmatic or API access to full text under their license?
2. Will GMU Libraries let us use NetID single sign-on (via EZproxy / Shibboleth)?
3. Do instructors consider AI-ranked quotes acceptable under the academic integrity policy?
4. Should the tool show counter-evidence by default, or only when asked?
