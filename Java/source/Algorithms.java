import java.util.*;
/** Implement these algorithms. docs/guide/CONTRACTS.md defines each field and limit. */
public final class Algorithms {
    /** Compare two versions of a sequence while preserving the order of shared characters. Implement lcs according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> lcs(Map<String,Object> input) {
        String a = (String) input.get("a");
        String b = (String) input.get("b");
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        StringBuilder seq = new StringBuilder();
        int i = n;
        int j = m;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                seq.append(a.charAt(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("length", (long) dp[n][m]);
        out.put("sequence", seq.reverse().toString());
        return out;
     }
    /** Plan the cheapest sequence of adjacent block merges when the original order must be preserved. Implement merge according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> merge(Map<String,Object> input) { 
        List<Object> raw = (List<Object>) input.get("sizes");
        int n = raw.size();
        long[] size = new long[n];
        for (int i = 0; i < n; i++)
            size[i] = ((Number) raw.get(i)).longValue();
        Map<String,Object> out = new LinkedHashMap<>();
        if (n <= 1) {
            out.put("cost", 0L);
            out.put("plan", new ArrayList<>());
            return out;
        }
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
            prefix[i + 1] = prefix[i] + size[i];
        long[][] dp = new long[n][n];
        int[][] splitAt = new int[n][n];
        for (int length = 2; length <= n; length++) {
            for (int l = 0; l + length <= n; l++) {
                int r = l + length - 1;
                dp[l][r] = Long.MAX_VALUE;
                for (int k = l; k < r; k++) {
                    long cost = dp[l][k] + dp[k + 1][r] + prefix[r + 1] - prefix[l];
                    if (cost < dp[l][r]) {
                        dp[l][r] = cost;
                        splitAt[l][r] = k;
                    }
                }
            }
        }
        List<List<Long>> plan = new ArrayList<>();
        addMergesInOrder(0, n - 1, splitAt, plan);
        out.put("cost", dp[0][n - 1]);
        out.put("plan", plan);
        return out;  
     }
    private static void addMergesInOrder(int l, int r, int[][] splitAt, List<List<Long>> plan) {
        if (l >= r)
            return;
        int k = splitAt[l][r];
        addMergesInOrder(l, k, splitAt, plan);
        addMergesInOrder(k + 1, r, splitAt, plan);
        plan.add(List.of((long) l, (long) k, (long) r));
    }
    /** Choose a maximum-value set when neighbouring vertices are incompatible and conflicts form a tree. Implement tree according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> tree(Map<String,Object> input) { 
        int n = ((Number) input.get("n")).intValue();
        Map<String,Object> out = new LinkedHashMap<>();
        if (n == 0) {
            out.put("weight", 0L);
            out.put("selected", new ArrayList<>());
            return out;
        }
        long[] weight = new long[n];
        List<Object> rawW = (List<Object>) input.get("weights");
        for (int i = 0; i < n; i++)
            weight[i] = ((Number) rawW.get(i)).longValue();
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++)
            adj.add(new ArrayList<>());
        for (Object eo : (List<Object>) input.get("edges")) {
            List<Object> e = (List<Object>) eo;
            int u = ((Number) e.get(0)).intValue();
            int v = ((Number) e.get(1)).intValue();
            adj.get(u).add(v);
            adj.get(v).add(u);
        }
        int[] parent = new int[n];
        Arrays.fill(parent, -1);
        parent[0] = -2;
        int[] postorder = new int[n];
        int count = 0;
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(0);
        while (!stack.isEmpty()) {
            int u = stack.pop();
            postorder[count++] = u;
            for (int v : adj.get(u)) {
                if (parent[v] == -1) {
                    parent[v] = u;
                    stack.push(v);
                }
            }
        }
        long[] skip = new long[n];
        long[] take = new long[n];
        for (int idx = n - 1; idx >= 0; idx--) {
            int u = postorder[idx];
            take[u] = weight[u];
            for (int v : adj.get(u)) {
                if (parent[v] == u) {
                    skip[u] += Math.max(skip[v], take[v]);
                    take[u] += skip[v];
                }
            }
        }
        boolean[] chosen = new boolean[n];
        boolean[] parentTaken = new boolean[n];
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(0);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            chosen[u] = !parentTaken[u] && take[u] >= skip[u];
            for (int v : adj.get(u)) {
                if (parent[v] == u) {
                    parentTaken[v] = chosen[u];
                    queue.add(v);
                }
            }
        }
        List<Long> selected = new ArrayList<>();
        long total = 0;
        for (int u = 0; u < n; u++) {
            if (chosen[u]) {
                selected.add((long) u);
                total += weight[u];
            }
        }
        out.put("weight", total);
        out.put("selected", selected);
        return out;
     }
    /** Establish a simple exact-search baseline against which the other two searches can be checked. Implement naive according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> naive(Map<String,Object> input) { 
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        int n = text.length();
        int m = pattern.length();
        List<Long> positions = new ArrayList<>();
        if (m == 0) {
            for (int i = 0; i <= n; i++)
                positions.add((long) i);
        } else {
            for (int start = 0; start + m <= n; start++) {
                boolean match = true;
                for (int j = 0; j < m; j++) {
                    if (text.charAt(start + j) != pattern.charAt(j)) {
                        match = false;
                        break;
                    }
                }
                if (match)
                    positions.add((long) start);
            }
        }
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("positions", positions);
        return out;
     }
    /** Reuse information about matched prefixes to avoid restarting the search after every mismatch. Implement kmp according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> kmp(Map<String,Object> input) { 
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        int n = text.length();
        int m = pattern.length();
        List<Long> positions = new ArrayList<>();
        if (m == 0) {
            for (int i = 0; i <= n; i++)
                positions.add((long) i);
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("positions", positions);
            return out;
        }
        int[] pi = new int[m];
        for (int i = 1; i < m; i++) {
            int j = pi[i - 1];
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j))
                j = pi[j - 1];
            if (pattern.charAt(i) == pattern.charAt(j))
                j++;
            pi[i] = j;
        }
        int matched = 0;
        for (int i = 0; i < n; i++) {
            while (matched > 0 && text.charAt(i) != pattern.charAt(matched)) {
                matched = pi[matched - 1];
            }
            if (text.charAt(i) == pattern.charAt(matched))
                matched++;
            if (matched == m) {
                positions.add((long) (i - m + 1));
                matched = pi[matched - 1];
            }
        }
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("positions", positions);
        return out;
     }
    /** Reject windows with unequal fingerprints and verify every equal-hash candidate. Implement rk according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> rk(Map<String,Object> input) { 
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        int n = text.length();
        int m = pattern.length();
        List<Long> positions = new ArrayList<>();
        if (m == 0) {
            for (int i = 0; i <= n; i++)
                positions.add((long) i);
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("positions", positions);
            return out;
        }
        if (m > n) {
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("positions", positions);
            return out;
        }
        final int BASE = 31;
        final int MOD = 101;
        long patternHash = 0;
        long windowHash = 0;
        long highPower = 1;
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * BASE + pattern.charAt(i)) % MOD;
            windowHash = (windowHash * BASE + text.charAt(i)) % MOD;
            if (i < m - 1)
                highPower = (highPower * BASE) % MOD;
        }
        for (int start = 0; ; start++) {
            if (windowHash == patternHash && charsMatch(text, pattern, start)) {
                positions.add((long) start);
            }
            if (start + m >= n)
                break;
            long drop = (text.charAt(start) * highPower) % MOD;
            windowHash = ((windowHash - drop + MOD) % MOD * BASE + text.charAt(start + m)) % MOD;
        }
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("positions", positions);
        return out;
     }
     private static boolean charsMatch(String text, String pattern, int start) {
        for (int j = 0; j < pattern.length(); j++) {
            if (text.charAt(start + j) != pattern.charAt(j))
                return false;
        }
        return true;
     }
    /** Order the text suffixes and use adjacent common prefixes to find repeated substrings. Implement suffix according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> suffix(Map<String,Object> input) { 
        String text = (String) input.get("text");
        int n = text.length();
        Map<String,Object> out = new LinkedHashMap<>();
        if (n == 0) {
            out.put("sa", new ArrayList<>());
            out.put("lcp", new ArrayList<>());
            out.put("repeated", "");
            return out;
        }
        Integer[] sa = new Integer[n];
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = i;
            rank[i] = text.charAt(i);
        }
        for (int k = 1; k < n; k <<= 1) {
            final int step = k;
            final int[] curRank = rank;
            Arrays.sort(sa, (x, y) -> {
                if (curRank[x] != curRank[y])
                    return Integer.compare(curRank[x], curRank[y]);
                int rx = x + step < n ? curRank[x + step] : -1;
                int ry = y + step < n ? curRank[y + step] : -1;
                return Integer.compare(rx, ry);
            });
            int[] newRank = new int[n];
            newRank[sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                int x = sa[i - 1];
                int y = sa[i];
                int rx = x + step < n ? rank[x + step] : -1;
                int ry = y + step < n ? rank[y + step] : -1;
                boolean different = rank[x] != rank[y] || rx != ry;
                newRank[y] = newRank[x] + (different ? 1 : 0);
            }
            rank = newRank;
            if (rank[sa[n - 1]] == n - 1)
                break;
        }
        int[] lcp = new int[n];
        int[] inv = new int[n];
        for (int i = 0; i < n; i++)
            inv[sa[i]] = i;
        int h = 0;
        for (int i = 0; i < n; i++) {
            if (inv[i] > 0) {
                int j = sa[inv[i] - 1];
                while (i + h < n && j + h < n && text.charAt(i + h) == text.charAt(j + h))
                    h++;
                lcp[inv[i]] = h;
                if (h > 0)
                    h--;
            } else {
                h = 0;
            }
        }
        int bestLen = 0;
        int bestIdx = -1;
        for (int i = 0; i < n; i++) {
            if (lcp[i] > bestLen) {
                bestLen = lcp[i];
                bestIdx = i;
            }
        }
        String repeated = bestLen == 0 ? "" : text.substring(sa[bestIdx], sa[bestIdx] + bestLen);
        List<Long> saList = new ArrayList<>();
        List<Long> lcpList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            saList.add((long) sa[i]);
            lcpList.add((long) lcp[i]);
        }
        out.put("sa", saList);
        out.put("lcp", lcpList);
        out.put("repeated", repeated);
        return out;
     }
    /** Build one dictionary automaton and count its patterns in several independent texts. Implement aho according to docs/guide/CONTRACTS.md. */
    static Map<String,Object> aho(Map<String,Object> input) { 
        List<Object> rawPatterns = (List<Object>) input.get("patterns");
        List<Object> rawTexts = (List<Object>) input.get("texts");
        int patternCount = rawPatterns.size();
        List<int[]> goList = new ArrayList<>();
        List<List<Integer>> terminalIds = new ArrayList<>();
        goList.add(new int[95]);
        Arrays.fill(goList.get(0), -1);
        terminalIds.add(new ArrayList<>());
        for (int id = 0; id < patternCount; id++) {
            String pattern = (String) rawPatterns.get(id);
            int state = 0;
            for (char ch : pattern.toCharArray()) {
                int c = ch - 32;
                int next = goList.get(state)[c];
                if (next == -1) {
                    next = goList.size();
                    goList.get(state)[c] = next;
                    goList.add(new int[95]);
                    Arrays.fill(goList.get(next), -1);
                    terminalIds.add(new ArrayList<>());
                }
                state = next;
            }
            terminalIds.get(state).add(id);
        }
        int stateCount = goList.size();
        int[][] go = goList.toArray(new int[0][]);
        int[] fail = new int[stateCount];
        Deque<Integer> queue = new ArrayDeque<>();
        for (int c = 0; c < 95; c++) {
            if (go[0][c] != -1)
                queue.add(go[0][c]);
        }
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int c = 0; c < 95; c++) {
                int v = go[u][c];
                if (v == -1)
                    continue;
                int f = fail[u];
                while (f != 0 && go[f][c] == -1)
                    f = fail[f];
                fail[v] = (go[f][c] != -1 && go[f][c] != v) ? go[f][c] : 0;
                queue.add(v);
            }
        }
        List<List<Integer>> reverseFail = new ArrayList<>();
        for (int i = 0; i < stateCount; i++)
            reverseFail.add(new ArrayList<>());
        for (int v = 1; v < stateCount; v++)
            reverseFail.get(fail[v]).add(v);
        int[] topo = new int[stateCount];
        int topoSize = 0;
        Deque<Integer> bfs = new ArrayDeque<>();
        bfs.add(0);
        while (!bfs.isEmpty()) {
            int u = bfs.poll();
            topo[topoSize++] = u;
            for (int v : reverseFail.get(u))
                bfs.add(v);
        }
        List<Object> counts = new ArrayList<>();
        for (Object rawText : rawTexts) {
            String text = (String) rawText;
            long[] visits = new long[stateCount];
            int state = 0;
            for (char ch : text.toCharArray()) {
                int c = ch - 32;
                while (state != 0 && go[state][c] == -1)
                    state = fail[state];
                if (go[state][c] != -1)
                    state = go[state][c];
                visits[state]++;
            }
            for (int i = stateCount - 1; i >= 0; i--) {
                int u = topo[i];
                for (int v : reverseFail.get(u))
                    visits[u] += visits[v];
            }
            long[] row = new long[patternCount];
            for (int s = 0; s < stateCount; s++) {
                for (int id : terminalIds.get(s))
                    row[id] = visits[s];
            }
            List<Long> rowList = new ArrayList<>();
            for (long value : row)
                rowList.add(value);
            counts.add(rowList);
        }
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("counts", counts);
        return out;
     }
}
