# Recordings: YouTube Videos of People Using Similar Apps

These videos show people using the tools our users rely on today: GMU's own
library search (the current process) and AI research assistants (competing
products). For each video we noted what the user does, where they struggle,
and the requirement it suggests.

> **Before you submit:** Watch each video and update the "Observed" column with
> timestamps (for example, `2:15 user retypes query with AND/OR`). The notes
> below come from the video titles, descriptions, and transcripts.

---

## 1. Current process: GMU (Mason) Libraries

These are official George Mason University Libraries videos, from the
[Mason Libraries YouTube channel](https://www.youtube.com/user/masonlibraries).

| # | Video | What the user does | Observed friction | Requirement it suggests |
|---|-------|--------------------|-------------------|-------------------------|
| R1 | [Doing a Simple Mason Catalog Search](https://www.youtube.com/watch?v=aQy6d6DYzYg) | Types keywords into Mason Search, then checks how to access a book or e-book. | Several steps after the search just to work out *how to get* the item. | Show access status (full text / PDF / request) right on each result. |
| R2 | [Doing an Advanced Mason Search](https://www.youtube.com/watch?v=-dcCbuE7jLs) | Uses the advanced form: fields, filters, and limits. | The user has to understand fields (title, subject, author) and build queries by hand. | Build advanced queries automatically from the thesis. |
| R3 | [Boolean and Database Searching @Mason](https://www.youtube.com/watch?v=uXup0ur-gr0) | Learns AND / OR / NOT to combine search terms in databases. | Boolean logic is a skill students have to learn first. | Generate Boolean strings for the user, and show them so the user can learn. |
| R4 | [Managing Mason Search Results](https://www.youtube.com/watch?v=iNlmS-lzA3Q) | Saves, organizes, and exports search results. | Results are saved as whole citations. Quotes still have to be pulled out by hand. | Save at the **quote** level, not just the article level. |
| R5 | [Accessing Full Text with Mason Catalog](https://www.youtube.com/watch?v=XtSlLiyTgoY) | Clicks through to reach the full text of online resources. | Several link hops (catalog, then the database, then the PDF). | Give one-click "Open full text via GMU" links. |
| R6 | [Introducing ProQuest Central](https://www.youtube.com/watch?v=ildmQ9Oq5tw) | Tours one database's interface and features. | Every database has a different interface to learn. | One interface that searches across GMU databases. |
| R7 | [Forward Citation Mining with Google Scholar](https://www.youtube.com/watch?v=qvq6KXtXG70) | Follows "Cited by" links to find newer related papers. | A manual, repeated process to grow the source list. | Offer "Find more like this" and citation-chaining suggestions. |

## 2. Similar apps: AI research assistants

| # | Video | What the user does | Observed friction / what users like | Requirement it suggests |
|---|-------|--------------------|-------------------------------------|-------------------------|
| R8 | [How to Use Elicit AI, Literature Reviews + More: Beginner Tutorial](https://www.youtube.com/watch?v=rJJPS-EvNfk) | Types a research question and gets a summary of the top papers plus a results table. | Users like the plain-language question and the summary table. | Accept a natural-language thesis. Show results as a scannable table. |
| R9 | [Start Your Literature Review Here! Elicit Literature Review Tool Tutorial](https://www.youtube.com/watch?v=tSqA4fkAedc) | Uses Elicit as the starting point of a literature review. | It searches Semantic Scholar, not the university's licensed databases. | Search GMU-licensed sources to get full text. |
| R10 | [Literature Review with Elicit AI Notebook](https://m.youtube.com/watch?v=RKelQ_6s95U) | Keeps searches organized in a notebook. | Users want to come back to earlier searches. | Save projects per thesis. |
| R11 | [Systematic Literature Review with Elicit: 4 Practical Use Cases + Limitations](https://www.youtube.com/watch?v=mFiy_fZwMfM) | Walks through use cases and the tool's **limitations**. | It misses papers, gives false positives, and has trouble with niche topics. | Show a relevance or support score, and let users mark results as irrelevant. |
| R12 | [An AI Search Engine Built for Academic Research: Consensus Walkthrough](https://www.youtube.com/watch?v=MQK0f_IYS8M) | Asks a question and reads summarized answers with citations. | Users like seeing study quality and agreement indicators. | Show peer-review status, year, and citation count. Tag each source as supporting or contradicting. |
| R13 | [Scite Overview: AI Tools for Researchers](https://www.youtube.com/watch?v=Dnqh4OidCFo) | Looks at "Smart Citations" that show whether later papers support or dispute a paper. | Users value seeing *how* a paper is cited. | Label each quote as supporting, contrasting, or background. |
| R14 | [How to Quickly Find Papers to Cite](https://www.youtube.com/watch?v=iZa0u2qF0nk) | Compares three tools for finding papers to cite. | The user's goal is **a citable source fast**, not reading everything. | Make "copy quote + citation" a single action. |
| R15 | [Using Google Scholar for research](https://www.youtube.com/watch?v=yl1o1wrP7sE) | Searches Scholar, then explains where else to search when Scholar isn't enough. | Students jump between several tools. | Bring the search into one place. |

---

## Common workflow across the videos

Every video shows the same manual chain:

```
think of keywords → pick a database → build a query (Boolean / filters)
→ skim titles & abstracts → find full text (several clicks) → read PDF
→ Ctrl+F for a supporting sentence → copy quote → format citation
```

Evidence Engine aims to shrink this chain to:

```
type thesis → review ranked quotes with citations → save / copy / export
```

See [`../images/current-workflow.png`](../images/current-workflow.png) for a
diagram of this flow.
