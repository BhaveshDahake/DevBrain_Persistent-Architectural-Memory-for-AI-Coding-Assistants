# DevBrain Hackathon Demo Script
**Theme:** “Erase Last Night — Persistent Repository Memory”
**Duration:** ~3 Minutes (180 Seconds)
**Tone:** Professional, energetic, technical

---

### Part 1 — The Problem (0:00–0:40)

*(Screen: A generic AI chat interface. Presenter types a highly specific architectural question. The AI answers correctly. Presenter clicks "Refresh" or opens a new tab. Presenter types the exact same question again. The AI gives a generic, hallucinated, incorrect answer.)*

**Presenter:** 
"Every morning, millions of developers wake up to what we call the 'AI Hangover.' You spend hours teaching your AI coding assistant the intricacies of your custom architecture... and the very next day, it completely forgets. 

When you refresh the page, the context disappears. Your onboarding friction resets to zero. The AI hallucinates frameworks you don't even use. It’s a stateless engine trying to understand stateful code.

Today, we're fixing that. Meet **DevBrain**: the coding assistant with persistent repository memory."

---

### Part 2 — Upload + Graph Construction (0:40–1:20)

*(Screen: Switch to the sleek DevBrain React dashboard. Presenter drags and drops a repository `.zip` file into the upload zone. A progress bar animates, showing 'Cleaning', 'Extracting', and 'Mapping'.)*

**Presenter:** 
"It starts with our ingestion engine. When I upload this codebase, our Spring Boot orchestrator doesn't just read text files—it calls the `cognee.remember()` API to persist it. 

Behind the scenes, we automatically strip out garbage data like `node_modules` and compiled binaries. We take the raw, clean source code and pipe it into Cognee Cloud, where we construct a massive, semantic Knowledge Graph. 

Every file, every class, every method, and every dependency is mapped as a node. Your repository is no longer flat text; it's a living, structural entity."

---

### Part 3 — Chat + Live Graph Traversal (1:20–2:20)

*(Screen: Split view. Left side: Chat interface. Right side: Interactive D3 Force-Directed Graph visualizing thousands of nodes. Presenter types: "Where is authentication implemented?")*

**Presenter:** 
"Now, let's ask our newly minted memory using the `cognee.recall()` API: *'Where is authentication implemented?'*

*(Screen: The AI generates the answer in the chat panel. Simultaneously, on the right side, three specific nodes in the dense graph light up, pulse brightly, and the camera zooms in on them.)*

**Presenter:** 
"Look at that. Not only do we get a perfectly grounded answer routed through Spring AI, but the exact architectural nodes the LLM used to formulate its logic are highlighted live on our graph. Complete visibility into the AI's reasoning.

And because code evolves, our memory evolves. If this answer is perfect, I click this 'Helpful' thumbs-up button. This triggers `cognee.improve()` asynchronously, strengthening the weights of these relationships in our backend graph, continuously improving future retrieval."

---

### Part 4 — Forget Mechanism (2:20–3:00)

*(Screen: Presenter clicks the bright red "Reset Context" button in the dashboard header. A warning modal appears. Presenter clicks "Confirm". The graph vanishes. Presenter asks the identical auth question again. The AI responds: "I do not know based on the provided repository context.")*

**Presenter:** 
"But absolute memory can be dangerous if you're switching projects or migrating contexts. We believe developers should control memory—not be trapped by it. 

With one click of the 'Reset Context' button, our API triggers `cognee.forget()`. It obliterates local caches and permanently severs the remote knowledge graph. If I ask that same authentication question again... the system correctly admits it has no idea. Clean slate. No stale assumptions.

DevBrain isn't just an AI wrapper. It's a structured, resilient, self-healing architecture layer that ensures your assistant is just as smart tomorrow as it was today.

Thank you, judges. We are DevBrain."
