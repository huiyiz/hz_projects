import utils
from queue import PriorityQueue
import math
import heapq

def func():
	# a = PriorityQueue()
	# a.put("hello", 1)
	# a.put("world", 2)
	# print(a.pop())
	# print(a.pop())

	# a = HeapQueue()
	# a.heappush(1, 'a')
	# a.heappush(2, 'b')
	# print(a.heappop())
	h = []
	heapq.heappush(h, (5, 'write code'))
	heapq.heappush(h, (7, 'release product'))
	heapq.heappush(h, (3, 'release product'))

	print(heapq.heappop(h)[1])
	print(heapq.heappop(h)[1])
	print(heapq.heappop(h)[1])



	# prio_queue = PriorityQueue()
	# prio_queue.put((2, 'super blah'))
	# prio_queue.put((1, 'Some thing'))
	# prio_queue.put((1, 'This thing would come after Some Thing if we sorted by this text entry'))
	# prio_queue.put((5, 'blah'))
	# prio_queue.put((3, 'blah'))


	# while not prio_queue.empty():
	#     item = prio_queue.get()[1]
	#     print(item)

	# dist = []
	# for i in range(10):
	# 	dist.insert(i, math.inf)
	# dist.insert(2, 30)
	# print(dist)

func()



# dist = []
# prev = []
# depth = []
# def dijkstra(client):
# 	for i in client.graph.nodes:
# 		dist.insert(i, math.inf)
# 		prev.inset(i, None)
# 	dist.inest(client.home, 0)
# 	depth[client.home] = 0

# 	queue = PriorityQueue()
# 	queue.put(1, client.home)
# 	while !queue.empty():
# 		node = queue.get()[1]
# 		#assume that neighbor is a vertex, which is also a number
# 		for neighbor in client.graph.adjacency():
# 			queue.add(lient.graph.get_edge_data(node, neighbor), neighbor)
# 			if dist[neighbor] > dist[node] + client.graph.get_edge_data(node, neighbor):
# 				dist[neighbor] = dist[node] + client.graph.get_edge_data(node, neighbor)
# 				prev[neighbor] = node
# 				depth[neighbor] = depth[node] + 1

# 	print(dist)
# 	print(prev)
# 	print(depth)

