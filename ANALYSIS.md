# Assignment 4 — analysis (complete in English)

## Environment and commands

- Language/runtime: **Java**
- OS/CPU/RAM: **Windows 10**
- Build/run/test commands:
  ```sh
  python tools/run.py --language Java --setup
  python tools/run.py --language Java --check
  ```
## Algorithm explanations

**lcs**
- **State/invariant.** `dp[i][j]` = length of the LCS of `a[0..i)` and `b[0..j)`. Index meaning: `i` counts characters consumed from `a`, `j` from `b`.
- **Transition/base.** Base `dp[0][j] = dp[i][0] = 0`. Match `a[i-1]==b[j-1]` -> `dp[i-1][j-1]+1`; else `max(dp[i-1][j], dp[i][j-1])`.
- **Order.** Outer `i`, inner `j`; the three predecessors `(i-1,j-1)`, `(i-1,j)`, `(i,j-1)` are always computed first.
- **Correctness.** Any optimal common subsequence either consumes `a[i-1]==b[j-1]` (diagonal move is optimal) or drops `a[i-1]`/`b[j-1]` (max move keeps an optimum).
- **Reconstruction/ties.** Walk back from `(n,m)`; on match append and step diagonally; on mismatch follow the larger neighbor (tie -> up; both are optimal so the choice is safe). Reverse the collected characters.
- **Time/memory.** `O(nm)` time and memory. Bound `|a|*|b|<=10⁶`. Container: `int[n+1][m+1]`.
- **Boundary test.** `a=""`, `b="abc"` must return `length=0, sequence=""`. Detects a missing base row/column or reconstruction reading `a[-1]`.

**merge**
- **State/invariant.** `dp[l][r]` = min cost to merge original blocks `l..r` into one block. `l`/`r` are closed original indices.
- **Transition/base.** `dp[i][i]=0`. For `l<r`, `dp[l][r] = min_{l<=k<r}( dp[l][k] + dp[k+1][r] + sum(l..r) )`. `sum(l..r)` from prefix sums in `O(1)`.
- **Order.** Increasing interval length, so `dp[l][k]` and `dp[k+1][r]` are ready.
- **Correctness.** Any optimal merge has a final split `k` joining the two halves; trying all `k` and taking the min is exactly the optimum.
- **Reconstruction/ties.** Postorder of the split tree: recurse `[l,k]`, then `[k+1,r]`, then append `[l,k,r]`. Chronological because child merges appear before the parent that consumes them; each merge acts on two adjacent blocks. Ties take the first `k` that strictly improves.
- **Time/memory.** `O(n³)` time, `O(n²)` memory (`long[n][n]` + `int[n][n]`). Costs are `long`; `[10⁹,10⁹,10⁹]` yields `5*10⁹` without overflow.
- **Boundary test.** `sizes=[2,3,4]` must give `cost=14` with merges of 5 then 9 (opposite order = 16). Catches a plan that emits parent before children or a wrong recurrence.

**tree**
- **State/invariant.** Rooted at 0. `take[u]` = best subtree weight when `u` is selected; `skip[u]` = best when not. `u` is a vertex id; parent map roots the tree.
- **Transition/base.** Leaf: `take[u]=w[u]`, `skip[u]=0`. For each child `v`: `take[u]=w[u]+Σ skip[v]`, `skip[u]=Σ max(skip[v], take[v])`.
- **Order.** Postorder from an iterative DFS; children resolved before parent. Iterative stack avoids overflow on 100 000-deep chains.
- **Correctness.** Any optimal independent set either contains `u` (children forbidden -> `take`) or not (children free -> `skip`). Max of the two at the root is the global optimum; root choice is irrelevant (undirected tree).
- **Reconstruction/ties.** BFS from root: take `u` iff `!parentTaken[u] && take[u] >= skip[u]`, propagate `parentTaken` to children. Empty set allowed -> all-negative returns 0, `[]`.
- **Time/memory.** `O(n)` time and memory; adjacency `List<List<Integer>>`.
- **Boundary test.** 3-chain `[-4,-1,-5]` must give `weight=0, selected=[]`. Catches a forced-take DP.

