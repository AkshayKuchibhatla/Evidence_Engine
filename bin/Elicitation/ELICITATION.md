# Requirements Elicitation: Evidence Engine

**Team:** Source Finder (CS 321, Section 010)
**Product:** Evidence Engine is an AI agent that takes a student's **thesis** and **one supporting reason**, searches GMU's scholarly databases, and returns **up to 10 sources with one quote each**, or a clear **"not enough evidence"** answer.

This page collects the artifacts we used to work out what our users do today, what frustrates them, and what Evidence Engine should look like. Each artifact ends with the requirement it suggests (→ **R#**). The requirements are listed together at the bottom.

---

## 1. Notes (comments from websites)

Paraphrased themes from public student discussions and product pages. Links go to the pages or searches the themes came from.

| # | Source | What users say (paraphrased) | Insight |
|---|--------|-----------------------------|---------|
| N1 | [r/college: "finding sources for essay"](https://www.reddit.com/r/college/search/?q=finding+sources+for+essay) | Finding sources takes longer than the writing. Students run many keyword searches and skim abstracts for hours. | Searching and reading is the bottleneck → **R1, R3** |
| N2 | [r/GradSchool: "can't find sources to support argument"](https://www.reddit.com/r/GradSchool/search/?q=can%27t+find+sources+to+support+argument) | Students find out late that the evidence for their claim doesn't exist, then have to rewrite the claim. | Report "insufficient evidence" early → **R4** |
| N3 | [r/college: "ChatGPT fake citations"](https://www.reddit.com/r/college/search/?q=chatgpt+fake+citations) | AI chatbots make up papers and quotes, so students no longer trust AI for citations. | Every source and quote must be real and verifiable → **R2, R5** |
| N4 | [GMU Libraries: Research help](https://library.gmu.edu/) | Licensed databases (JSTOR, EBSCO, ProQuest) require a GMU login and have a different interface for each one. | Use the student's GMU login session. Hide the differences between databases behind connectors → **R6** |
| N5 | [Elicit](https://elicit.com/), [Consensus](https://consensus.app/), [Scite](https://scite.ai/) product pages | Existing AI research tools answer *research questions*. None of them takes an essay's *thesis + reason* pair as input. | Our input model is the gap in the market → **R1** |

## 2. Recordings (YouTube videos of similar workflows)

| # | Video search | What we watched for |
|---|--------------|---------------------|
| V1 | [How to find scholarly sources using library databases](https://www.youtube.com/results?search_query=how+to+find+scholarly+sources+library+database) | The manual process today: build a keyword query → filter by "peer reviewed" → open each PDF → Ctrl+F for a quote. It takes several steps for every source. |
| V2 | [Elicit AI research assistant tutorial](https://www.youtube.com/results?search_query=elicit+ai+research+assistant+tutorial) | How a competitor lays out results: a table of papers with an extracted claim column. Users like seeing a summary or quote next to each paper. |
| V3 | [Consensus AI app walkthrough](https://www.youtube.com/results?search_query=consensus+ai+app+walkthrough) | A yes/no "consensus meter" tells users quickly whether the evidence supports a claim. This is close to our two-outcome result. |
| V4 | [Writing an argumentative essay: finding evidence for body paragraphs](https://www.youtube.com/results?search_query=argumentative+essay+finding+evidence+for+body+paragraphs) | Students work one body paragraph (one reason) at a time. That supports making **one reason per search** our unit of work. |

**What we saw across the videos:** users want (a) fewer steps per source, (b) the quote shown right next to its source, and (c) a quick verdict on whether their claim can be supported → **R3, R4, R7**.

## 3. Images (screenshots and figures)

Each screenshot listed below is saved in [`/Images/`](/Images/) with the name shown.

| # | File | What it shows | Requirement it suggests |
|---|------|---------------|-------------------------|
| I1 | `gmu_library_search.JPEG`: screenshot of the [GMU Libraries](https://library.gmu.edu/) search results page | Students currently face a long list of results with filters and no relevance to their thesis. | Ranked, filtered results (at most 10) → **R3** |
| I2 | `elicit_results_table.AVIF`: screenshot of [Elicit](https://elicit.com/) results | A row for each paper with an extracted text column | Layout for the results card: citation + quote + link → **R5, R7** |
| I3 | `consensus_meter.JPG`: screenshot of [Consensus](https://consensus.app/) | A summary showing whether evidence supports the claim | A banner that says "Sufficient / Not enough evidence" → **R4** |
| I4 | `evidence_engine_flow.png`: our "How It Works" figure (Deliverable 0) | UI → Backend → AI agent → Connectors → Databases → back to the UI | The architecture our requirements map onto |

---

## 4. Elicited Requirements (draft, to be refined into features and stories)

| ID | Requirement | From |
|----|-------------|------|
| R1 | The user can enter a **thesis** and **one supporting reason** as the only inputs to a search. | N1, N5, V4, I5 |
| R2 | The agent returns only sources that exist in GMU-accessible databases. No generated or made-up citations. | N3 |
| R3 | The agent returns **at most 10** sources, ranked by how well they support the reason. | N1, V1, I1 |
| R4 | If the evidence is insufficient, the system says so explicitly instead of returning weak sources. | N2, V3, I3 |
| R5 | Each source includes **exactly one verbatim quote** and a link to the original. | N3, I2 |
| R6 | The system uses the student's saved, encrypted GMU login session to reach licensed databases through connectors. | N4 |
| R7 | Each result shows the citation, the quote, and why the quote supports the reason. | V2, V3, I2 |
| R8 | The user can view the **history** of past searches and their results. | I5 |

**Target users:** GMU undergraduates in writing-heavy courses who already have a thesis and reasons but still need scholarly evidence, and who are short on time.
