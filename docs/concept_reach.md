# Calculating the walking distance of a Token on the BattleGrid

We are implementing a GameBoard for Dungeons and Dragons. Hence
we need to implement movement rules for Tokens on a Grid.

A token has a given Speed, provided in movement steps. We need
to generate a Grid<Integer> that counts the number of steps of
the fields that are in reach.

this is a pseudo code implementation of the algorithm:

```pseudo
# Build the graph
graph = {}
for r in range(rows):
    for c in range(cols):
        if is_blocked(r, c):
            continue

        neighbors = []
        for dr, dc in directions:
            nr, nc = r + dr, c + dc
            if 0 <= nr < rows and 0 <= nc < cols and not is_blocked(nr, nc):
                neighbors.append((nr, nc))

        graph[(r, c)] = neighbors

# BFS on this explicit graph
from collections import deque

queue = deque()
distance = {}
start = (sr, sc)

queue.append(start)
distance[start] = 0

while queue:
    node = queue.popleft()
    d = distance[node]
    if d == n:
        continue
    for neighbor in graph[node]:
        if neighbor not in distance:
            distance[neighbor] = d + 1
            queue.append(neighbor)