**naive**
- **State/invariant.** For each start `s ∈ [0, n-m]`, compare `text[s..s+m)` to `pattern` character by character.
- **Transition/base.** Record `s` on full match. Empty pattern -> every boundary `0..n`.
- **Order.** Left to right, independent starts, so overlaps counted.
- **Correctness.** Every window of length `m` is tested exactly once.
- **Reconstruction/ties.** N/A.
- **Time/memory.** `O(nm)` time; `O(1)` auxiliary; `O(z+1)` output.
- **Boundary test.** `text="ab"`, `pattern=""` must return `[0,1,2]`. Catches an `n-m` loop bound and missing empty-pattern branch.

**kmp**
- **State/invariant.** `pi[i]` = length of the longest proper prefix of `pattern[0..i]` that is also a suffix of `pattern[0..i]`. `matched` = number of characters matched so far.
- **Transition/base.** Build `pi` with `j=pi[i-1]` then fall back while mismatch. Matching: on mismatch `matched=pi[matched-1]`; on match `matched++`; on `matched==m` record `i-m+1` and reset `matched=pi[m-1]` (overlaps).
- **Order.** `pi` built left to right; text scanned once.
- **Correctness.** `pi` records the longest shift that keeps a valid prefix; after a mismatch the matched prefix is preserved and only impossible shifts are skipped.
- **Reconstruction/ties.** N/A. Post-match fallback is essential.
- **Time/memory.** `O(n+m)` time, `O(m)` auxiliary (`int[m]`).
- **Boundary test.** `abababa/aba` -> `[0,2,4]`. A reset to `j=0` instead of `pi[m-1]` loses 2 and 4.

**rk**
- **State/invariant.** Base 31, modulus 101, ASCII codes. `patternHash` and `windowHash` use the leftmost character at the highest power. `highPower = 31^(m-1) mod 101`.
- **Transition/base.** Empty pattern -> `0..n`; `m>n` -> `[]`. Rolling: `windowHash = ((windowHash - text[i]*highPower) mod 101)*31 + text[i+m]`, adding 101 before mod to normalize negatives.
- **Order.** One pass left to right.
- **Correctness.** Equal hash is a candidate only. `charsMatch` verifies before recording. So false positives cannot leak into the output.
- **Reconstruction/ties.** N/A.
- **Time/memory.** `O(n+m+c*m)` where `c` is the number of equal-hash windows; worst case `O(nm)`. `O(1)` auxiliary.
- **Boundary test.** `abdj/ab` -> `[0]`; `dj` hashes to 75 but is rejected by verification. Detects missing candidate verification.
- **Note on modulus.** Mod 101 is a collision laboratory; production hashes use large primes or 64-bit unsigned and random bases, and do not claim deterministic linear time.

**suffix**
- **State/invariant.** `sa` = suffix indices in lexicographic order; `rank[i]` = rank of suffix `i`.
- **Doubling.** Initial rank = character code. Each round sorts by `(rank[i], rank[i+k])` with `-1` past end; then compress pairs into new ranks. `k` doubles each round. Stop when `rank[sa[n-1]] = n-1`.
- **Kasai.** `inv[sa[i]]=i`. For each `i` in text order, LCP of `i` and `sa[inv[i]-1]`, reusing `h-1` from the previous position. `lcp[0]=0`.
- **Order.** Doubling `O(log n)` rounds; Kasai one pass.
- **Correctness.** The pair `(rank[i], rank[i+k])` encodes the first `2k` characters of suffix `i`; sorting by it orders suffixes by longer and longer prefixes until all ranks are distinct. Kasai's `h-1` reuse is valid because the predecessor of `i+1` extends the predecessor of `i`.
- **Reconstruction/ties.** `repeated` = `text.substring(sa[argmax], sa[argmax]+max(lcp))`; overlaps allowed. Ties in `max(lcp)` are all valid answers. No sentinel in output.
- **Time/memory.** Comparison sort per round `O(n log² n)`; Kasai `O(n)`; `O(n)` auxiliary and output. `Integer[]` for `sa` (autoboxed `Arrays.sort`), `int[]` for ranks.
- **Boundary test.** `banana` -> `sa=[5,3,1,0,4,2]`, `lcp=[0,1,3,0,0,2]`, `repeated="ana"`; `aaaa` -> `"aaa"`. Catches a missing `-1` past-end sentinel and a Kasai off-by-one.

