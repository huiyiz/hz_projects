import networkx as nx
import random
import numpy as np
import math
from queue import PriorityQueue

def solve(client):
	client.end()
	client.start()

	'''
	==============
	Prep work: All students scout all vertices.
	Important memories:
		1. s_weight: a length=k list; weight of each student; index = student_id - 1; to be updated.
		2. opinion_matr: an v*k 2d list with (i, j) representing the student(j+1)'s opinion towards vertex_id = i+1
		3. student_wrong: a list of length k. Records number of wrong responses of student(i+1).
		4. student_pf: a list of length k. Records number of false positive of student(i+1).
	==============
	'''
	# prep work
	# all students scout all vertices
	all_students = list(range(1, client.students + 1))
	# s_weight: weight of each student; will be updated later
	s_weight = np.ones(client.students)
	# Call scout on all vertices by all students
	opinion_matr = []
	# CAUTION: Need to confirm whether nodes are sorted in client.G.nodes()
	for i in client.G.nodes():
	  # Opinion to one vertex from all students
		if i != client.home:
			dict_opinion = client.scout(i, all_students)
			lst_opinion = [False]*client.students
			for key in dict_opinion:
				lst_opinion[key-1] = dict_opinion[key]
			opinion_matr.append(lst_opinion)
		else:
			opinion_matr.append([0] * client.k)
	opinion_matr_fixed = opinion_matr.copy()
	student_wrong = [0]*client.students
	student_pf = [0]*client.students

	'''
	==============
	Dijkstra's Algorithm
	==============
	'''
	# # DIJKSTRA STARTS
	def dijkstra(client, graph):
		dist = [math.inf]*(client.v+1)
		prev = [None]*(client.v+1)
		depth = [-math.inf] + [math.inf]*(client.v)
		visited = [0]*(client.v+1)
		# for i in client.graph.nodes:
			# dist.insert(i, math.inf)
			# prev.insert(i, None)
		dist.insert(client.home, 0)
		depth[client.home] = 0
		prev[client.home] = client.home
		queue = PriorityQueue()
		for node in graph:
				queue.put((math.inf, node))
		queue.put((1, client.home))
		while not queue.empty():
			node = queue.get()[1]
			if visited[node] == 1:
				continue
			visited[node] = 1
		#assume that neighbor is a vertex, which is also a number
			for neighbor in graph.neighbors(node):
				if dist[neighbor] > dist[node] + graph.get_edge_data(node, neighbor).get('weight'):
					dist[neighbor] = dist[node] + graph.get_edge_data(node, neighbor).get('weight')
					prev[neighbor] = node
					depth[neighbor] = depth[node] + 1
					if (visited[neighbor] == 0):
						queue.put((dist[neighbor], neighbor))
		return dist, prev, depth
	dist, prev, depth = dijkstra(client, client.graph)
	# # DIJKSTRA ENDS
	'''
	==============
	All Pairs Shortest path
	1. Determine all pairs shortest path using Floyd-Warshall algorithm.
	2. Select potential vertices with the same logic.
	3. Remote to the closest neighbor to check if a bot is there & remember the result.
	4. Then send all 5 home by constructing MST.
	==============
	'''
	a = [0]*(client.v+1)
	b = [0]*(client.v+1)
	pairs_dist = np.array([a for _ in range(client.v+1)])
	next = np.array([b for _ in range(client.v+1)])
	for i in range(1, client.v+1):
		for j in client.graph.neighbors(i):
			pairs_dist[i, j] = client.graph.get_edge_data(i, j).get('weight')
			pairs_dist[j, i] = client.graph.get_edge_data(i, j).get('weight')
			next[i][j] = j
			next[j][i] = i
	# print(pairs_dist, next)
	assert pairs_dist[10][90] == pairs_dist[90][10]
	for k in range(1, client.v+1):
		for i in range(1, client.v+1):
			for j in range(1, client.v+1):
				if k!=i and k!=j and i!=j and pairs_dist[i][j] > pairs_dist[i][k]+pairs_dist[k][j]:
					pairs_dist[i][j] = pairs_dist[i][k]+pairs_dist[k][j]
					next[i][j] = next[i][k]

	def get_path(u, v, next=next):
		if next[u][v] == 0:
			return []
		else:
			path = [u]
			while u != v:
				u = next[u][v]
				path.append(u)
			return path
	def check_student(client, student_wrong):
		the_student = None
		for student in range(1, client.k+1):
			if student_wrong[student-1] >= client.v // 2:
				the_student = student
		return the_student

	'''
	==============
	Remote & Update weight:
	==============
	'''
	lost_kids_count = client.l
	found_bots = [0]*(client.v+1)
	home_kids = 0
	bot_prob = [0]*client.v
	failed_remote = 0
	success_remote = 0
	# Find all bots and meanwhile move those with great depth closer
	highest_prob_vertices_ind = []
	heavy_student = False
	remoted = []
	while lost_kids_count > 0:
		# bot_prob: average "yes" rate for each vertex; will be updated later once s_weight is updated
		bot_prob = [np.dot(s_weight, v_opinion) for v_opinion in opinion_matr]
		# print(['%.5f' % elem for elem in bot_prob])
		bot_prob[client.home - 1] = 0
		if lost_kids_count <= 0:
			break
		bot_prob_dic = {i+1:round(bot_prob[i], 5) for i in range(len(bot_prob))}
		# print("bot prob dictionary:", bot_prob_dic)
		# print("bot_prob: ",bot_prob)
		# Sort for L vertices with highest yes rate
		# The indices of the L bots with highest rate, sorted in decensding order
		new_highest_prob_vertices_ind = sorted(range(len(bot_prob)), key=lambda k: bot_prob[k], reverse = True)[0:1]
		print("chosen v to remote is:", new_highest_prob_vertices_ind[0]+1)
		max_prob_v = []
		max_prob = max(bot_prob)
		for i in range(len(bot_prob)):
			if bot_prob[i] == max_prob:
				max_prob_v.append((i+1, bot_prob[i]))
		#print("max prob vertices are:", max_prob_v)
		#print("pron for 32:", bot_prob[31])

		if set(new_highest_prob_vertices_ind) == set(highest_prob_vertices_ind):
			break
		else:
			highest_prob_vertices_ind = new_highest_prob_vertices_ind
		# print('OPINION MATRIX: ', opinion_matr)
		# print('Student weights: ', s_weight)
		# print('Highest prob vertices: ', highest_prob_vertices_ind)

		for index in highest_prob_vertices_ind:
			key = index + 1
			free_rider = False
			if key != client.home:
				# CLOSETEST NEIGHBOR STARTS
				min_vertex = int(get_path(key, client.home)[1])
				# closest neighbors
				if found_bots[key] == 0:
					sp_neighbor_weight = client.graph.get_edge_data(key, min_vertex).get("weight")
					min_neighbor_weight = math.inf
					for neighbor in client.graph.neighbors(key):
						w = client.graph.get_edge_data(key, neighbor).get("weight")
						if sp_neighbor_weight > 2*w and min_neighbor_weight > w and depth[neighbor] <= depth[key]: # pairs_dist[key][client.home] > pairs_dist[neighbor][client.home]
							min_neighbor_weight = w
							min_vertex = neighbor
				min_weight = pairs_dist[key][min_vertex] #pairs_dist[key][client.home]
				# print('ORIGINAL PREV[KEY]'+str(get_path(key, client.home)[1]))
				#print('WEIGHT to home = '+str(min_weight))
			    # CLOSEST NEIGHBOR ENDS

				# FREE RIDER STARTS
				for i in range(1, len(found_bots)):
					if found_bots[i] > 0 and client.graph.has_edge(key, i):
						weight = client.graph.get_edge_data(key, i).get('weight')
						if weight <= min_weight:
							min_weight = weight
							min_vertex = i
							free_rider = True
							#print('FREE RIDER'+str(min_vertex))
							#print('WEIGHT = '+str(min_weight))
				# FREE RIDER ENDS
				if lost_kids_count <= 0:
					break

				#print("student weight of no 6:", s_weight[5])
				if heavy_student:
					print("heavy student:", heavy_student+1)
					print("heavy student weight ratio:", s_weight[heavy_student]/sum(s_weight))
					print("heavy student WRONG:", student_wrong[heavy_student])
				max_w_student = []
				max_w = max(s_weight)
				for i in range(len(s_weight)):
					if s_weight[i] == max_w:
						max_w_student.append(i+1)
				print("max weight students are:", max_w_student)
				result = client.remote(key, min_vertex)
				remoted.append(key)
				print("Remoted weight =", client.graph.get_edge_data(key, min_vertex).get("weight"))
				#print("remoted vertices", sorted(remoted))


				if not result:
					true_result = False
					opinion_matr[key-1] = [-1]*client.k
					failed_remote += 1
					# # UPDATE STUDENT USING FALSE POSITIVE
					for student in range(1, client.k+1):
						if opinion_matr_fixed[key-1][student-1] != False:
							student_pf[student-1] += 1
							if student_pf[student-1] < 15:
								s_weight[student-1] -= 0.03 * abs(s_weight[student-1])
							else:
								s_weight[student-1] += 0.06 * abs(s_weight[student-1])
					# # UPDATE STUDENTS USING FALSE POSITIVE ENDS
				else:
					true_result = True
					lost_kids_count -= result - found_bots[key]
					found_bots[key] = 0
					found_bots[min_vertex] += result
					success_remote += 1
					if min_vertex == client.home:
						home_kids += result
					opinion_matr[key-1] = [-1]*client.k
					#  opinion_matr[min_vertex-1] = [-1]*client.k
				# UPDATE WEIGHT STARTS
				# for student in range(1, client.k+1):
				# 	if opinion_matr_fixed[key-1][student-1] != true_result:
				# 		student_wrong[student-1] += 1
				# 		if student_wrong[student-1] >= 40:
				# 			s_weight[student-1] += 50 * abs(s_weight[student-1])
				# 			#print(">40 student wrong:", student_wrong[student-1])
				# 			#print("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????")
				# 		if student_wrong[student-1] >= 45:
				# 			heavy_student = student
				# 			s_weight[student-1] += 2000 * abs(s_weight[student-1])
						# if student_wrong[student-1] < 15:
						# 	s_weight[student-1] -= 0.5 * abs(s_weight[student-1])
						# else:
						# 	s_weight[student-1] += 0.8 * abs(s_weight[student-1])
				for student in range(1, client.k+1):
					if opinion_matr_fixed[key-1][student-1] != true_result:
						student_wrong[student-1] += 1
				s_ind = np.argmax(student_wrong)
				if max(student_wrong) >= 50:
					heavy_student = s_ind
					s_weight[s_ind] = 2*sum(s_weight)
				# elif max(student_wrong) == 45:
				# 	heavy_student = s_ind
				# 	s_weight[s_ind] = 2*max(s_weight)
				# elif max(student_wrong) == 40:
				# 	heavy_student = s_ind
				# 	s_weight[s_ind] = 1.5*max(s_weight)




				# UPDATE WEIGHT ENDS
				the_student = check_student(client, student_wrong)
				if the_student:
					s_weight[the_student - 1] = 2000
				print("number of lost:" + str(lost_kids_count))
	print("number of lost:" + str(lost_kids_count))
	print("number of home kids after first while loop:" + str(home_kids))
	print(found_bots)
	print('Failed remote count: ', failed_remote)
	print('Success remote count: ', success_remote)
	print("Student wrongs:", student_wrong)
	#print("s weight:", s_weight)

	'''
	==============
	Bring all bots home by MST:
	1. Suppose that B is the set of vertices with bots. Run dynamic programming all pairs
	   Dijkstra’s algorithm (Floyd Warshall Algorithm) on the graph.
	2. Construct a graph G’ that consists of all vertices in B. For (u, v) in B, construct an undirected
	   edge with weight shortest_path(u, v, |V|) from the dynamic programming result.
	3. Construct a MST on G’, and categorize nodes according to their depth from h, then each time remotes
	   all the bots that are depth i from h to nodes with depth i - 1
	==============
	'''

	
	'''
	1) Start with a subtree T consisting of one given terminal vertex
	2) While T does not span all terminals
		a) Select a terminal x not in T that is closest to a vertex in T.
		b) Add to T the shortest path that connects x with T
	'''
	steiner_tree_v = [client.home]
	wandering_kids_vertices = []
	steiner_depth = [0]*(client.v+1)
	steiner_tree_prev = [0]*(client.v+1)
	for i in range(1, len(found_bots)):
		if found_bots[i] > 0 and i!= client.home:
			wandering_kids_vertices.append(i) # a list of all vertices with bots
	wandering_kids_vertices_fixed = wandering_kids_vertices.copy()

	while len(wandering_kids_vertices) !=0:
		min_dist = math.inf
		v_next = None
		v_curr = None
		for v in steiner_tree_v:
			for w in wandering_kids_vertices:
				if pairs_dist[v][w] < min_dist:
					min_dist = pairs_dist[v][w]
					v_next = w
					v_curr = v
		print("to be added to tree:", v_next, "to", v_curr)
		steiner_tree_v.extend(get_path(v_curr, v_next)[1:])
		v = v_next
		while v != v_curr:
			w = get_path(v, v_curr)[1] # next vertex on the path
			steiner_tree_prev[v] = w
			v = w
		print("steiner_tree_prev of", v_next,"is:", v_curr)
		steiner_tree_prev[v_next] = get_path(v_next, v_curr)[1]
		# print("steiner_tree_prev:", steiner_tree_prev)
		steiner_depth[v_next] = steiner_depth[v_curr]+len(get_path(v_curr, v_next))-1
		wandering_kids_vertices.remove(v_next)
	print("steiner_tree_v:", steiner_tree_v)
	# print("steiner_depth:", steiner_depth)
	sorted_steiner_depth = sorted(steiner_tree_v, key=lambda k: steiner_depth[k], reverse = True)

	for i in sorted_steiner_depth:
		# print("to be remoted:", i)
		# print("found_bots", found_bots)
		if found_bots[client.home] == 5:
			break
		if found_bots[i] > 0:
			# print("remote from", i, "to", steiner_tree_prev[i])
			result = client.remote(int(i), int(steiner_tree_prev[i]))
			print("remoted weight:", client.graph.get_edge_data(int(i), int(steiner_tree_prev[i])).get('weight'))
			found_bots[i] = 0
			found_bots[steiner_tree_prev[i]] += result



 #    # MST with all-pairs begins
    
	# mst_graph = nx.Graph(func='MST')
	# wandering_kids_vertices = []
	# for i in range(1, len(found_bots)):
	# 	if found_bots[i] > 0 and i!= client.home:
	# 		wandering_kids_vertices.append(i)
	# wandering_kids_vertices.append(client.home)
	# # print("wandering_kids_vertices:", wandering_kids_vertices)
	# for i in wandering_kids_vertices:
	# 	mst_graph.add_node(i)
	# # print('CONSTRUCTING MST_GRAPH')
	# for i in wandering_kids_vertices:
	# 	for j in wandering_kids_vertices:
	# 		if i != j:
	# 			mst_graph.add_edge(i, j, weight=pairs_dist[i][j])
	# mst = nx.minimum_spanning_tree(mst_graph, weight='weight')

	# def bfs(graph, client):
	# 	queue = [client.home]
	# 	visited = []
	# 	while len(queue) != 0:
	# 		curr = queue.pop(0)
	# 		if curr not in visited:
	# 			visited += [curr]
	# 			for v in graph.neighbors(curr):
	# 				queue += [v]
	# 	return visited
	# visited = bfs(mst, client)


	# dist, prev, depth = dijkstra(client, mst)
	# sorted_depth = sorted(range(len(depth)), key=lambda k: depth[k], reverse = True)
	# #  print('SORTED_DEPTH', sorted_depth)
	# for i in sorted_depth:
	# 	if depth[i] > 0 and i in visited:
	# 		#  print('WEIGHT', client.graph.get_edge_data(i, prev[i]).get('weight'))
	# 		cur_remote_v = i
	# 		while cur_remote_v != prev[i]:
	# 			print("get path:", get_path(cur_remote_v, prev[i]))
	# 			next_remote_v = int(get_path(cur_remote_v, prev[i])[1])
	# 			client.remote(cur_remote_v, next_remote_v)
	# 			print("remoted weight:", client.graph.get_edge_data(cur_remote_v, next_remote_v).get('weight'))
	# 			cur_remote_v = next_remote_v
	
	# # MST with all-pairs ends

	client.end()





	# total_dist = 0
	# for k in wandering_kids_vertices:
	# 	total_dist += dist[k]
	# print("shortest path total dist:", total_dist)
	# best_v = client.home
	# best_total_to_v = total_dist
	# for v in client.graph:
	# 	if v != client.home:
	# 		total_to_v = dist[v]
	# 		for k in wandering_kids_vertices:
	# 			if k != v:
	# 				total_to_v += client.graph.get_edge_data(k, v).get('weight')
	# 		print("best_v distance to", v, "is:", total_to_v)
	# 		if total_to_v < best_total_to_v:
	# 			best_total_to_v = total_to_v
	# 			best_v = v
	#
	#
	# if best_v != client.home:
	# 	print("best_v implemented!")
	# 	for key in wandering_kids_vertices:
	# 		client.remote(key, best_v)
	# 	while best_v != client.home:
	# 		result = client.remote(best_v, prev[best_v])
	# 		best_v = prev[best_v]
	# 	home_kids += result
	# else:
	# 	print("best_v NOT implemented!")
	# 	bots_vertex_depth = {}
	# 	while home_kids != client.l:
	# 		bots_loc_id = [i for i in range(1, len(found_bots)) if found_bots[i] > 0]
	# 		if bots_loc_id == [client.home]:
	# 			break
	# 		bots_vertex_depth = {i: depth[i] for i in bots_loc_id}
	# 		max_depth = max(bots_vertex_depth.values())
	# 		for key in bots_vertex_depth:
	# 			if bots_vertex_depth[key] == max_depth and key != client.home:
	# 				result = client.remote(key, prev[key])
	# 				found_bots[key] = 0
	# 				if prev[key] == client.home:
	# 					home_kids += result
	# 				else:
	# 					found_bots[prev[key]] = result
	# print('num home kids:' + str(home_kids))
	# print(opinion_matr == opinion_matr_fixed)
	# print()
	#
	# client.end()




	# The second while loop
	# while home_kids != client.l:
	# 	bots_loc_id = [i for i in range(1, len(found_bots)) if found_bots[i] > 0]
	# 	if bots_loc_id == [client.home]:
	# 		break
	# 	bots_vertex_depth = {i: depth[i] for i in bots_loc_id}
	# 	max_depth = max(bots_vertex_depth.values())
	# 	for key in bots_vertex_depth:
	# 		if bots_vertex_depth[key] == max_depth and key != client.home:
	# 			result = client.remote(key, prev[key])
	# 			found_bots[key] = 0
	# 			if prev[key] == client.home:
	# 				home_kids += result
	# 			else:
	# 				found_bots[prev[key]] = result


# while home_kids != client.l:
# 	bots_loc_id = [i for i in range(1, len(found_bots)) if found_bots[i] > 0]
#
# 	if bots_loc_id == [client.home]:
# 		break
# 	if client.home in bots_loc_id:
# 		bots_loc_id.remove(client.home)
# 	sp_weight_sum = sum([dist[id] for id in bots_loc_id])
# 	min_sum = sp_weight_sum
# 	min_intermediate_v = client.home
# 	for v in client.graph.neighbors(client.home):
# 		v_sum = sum([client.graph.get_edge_data(id,v).get('weight') for id in bots_loc_id if id != v]) + dist[v]
# 		if v_sum < min_sum:
# 			min_sum = v_sum
# 			min_intermediate_v = v
# 	print('FOUND V '+str(min_intermediate_v))
# 	print(bots_loc_id)
# 	results = [client.remote(id,min_intermediate_v) for id in bots_loc_id if id != min_intermediate_v]
# 	# found_bots[min_intermediate_v] = sum(results)
# 	for pos in bots_loc_id:
# 		found_bots[pos] = 0
# 	final_result = client.remote(min_intermediate_v, client.home)
# 	# home_kids += final_result