**aho**
- **State/invariant.** Trie over 95 printable ASCII characters; `fail[u]` = longest proper suffix of the trie path at `u` that is also a trie state. `terminalIds[u]` = all pattern ids ending at `u` (duplicates share `u`).
- **Transition/base.** Build trie; BFS failure links (root children point to 0). Per text: walk the automaton, `visits[state]++` at every character; then reverse topo order of the reverse-failure tree - for each `u`, `visits[u] += Σ visits[v]` over `v` whose `fail[v]=u`. This sums every suffix pattern occurrence along `fail`.
- **Order.** BFS for failure links; reverse BFS order for propagation.
- **Correctness.** After propagation, `visits[u]` = number of text positions at which the prefix matching `u` ends or whose failure chain reaches `u` - exactly the suffix-pattern count. Duplicates get equal counts because they share `u`; state is reset per text.
- **Reconstruction/ties.** N/A. Output rows follow `texts` order, columns follow dictionary order.
- **Time/memory.** Build `O(L + 95*S + p)`; per text `O(|text| + S + p)`; `O(95*S + p)` auxiliary; `O(q*p)` output.
- **Boundary test.** `["he","she","he"]` in `"sheshe"` -> `[2,2,2]`. Missing suffix propagation loses `he`; collapsed duplicates give a wrong third column.

## Own tests


|Command|Input or fixture path|Expected result|Actual result / log|What it checks|
|---|---|---|---|---|
|lcs|`AGGTAB/GXTXAYB`|length 4|PASS|non-contiguous match|
|merge|`[10,20,30,40,50]`|cost 340, 4 merges|PASS|chronological plan|
|tree|5-node, weights `[3,2,5,10,7]`|optimal non-adjacent set|PASS|take/skip|
|naive|`mississippi/issi`|`[1,4]`|PASS|overlaps|
|kmp|`mississippi/issi`|`[1,4]`|PASS|matches naive|
|rk|`abracadabra/abra`|`[0,7]`|PASS|rolling window|
|rk|`abdj/ab`|`[0]`|PASS|known collision `dj` rejected|
|suffix|`mississippi`|`len(sa)=len(lcp)=11`, `repeated="issi"`|PASS|no sentinel|
|aho|7-pattern dict on `abccab`|`[2,1,1,1,1,1,0]`|PASS|suffix links|
|aho|same dict, text `babcaa`|fresh row, same columns|PASS|per-text reset|

## Search comparison

Use the same inputs for naive/KMP/RK. At three increasing sizes in the common domain (n, m≤3000), measure random and repetitive cases separately. Record generator/seed, n, m, pattern distribution and correctness verification first. At least three measured runs per algorithm/size/case; report raw times, unit and median. Exclude compilation; give the identical timing scope, warm-up/JIT policy and commands.

|Case|n|m|Seed/generator|Algorithm|Run 1|Run 2|Run 3|Median|Correct outputs checked|
|---|---:|---:|---|---|---:|---:|---:|---:|---|
|random|500|50|`Random(42)`|naive|0|0|0|0|yes|
|random|500|50|`Random(42)`|kmp|0|0|0|0|yes|
|random|500|50|`Random(42)`|rk|0|0|0|0|yes|
|random|1500|150|`Random(42)`|naive|0|0|1|0|yes|
|random|1500|150|`Random(42)`|kmp|0|0|0|0|yes|
|random|1500|150|`Random(42)`|rk|0|0|0|0|yes|
|random|3000|300|`Random(42)`|naive|0|8|0|0|yes|
|random|3000|300|`Random(42)`|kmp|0|0|0|0|yes|
|random|3000|300|`Random(42)`|rk|0|0|0|0|yes|
|repetitive|500|50|`"a"*n, "a"*m`|naive|2|1|0|1|yes|
|repetitive|500|50|`"a"*n, "a"*m`|kmp|0|0|0|0|yes|
|repetitive|500|50|`"a"*n, "a"*m`|rk|0|1|1|1|yes|
|repetitive|1500|150|`"a"*n, "a"*m`|naive|6|1|1|1|yes|
|repetitive|1500|150|`"a"*n, "a"*m`|kmp|0|0|0|0|yes|
|repetitive|1500|150|`"a"*n, "a"*m`|rk|10|1|2|2|yes|
|repetitive|3000|300|`"a"*n, "a"*m`|naive|2|3|3|3|yes|
|repetitive|3000|300|`"a"*n, "a"*m`|kmp|0|0|0|0|yes|
|repetitive|3000|300|`"a"*n, "a"*m`|rk|5|7|9|7|yes|

Explain observations without treating measured times as a proof of asymptotic complexity. Discuss how the deliberately small modulus affects RK; do not generalize it to every production hash implementation. Add the known collision case as a correctness test.

## Sources and AI assistance

AI was used for explaining the assignment, debugging the code, analysis.
